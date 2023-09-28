package net.foster.chatcontrol.listeners;

import net.foster.chatcontrol.data.Database;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public record PlayerJoinListener(Database database) implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        database.deserializePlayerData(player.getUniqueId());
    }
}
