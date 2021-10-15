package me.hypherionmc.sdlinklib.discord.utils;

import me.hypherionmc.sdlinklib.config.ModConfig;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class DiscordEventHandler extends ListenerAdapter {

    private final MinecraftEventHandler eventHandler;
    private final ModConfig modConfig;

    public DiscordEventHandler(MinecraftEventHandler eventHandler, ModConfig config) {
        this.eventHandler = eventHandler;
        this.modConfig = config;
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (event.getChannel().getIdLong() == modConfig.chatConfig.channelID) {
            if ((modConfig.chatConfig.ignoreBots && !event.getAuthor().isBot()) && !event.isWebhookMessage() && event.getAuthor() != event.getJDA().getSelfUser()) {
                eventHandler.discordMessageReceived(event.getAuthor().getName(), event.getMessage().getContentStripped());
            }
        }
    }
}
