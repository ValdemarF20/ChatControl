package net.foster.chatcontrol;

import io.papermc.paper.chat.ChatRenderer;

import java.util.UUID;

public class ChatUser {
    private final UUID uuid;
    private ChatRenderer activeChat;
    private boolean showProfanity;

    public ChatUser(UUID uuid, ChatRenderer activeChat, boolean showProfanity) {
        this.uuid = uuid;
        this.activeChat = activeChat;
        this.showProfanity = showProfanity;
    }

    public boolean shouldShowProfanity() {
        return showProfanity;
    }

    public void setShowProfanity(boolean showProfanity) {
        this.showProfanity = showProfanity;
    }

    public ChatRenderer getActiveChat() {
        return activeChat;
    }

    public void setActiveChat(ChatRenderer activeChat) {
        this.activeChat = activeChat;
    }
}
