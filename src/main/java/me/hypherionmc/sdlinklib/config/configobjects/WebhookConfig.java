package me.hypherionmc.sdlinklib.config.configobjects;

import me.hypherionmc.nightconfig.core.conversion.Path;
import me.hypherionmc.nightconfig.core.conversion.SpecComment;

public class WebhookConfig {

    @Path("enabled")
    @SpecComment("Should webhook messages be used")
    public boolean enabled = false;

    @Path("webhookurl")
    @SpecComment("The URL of the channel webhook")
    public String webhookurl = "";

    @Path("serverAvatar")
    @SpecComment("A DIRECT link to an image to use as the avatar for server messages")
    public String serverAvatar = "";

}
