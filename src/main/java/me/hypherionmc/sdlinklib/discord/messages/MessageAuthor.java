package me.hypherionmc.sdlinklib.discord.messages;

import static me.hypherionmc.sdlinklib.config.ConfigController.modConfig;

public class MessageAuthor {

    public static final MessageAuthor SERVER = new MessageAuthor(modConfig.webhookConfig.serverName, modConfig.webhookConfig.serverAvatar, true);

    private final String username;
    private final String avatar;
    private final boolean isServer;

    private MessageAuthor(String username, String avatar, boolean isServer) {
        this.username = username;
        this.avatar = avatar;
        this.isServer = isServer;
    }

    public static MessageAuthor of(String username, String uuid) {
        return new MessageAuthor(username, modConfig.chatConfig.playerAvatarType.getUrl().replace("{uuid}", uuid), false);
    }

    public String getAvatar() {
        return avatar;
    }

    public String getUsername() {
        return username;
    }

    public boolean isServer() {
        return isServer;
    }
}
