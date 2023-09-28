package net.foster.chatcontrol.listeners;

import net.foster.chatcontrol.data.Database;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public record PlayerQuitListener(Database database) implements Listener {
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        database.serializePlayerData(player.getUniqueId());
    }
}
