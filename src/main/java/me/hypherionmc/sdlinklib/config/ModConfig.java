package me.hypherionmc.sdlinklib.config;

import me.hypherionmc.moonconfig.core.conversion.Path;
import me.hypherionmc.moonconfig.core.conversion.SpecComment;
import me.hypherionmc.sdlinklib.config.configobjects.*;

public class ModConfig {

    @Path("general")
    @SpecComment("General Mod Config")
    public GeneralConfig general = new GeneralConfig();

    @Path("webhookConfig")
    @SpecComment("Webhook Config")
    public WebhookConfig webhookConfig = new WebhookConfig();

    @Path("chatConfig")
    @SpecComment("Chat Config")
    public ChatSettingsConfig chatConfig = new ChatSettingsConfig();

    @Path("messages")
    @SpecComment("Change the contents of certain event messages")
    public MessageConfig messageConfig = new MessageConfig();

    @Path("messageDestinations")
    @SpecComment("Change in which channel messages appear")
    public MessageChannelsConfig messageDestinations = new MessageChannelsConfig();

}
