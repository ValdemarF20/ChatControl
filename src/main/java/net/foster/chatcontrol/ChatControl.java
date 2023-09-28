package net.foster.chatcontrol;

import co.aikar.commands.PaperCommandManager;
import net.foster.chatcontrol.commands.AdminCommands;
import net.foster.chatcontrol.commands.PlayerCommands;
import net.foster.chatcontrol.data.ConfigManager;
import net.foster.chatcontrol.data.Database;
import net.foster.chatcontrol.listeners.PlayerJoinListener;
import net.foster.chatcontrol.listeners.PlayerMessageListener;
import net.foster.chatcontrol.listeners.PlayerQuitListener;
import net.foster.chatcontrol.managers.ChatManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashSet;

public final class ChatControl extends JavaPlugin {
    private final static Logger LOGGER = LoggerFactory.getLogger(ChatControl.class);
    private static ChatControl instance;

    /* Managers */
    private ChatManager chatManager;
    private ConfigManager configManager;

    // Data
    private Database database;
    public static String TABLE_NAME;
    public static String DATABASE_PATH;
    public static HashSet<String> PROFANITY = new HashSet<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        LOGGER.info("ChatControl has been enabled");

        instance = this;

        /* Managers */
        // High priority (independent of all other classes)
        PaperCommandManager manager = new PaperCommandManager(this);
        manager.enableUnstableAPI("help");
        configManager = ConfigManager.getInstance();
        chatManager = new ChatManager();
        chatManager.createChats();

        // Dependent on only itself and high priority classes
        setupServer(); // Depends on ChatManager

        // Dependent classes, ordered after which dependencies are required

        // Commands
        manager.registerCommand(new AdminCommands(chatManager));
        manager.registerCommand(new PlayerCommands(chatManager));

        // Listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(database),  this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(database),  this);
        getServer().getPluginManager().registerEvents(new PlayerMessageListener(chatManager),  this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void reload() {
        LOGGER.info("ChatControl has been reloaded");
        configManager.reload();
    }

    public static ChatControl getInstance() {
        return instance;
    }

    /**
     * Sets up all that's required for the database to work
     */
    private void setupServer() {
        /* Setup of Database management */
        DATABASE_PATH = getDataFolder().getAbsolutePath() + File.separator + ConfigManager.getString("database.path") + ".db";
        LOGGER.info(DATABASE_PATH);

        database = new Database(this, chatManager);
        database.initialize();
    }
}
