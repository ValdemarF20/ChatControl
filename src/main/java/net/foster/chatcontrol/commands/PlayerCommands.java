package net.foster.chatcontrol.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Name;
import io.papermc.paper.chat.ChatRenderer;
import net.foster.chatcontrol.ChatUser;
import net.foster.chatcontrol.managers.ChatManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("chatcontrol|cc")
public class PlayerCommands extends BaseCommand {
    private final ChatManager chatManager;
    public PlayerCommands(ChatManager chatManager) {
        this.chatManager = chatManager;
    }

    @CommandAlias("select|s")
    @Description("Selects a specific chat")
    public void onChatSelect(Player sender, @Name("chat") String chatName) {
        ChatRenderer chat = chatManager.getChat(chatName);
        if(chat == null) {
            sender.sendMessage(Component.text("Incorrect chat name - see /cc list"));
        } else {
            sender.sendMessage(Component.text(chatName + " has been selected successfully!"));
        }
        chatManager.updateActiveChat(sender.getUniqueId(), chatName);
    }

    @CommandAlias("list")
    @Description("Lists all available chats")
    public void onChatSelect(Player sender) {
        sender.sendMessage(chatManager.getChats().values().toString());
    }

    @CommandAlias("show profanity|sp")
    @Description("Toggles whether profanity should be shown in chat or not")
    public void onProfanityToggle(Player player) {
        ChatUser chatUser = chatManager.getChatUser(player.getUniqueId());
        if(chatUser.shouldShowProfanity()) {
            player.sendMessage(Component.text("Profanity in chat has been disabled", NamedTextColor.GREEN));
            chatUser.setShowProfanity(false);
        } else {
            player.sendMessage(Component.text("Profanity in chat has been enabled", NamedTextColor.GREEN));
            chatUser.setShowProfanity(true);
        }
    }

    @HelpCommand
    public static void onHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }
}
