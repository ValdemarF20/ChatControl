package net.foster.chatcontrol.listeners;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.ChatEvent;
import net.foster.chatcontrol.ChatUser;
import net.foster.chatcontrol.chats.GlobalChat;
import net.foster.chatcontrol.chats.LocalChat;
import net.foster.chatcontrol.managers.ChatManager;
import net.foster.chatcontrol.utils.Utils;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Set;

public record PlayerMessageListener(ChatManager chatManager) implements Listener {
    @EventHandler
    public void onMessageSend(ChatEvent e) {
        Player player = e.getPlayer();
        ChatRenderer chat = chatManager().getActiveChat(player.getUniqueId());
        String message = PlainTextComponentSerializer.plainText().serialize(e.message());
        if(chat == null) {
            player.sendMessage("An error has occurred - contact staff");
            return;
        }

        if(chat instanceof GlobalChat) {
            Set<Audience> viewersCopy = new HashSet<>(e.viewers());
            for (Audience viewer : viewersCopy) { // Loop through all viewers
                if(!(viewer instanceof Player playerViewer)) { // Only check for players
                    continue;
                }
                ChatUser chatUser = chatManager.getChatUser(playerViewer.getUniqueId());
                boolean senderIsViewer = playerViewer.getUniqueId().equals(player.getUniqueId());

                // Check for profanity
                if(Utils.checkForProfanity(message)) {
                    // Message contains profanity
                    if(!(chatUser.shouldShowProfanity())) {
                        // Player shouldn't see profanity (don't remove for sender)
                        if(!senderIsViewer) {
                            e.viewers().remove(viewer);
                        }
                    }
                }
            }
        } else if(chat instanceof LocalChat) {
            Set<Audience> viewersCopy = new HashSet<>(e.viewers());
            for (Audience viewer : viewersCopy) { // Loop through all viewers
                if(!(viewer instanceof Player playerViewer)) { // Only check for players
                    continue;
                }
                boolean senderIsViewer = playerViewer.getUniqueId().equals(player.getUniqueId());

                // Check if playerViewer isn't within range
                if(!(Utils.getNearbyPlayers(player, ((LocalChat) chat).range()).contains(playerViewer))) {
                    // Remove viewer (don't remove for sender)
                    if(!senderIsViewer) {
                        e.viewers().remove(viewer);
                    }
                } else { // Viewer is within range
                    ChatUser chatUser = chatManager.getChatUser(playerViewer.getUniqueId());

                    // Check for profanity
                    if(Utils.checkForProfanity(message)) {
                        // Message contains profanity
                        if(!(chatUser.shouldShowProfanity())) {
                            // Player shouldn't see profanity (don't remove for sender)
                            if(!senderIsViewer) {
                                e.viewers().remove(viewer);
                            }
                        }
                    }
                }
            }
        }
        e.renderer(chat);
    }
}
