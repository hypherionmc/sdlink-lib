package me.hypherionmc.sdlinklib.config.configobjects;

import me.hypherionmc.nightconfig.core.conversion.Path;
import me.hypherionmc.nightconfig.core.conversion.SpecComment;

public class WebhookConfig {

    @Path("enabled")
    @SpecComment("Should webhook messages be used")
    public boolean enabled = false;

    @Path("webhookurl")
    @SpecComment("The URL of the channel webhook to use for Chat Messages")
    public String webhookurl = "";

    @Path("webhookurlLogs")
    @SpecComment("The URL of the channel webhook to use for Server Messages Messages")
    public String webhookurlLogs = "";

    @Path("serverAvatar")
    @SpecComment("A DIRECT link to an image to use as the avatar for server messages. Also used for embeds")
    public String serverAvatar = "";

    @Path("serverName")
    @SpecComment("The name to display for Server messages when using Webhooks")
    public String serverName = "Minecraft Server";

}
