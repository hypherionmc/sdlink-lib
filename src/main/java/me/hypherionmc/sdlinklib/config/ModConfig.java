package me.hypherionmc.sdlinklib.config;

import me.hypherionmc.moonconfig.core.conversion.Path;
import me.hypherionmc.moonconfig.core.conversion.SpecComment;
import me.hypherionmc.sdlinklib.config.configobjects.*;

public class ModConfig {

    @Path("general")
    @SpecComment("General Mod Config")
    public GeneralConfig generalConfig = new GeneralConfig();

    @Path("botConfig")
    @SpecComment("Config specific to the discord bot")
    public BotSettingsConfig botConfig = new BotSettingsConfig();

    @Path("channels")
    @SpecComment("Config relating to the discord channels to use with the mod")
    public ChannelConfig channelConfig = new ChannelConfig();

    @Path("webhooks")
    @SpecComment("Webhook Config")
    public WebhookConfig webhookConfig = new WebhookConfig();

    @Path("chat")
    @SpecComment("Chat Config")
    public ChatSettingsConfig chatConfig = new ChatSettingsConfig();

    @Path("messages")
    @SpecComment("Change the contents of certain event messages")
    public MessageConfig messageConfig = new MessageConfig();

    @Path("messageDestinations")
    @SpecComment("Change in which channel messages appear")
    public MessageChannelsConfig messageDestinations = new MessageChannelsConfig();

    @Path("linkedCommands")
    @SpecComment("Execute Minecraft commands in Discord")
    public LinkedCommandsConfig linkedCommands = new LinkedCommandsConfig();

}
