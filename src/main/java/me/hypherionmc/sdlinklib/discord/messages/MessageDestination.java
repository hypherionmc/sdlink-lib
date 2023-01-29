package me.hypherionmc.sdlinklib.discord.messages;

public enum MessageDestination {
    CHAT,
    SERVER,
    CONSOLE;

    public boolean isChat() {
        return this == CHAT;
    }

    public boolean isServer() {
        return this == SERVER;
    }

    public boolean isConsole() {
        return this == CONSOLE;
    }
}
