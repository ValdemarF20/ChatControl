package net.foster.chatcontrol.chats;

import io.papermc.paper.chat.ChatRenderer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * A GlobalChat is like the standard chat, has unlimited range
 * @param chatName
 * @param baseColor
 * @param nameColor
 * @param messageColor
 */
public record GlobalChat(String chatName, int baseColor, int nameColor, int messageColor) implements ChatRenderer {
    @Override
    public @NotNull Component render(@NotNull Player source, @NotNull Component sourceDisplayName, @NotNull Component message, @NotNull Audience viewer) {
        return Component.text("[" + chatName + "]", TextColor.color(nameColor))
                .append(Component.space().color(TextColor.color(baseColor))
                        .append(sourceDisplayName).append(Component.space())
                        .append(Component.text(">>")).append(Component.space())
                        .append(message.color(TextColor.color(messageColor))));
    }

    @Override
    public String toString() {
        return chatName;
    }
}