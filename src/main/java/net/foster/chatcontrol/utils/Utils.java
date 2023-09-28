package net.foster.chatcontrol.utils;

import com.google.common.base.Charsets;
import net.foster.chatcontrol.ChatControl;
import net.foster.chatcontrol.data.ConfigManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import static net.foster.chatcontrol.ChatControl.PROFANITY;

public class Utils {
    private final static Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    /**
     * Gets all nearby players from given player location
     * @param player Method checks nearby players based on this player location
     * @param range Range of how far the method should check
     * @return An ArrayList with all the nearby players
     */
    public static ArrayList<Player> getNearbyPlayers(Player player, int range){
        ArrayList<Player> nearby = new ArrayList<>();
        for (Entity entity : player.getNearbyEntities(range, range, range)){
            if (entity instanceof Player){
                nearby.add((Player) entity);
            }
        }
        return nearby;
    }

    /**
     * Method used to check for profanity
     * @param inputSentence Sentence to check for profanity in
     * @return true if sentence contains profanity, false if not
     */
    public static boolean checkForProfanity(String inputSentence) {
        if(PROFANITY.isEmpty()) {
            PROFANITY.addAll(ConfigManager.getProfanityWords());
        }

        for(String profanityWord : PROFANITY) {
            if (inputSentence.contains(profanityWord)) {
                return true;
            }
        }

        return false;
    }

    /* Config stuff */
    /**
     * Use this method instead of JavaPlugin#saveResource() to avoid console spam
     * JavaPlugin#saveResource() should be used if you want to use the "boolean replace" parameter
     * This method will not replace a file.
     * @param file File that will be copied from resources to data folder
     * @param path Path inside data holder
     * @return The YamlConfiguration for the resource
     */
    public static FileConfiguration saveResource(File file, String path) {
        ChatControl bossEvents = ChatControl.getInstance();
        try {
            if(!Files.exists(Paths.get(path))) {
                bossEvents.saveResource(path, false);
            }
        } catch(IllegalArgumentException e) {
            LOGGER.error(path + " could not be created", e);
        }

        return YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Use this method instead of JavaPlugin#saveResource() to avoid console spam
     * JavaPlugin#saveResource() should be used if you want to use the "boolean replace" parameter
     * This method will not replace a file.
     * @param path Path inside data holder
     */
    public static void saveResource(String path) {
        ChatControl bossEvents = ChatControl.getInstance();
        try {
            if(!Files.exists(Paths.get(path))) {
                bossEvents.saveResource(path, false);
            }
        } catch(IllegalArgumentException e) {
            LOGGER.error(path + " could not be created", e);
        }
    }

    /**
     * Reload a resource using given file and path
     * @param file File for resource file
     * @param path Path to resource file
     * @return A new reloaded {@link FileConfiguration}
     */
    public static FileConfiguration reloadResource(File file, String path) {
        ChatControl bossEvents = ChatControl.getInstance();
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        final InputStream defConfigStream = bossEvents.getResource(path);
        if (defConfigStream == null) {
            return null;
        }
        configuration.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, Charsets.UTF_8)));

        return configuration;
    }
}
