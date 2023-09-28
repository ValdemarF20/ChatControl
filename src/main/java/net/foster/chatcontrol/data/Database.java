package net.foster.chatcontrol.data;

import net.foster.chatcontrol.ChatControl;
import net.foster.chatcontrol.managers.ChatManager;
import net.foster.chatcontrol.utils.Errors;
import net.foster.chatcontrol.utils.lambda.SafeConsumer;
import net.foster.chatcontrol.utils.lambda.SafeFunction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.javax.SQLiteConnectionPoolDataSource;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.foster.chatcontrol.ChatControl.TABLE_NAME;

/**
 * Database manager that should handle all data going to the local SQLite database
 */
public class Database {

    public static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    private static final SQLiteConnectionPoolDataSource DATA_SOURCE = new SQLiteConnectionPoolDataSource(); // Doesn't use pool for now
    private static final Logger LOGGER = LoggerFactory.getLogger(Database.class);
    private final ChatControl chatControl;
    private final ChatManager chatManager;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private Connection writeConnection;

    public Database(ChatControl chatControl, ChatManager chatManager) {
        this.chatControl = chatControl;
        this.chatManager = chatManager;
    }

    /**
     * Opens a connection to the database
     * For SQLite a connection can be opened for every query without a performance decrease
     * Because the database is local and not remote
     *
     * @return A {@link CompletableFuture<Connection>} containing the {@link Connection}
     */
    private static CompletableFuture<Connection> openConnection() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return DATA_SOURCE.getConnection();
            } catch (SQLException e) {
                LOGGER.error("Error when opening connection");
                throw new RuntimeException(e);
            }
        }, EXECUTOR);
    }

    /**
     * Creates the database if necessary and opens a connection to the local file
     */
    public void initialize() {
        TABLE_NAME = ConfigManager.getString("database.table-name");
        if (TABLE_NAME == null) {
            LOGGER.error("database.table-name not found in config.yml");
            return;
        }
        LOGGER.info("Table name found");

        /* Setup DataSource (instead of the horrible DriverManager) */
        DATA_SOURCE.setDatabaseName(TABLE_NAME);
        DATA_SOURCE.setUrl("jdbc:sqlite:" + ChatControl.DATABASE_PATH);

        if (!Files.exists(Paths.get(ChatControl.DATABASE_PATH))) {
            openConnection().thenAccept(connection -> {
                try (Connection newConnection = connection;
                     PreparedStatement statement = newConnection.prepareStatement(
                             "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                                     " (\"uuid\" VARCHAR(36) PRIMARY KEY, \"active_chat\" INTEGER, \"show_profanity\" BOOLEAN)")) {
                    statement.execute();
                    LOGGER.info("SQLite file created");
                } catch (SQLException e) {
                    LOGGER.error("Something went wrong while creating the SQLite file", e);
                }
            });
        }

        try {
            writeConnection = openConnection().get();
        } catch (Exception e) {
            LOGGER.error("Write connection could not be created", e);
            throw new RuntimeException();
        }

        autoSave();
    }

    /**
     * Executes a query to database
     *
     * @param query       A String query to send to the database
     * @param initializer A {@link Consumer} that can throw a checked exception.
     * @param process     A {@link Function} that can throw a checked exception.
     * @param <T>         The type of the result for {@link SafeFunction}
     * @return A {@link CompletableFuture<T>} for handling exceptions
     */
    private <T> CompletableFuture<T> query(String query, @NotNull SafeConsumer<PreparedStatement> initializer, @NotNull SafeFunction<ResultSet, T> process) {
        return openConnection().thenApply(connection -> {
            try (Connection newConnection = connection;
                 PreparedStatement statement = newConnection.prepareStatement(query);
                 ResultSet resultSet = statement.executeQuery()) {
                initializer.consume(statement);
                return process.apply(resultSet);
            } catch (Exception e) {
                LOGGER.error("Error when preparing statement for query: " + query);
                Errors.sneakyThrow(e);
                return null;
            }
        });
    }

    /**
     * Updates query to database
     * Must be a SQL Data Manipulation Language (DML) statement, such as INSERT, UPDATE or DELETE
     *
     * @param query       A String query to send to the database
     * @param initializer A {@link Consumer} that can throw a checked exception.
     * @return A {@link CompletableFuture<Void>} for handling exceptions
     */
    private CompletableFuture<Void> update(String query, @NotNull SafeConsumer<PreparedStatement> initializer) {
        Lock writeLock = lock.writeLock();
        return CompletableFuture.runAsync(() -> {
            writeLock.lock();
            try (PreparedStatement statement = writeConnection.prepareStatement(query)) {
                initializer.consume(statement);
                statement.executeUpdate();
            } catch (Exception e) {
                LOGGER.error("Error when preparing statement for query: " + query, e);
                Errors.sneakyThrow(e);
            } finally {
                writeLock.unlock();
            }
        }, EXECUTOR);
    }

    /**
     * Create default data for player with given uuid
     * This method will not change anything if database already contains uuid
     *
     * @param uuid Player's uuid to create default data for
     * @return
     */
    public CompletableFuture<Void> createDefaultData(UUID uuid) {
        return this.update("INSERT OR IGNORE INTO " + TABLE_NAME + "(uuid, active_chat, show_profanity) VALUES(?, ?, ?)",
                statement -> {
                    statement.setString(1, uuid.toString());
                    statement.setString(2, chatManager.getDefaultChat().toString());
                    statement.setBoolean(3, ConfigManager.getBoolean("defaults.show-profanity"));
                });
    }

    /**
     * Save player data from given uuid
     * @param uuid Player uuid to update data for
     */
    public void serializePlayerData(UUID uuid) {
        this.update("UPDATE " + TABLE_NAME +
                        " SET active_chat = ? , " + "show_profanity = ?" +
                        " WHERE uuid = ?",
                statement -> {
                    statement.setString(3, uuid.toString());
                    statement.setString(1, chatManager.getActiveChat(uuid).toString());
                    statement.setBoolean(2, chatManager.getChatUser(uuid).shouldShowProfanity());
                });
    }

    /**
     * Load player data from the database into the server
     * @param uuid UUID for player that should have loaded data
     * @return A {@link CompletableFuture<Void>} for handling exceptions
     */
    public CompletableFuture<Void> deserializePlayerData(UUID uuid) {
        /* Create default data if database doesn't contain uuid */
        createDefaultData(uuid);

        /* Update player data */
        return this.query("SELECT * FROM " + TABLE_NAME + " WHERE uuid = '" + uuid + "';",
                statement -> {
                },
                resultSet -> {
                    resultSet.next();
                    String activeChat = resultSet.getString("active_chat");
                    boolean showProfanity = resultSet.getBoolean("show_profanity");
                    chatManager.addChatUser(uuid, activeChat, showProfanity);

                    return null;
                });
    }

    /**
     * Makes sure that every task is stopped before closing the JVM
     */
    private void shutdown() {
        EXECUTOR.shutdown(); // Stops new tasks from being scheduled to the executor.

        try {
            if (!EXECUTOR.awaitTermination(30, TimeUnit.SECONDS)) { // Wait for existing tasks to terminate.
                EXECUTOR.shutdownNow(); // Cancel currently executing tasks that didn't finish in the time.

                if (!EXECUTOR.awaitTermination(30, TimeUnit.SECONDS)) { // Wait for tasks to respond to cancellation.
                    LOGGER.error("Pool failed to terminate");
                }
            }
        } catch (InterruptedException e) {
            EXECUTOR.shutdownNow(); // Cancel currently executing tasks if interrupted.
            Thread.currentThread().interrupt(); // Preserve interrupt status.
        }
    }

    /**
     * Saves the data for all players
     */
    private void serializePlayerDatas() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            serializePlayerData(player.getUniqueId());
        }
    }

    /**
     * Serializes the data synchronized to avoid running task after server has stopped
     *
     * @param shutdown Whether the current tasks running should stop or not
     */
    public void save(boolean shutdown) {
        serializePlayerDatas();

        if (shutdown) {
            shutdown();
        }
    }

    /**
     * Runs at given interval to autosave the data into database
     */
    private void autoSave() {
        new BukkitRunnable() {
            @Override
            public void run() {
                save(false);
            }
        }.runTaskTimerAsynchronously(chatControl, ConfigManager.getDataSaveInterval(), ConfigManager.getDataSaveInterval());
    }
}
