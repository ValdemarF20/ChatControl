package net.foster.chatcontrol.managers;

import io.papermc.paper.chat.ChatRenderer;
import net.foster.chatcontrol.ChatUser;
import net.foster.chatcontrol.data.ConfigManager;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatManager {
    private final Map<String, ChatRenderer> chats = new HashMap<>();
    private final Map<UUID, ChatUser> chatUsers = new HashMap<>();

    @Nullable
    public ChatRenderer getChat(String chatName) {
        return chats.get(chatName);
    }

    public Map<String, ChatRenderer> getChats() {
        return chats;
    }

    @Nullable
    public ChatRenderer getActiveChat(UUID uuid) {
        return chatUsers.get(uuid).getActiveChat();
    }

    public void createChats() {
        chats.putAll(ConfigManager.createChats());
    }

    public void updateActiveChat(UUID uuid, String chatName) {
        ChatRenderer chat = getChat(chatName);
        if(chat == null) {
            chat = getDefaultChat();
        }
        chatUsers.get(uuid).setActiveChat(chat);
    }

    public ChatRenderer getDefaultChat() {
        String chatName = ConfigManager.getString("defaults.chat");
        if(chatName == null) {
            throw new RuntimeException("Default chat could not be found in config.yml (defaults.chat)");
        }
        ChatRenderer chat = getChat(chatName);
        if(chat == null) {
            throw new RuntimeException("Default chat could not be created");
        }
        return chat;
    }

    public void addChat(String chatName, ChatRenderer chat) {
        chats.put(chatName, chat);
    }

    public void addChatUser(UUID uuid, String chatName, boolean showProfanity) {
        ChatUser chatUser = new ChatUser(uuid, getChat(chatName), showProfanity);
        chatUsers.put(uuid, chatUser);
        updateActiveChat(uuid, chatName);
    }

    public ChatUser getChatUser(UUID uuid) {
        return chatUsers.get(uuid);
    }
}
