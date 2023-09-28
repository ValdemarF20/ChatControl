package net.foster.chatcontrol.data;

import io.papermc.paper.chat.ChatRenderer;
import net.foster.chatcontrol.ChatControl;
import net.foster.chatcontrol.chats.GlobalChat;
import net.foster.chatcontrol.chats.LocalChat;
import net.foster.chatcontrol.managers.ChatType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {
    private static ChatControl main;
    public FileConfiguration config;
    private static ConfigManager instance;

    /* Plugin specific instances */
    private static ConfigurationSection chatsSection;

    private ConfigManager() {
        main = ChatControl.getInstance();
        config = main.getConfig();

        instantiate();
    }

    /**
     * Method used on server startup
     * starts an autosaving task of the config.yml file
     */
    private void instantiate() {
        setupConfig();

        new BukkitRunnable() {
            @Override
            public void run() {
                saveConfig();
            }
        }.runTaskTimerAsynchronously(main, getInt("config-save-interval") * 20L * 60L, getInt("config-save-interval") * 20L * 60L);
    }

    /**
     * Sets up all required config files for plugin
     */
    public void setupConfig() {
        config.options().copyDefaults(true);
        main.saveDefaultConfig();

        chatsSection = config.getConfigurationSection("chats");
        if(chatsSection == null) {
            throw new RuntimeException("Invalid config.yml (need to add \"chats\" section)");
        }
    }

    /**
     * Saves all config files from memory to disk
     */
    public void saveConfig() {
        config.options().copyDefaults();
        main.saveConfig();
    }

    /**
     * Reload all configs from disk
     */
    public void reload() {
        main.reloadConfig();
    }

    /**
     * Use method to get an instance of singleton class
     * @return Instance of the class
     */
    public static ConfigManager getInstance() {
        if(instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    public static String getString(String path) {
        return main.getConfig().getString(path);
    }

    public static String getString(String path, String defaultOption) {
        return main.getConfig().getString(path, defaultOption);
    }

    public static int getInt(String path) {
        return main.getConfig().getInt(path);
    }

    public static boolean getBoolean(String path) { return main.getConfig().getBoolean(path); }

    public static double getDouble(String path) {
        return main.getConfig().getDouble(path);
    }

    public static List<String> getList(String path) {
        return main.getConfig().getStringList(path);
    }

    public static ConfigurationSection getConfigurationSection(String path) {
        return main.getConfig().getConfigurationSection(path);
    }

    public static long getDataSaveInterval() {
        return main.getConfig().getLong("database.save-interval") * 20 /* Seconds */ * 60 /* Minutes */;
    }


    /* Plugin specific methods */

    /**
     * Create all chats from config.yml chats section and load them into ChatManager
     * @return A {@link Map<String, ChatRenderer>} containing all the chat names with corresponding ChatRenderer instances
     */
    public static Map<String, ChatRenderer> createChats() {
        final Map<String, ChatRenderer> chats = new HashMap<>();

        for (String chatName : chatsSection.getKeys(false)) {
            ChatRenderer chat;
            ChatType type = ChatType.valueOf(chatsSection.getString(chatName + ".type", "local").toLowerCase());
            int baseColor = chatsSection.getInt(chatName + ".base-color", 0xFFFFFF);
            int messageColor = chatsSection.getInt(chatName + ".message-color", 0xFFFFFF);
            int nameColor = chatsSection.getInt(chatName + ".name-color", 0xF0FF);

            switch (type) {
                case global -> chat = new GlobalChat(chatName, baseColor, nameColor, messageColor);
                case local -> {
                    int range = ConfigManager.getInt("chats." + chatName + ".range");
                    chat = new LocalChat(chatName, range, baseColor, nameColor, messageColor);
                }
                default -> { continue; }
            }

            chats.putIfAbsent(chatName, chat);
        }

        return chats;
    }

    /**
     * Add a new chat to config.yml
     * @param chatName New chat's name
     * @param chatType New chat's type
     * @param range New chat's range (only used if type is local)
     * @param baseColor New chat's base color
     * @param nameColor New chat's name color
     * @param messageColor New chat's message color
     */
    public static void addChat(String chatName,
                               ChatType chatType,
                               int range,
                               int baseColor, int nameColor, int messageColor) {
        main.getConfig().set(chatName + ".type", chatType.name());
        if(chatType == ChatType.local) {
            main.getConfig().set(chatName + ".range", range);
        }
        main.getConfig().set(chatName + ".base-color", baseColor);
        main.getConfig().set(chatName + ".name-color", nameColor);
        main.getConfig().set(chatName + ".message-color", messageColor);
        main.saveConfig();
    }

    public static List<String> getProfanityWords() {
        return main.getConfig().getStringList("profanity-words");
    }
}
