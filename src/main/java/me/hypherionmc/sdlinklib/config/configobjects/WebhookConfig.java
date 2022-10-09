package me.hypherionmc.sdlinklib.config.configobjects;

import me.hypherionmc.moonconfig.core.conversion.Path;
import me.hypherionmc.moonconfig.core.conversion.SpecComment;

public class WebhookConfig {

    @Path("enabled")
    @SpecComment("Should webhook messages be used")
    public boolean enabled = false;

    @Path("chatWebhook")
    @SpecComment("The URL of the channel webhook to use for Chat Messages")
    public String chatWebhook = "";

    @Path("eventsWebhook")
    @SpecComment("The URL of the channel webhook to use for Server Messages")
    public String eventsWebhook = "";

    @Path("chatEmbeds")
    @SpecComment("Use EMBED style messages for chat channel messages")
    public boolean chatEmbeds = false;

    @Path("eventEmbeds")
    @SpecComment("Use EMBED style messages for event channel messages")
    public boolean eventEmbeds = false;

    @Path("serverAvatar")
    @SpecComment("A DIRECT link to an image to use as the avatar for server messages. Also used for embeds")
    public String serverAvatar = "";

    @Path("serverName")
    @SpecComment("The name to display for Server messages when using Webhooks")
    public String serverName = "Minecraft Server";

}
