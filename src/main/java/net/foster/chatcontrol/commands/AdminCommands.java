package net.foster.chatcontrol.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import io.papermc.paper.chat.ChatRenderer;
import net.foster.chatcontrol.ChatControl;
import net.foster.chatcontrol.chats.GlobalChat;
import net.foster.chatcontrol.chats.LocalChat;
import net.foster.chatcontrol.data.ConfigManager;
import net.foster.chatcontrol.managers.ChatManager;
import net.foster.chatcontrol.managers.ChatType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

@CommandAlias("chatcontroladmin|cca")
@CommandPermission("chatcontrol.admin")
public class AdminCommands extends BaseCommand {
    private final ChatManager chatManager;
    private final int defaultBaseColor;
    private final int defaultNameColor;
    private final int defaultMessageColor;
    private final int defaultRange;


    public AdminCommands(ChatManager chatManager) {
        this.chatManager = chatManager;

        defaultBaseColor = ConfigManager.getInt("defaults.base-color");
        defaultNameColor = ConfigManager.getInt("defaults.name-color");
        defaultMessageColor = ConfigManager.getInt("defaults.message-color");
        defaultRange = ConfigManager.getInt("defaults.range");

    }

    @CommandAlias("reload")
    @Description("Reloads the config files")
    public void onReload(CommandSender sender) {
        ChatControl.getInstance().reload();
    }

    @CommandAlias("create")
    @Description("Creates a new chat type, range can only be set if type has been specified to local")
    public void onCreate(CommandSender sender,
                         @Name("type") ChatType chatType,
                         @Name("chat name") String chatName,
                         @Optional @Name("base color") Integer baseColor,
                         @Optional @Name("name color") Integer nameColor,
                         @Optional @Name("message color") Integer messageColor,
                         @Optional @Name("range") Integer range) {
        if(baseColor == null || baseColor == 0) {
            baseColor = defaultBaseColor;
        }
        if(nameColor == null || nameColor == 0) {
            nameColor = defaultNameColor;
        }
        if(messageColor == null || messageColor == 0) {
            messageColor = defaultMessageColor;
        }

        ChatRenderer chat;
        if(chatType == ChatType.global) {
            if(range != null) {
                sender.sendMessage(Component.text("Global chat cannot have a range", NamedTextColor.RED));
                return;
            }
            chat = new GlobalChat(chatName, baseColor, nameColor, messageColor);
        } else {
            if(range == null) {
                sender.sendMessage(Component.text("No range specified, default is: " + defaultRange, NamedTextColor.GREEN));
                range = defaultRange;
            }
            chat = new LocalChat(chatName, range, baseColor, nameColor, messageColor);
        }

        chatManager.addChat(chatName, chat);
        sender.sendMessage(Component.text("Chat successfully added: " + chatName, NamedTextColor.GREEN));
        if(range == null) {
            range = defaultRange;
        }
        ConfigManager.addChat(chatName, chatType, range, baseColor, nameColor, messageColor);
    }

    @HelpCommand
    public static void onHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }
}
