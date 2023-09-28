package net.foster.chatcontrol.chats;

import io.papermc.paper.chat.ChatRenderer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * LocalChat instances only sends messages to players within specific range
 * @param chatName
 * @param range
 * @param baseColor
 * @param nameColor
 * @param messageColor
 */
public record LocalChat(String chatName, int range, int baseColor, int nameColor, int messageColor) implements ChatRenderer {
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