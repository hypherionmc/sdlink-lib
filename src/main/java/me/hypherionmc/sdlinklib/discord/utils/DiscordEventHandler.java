package me.hypherionmc.sdlinklib.discord.utils;

import me.hypherionmc.sdlinklib.config.ConfigEngine;
import me.hypherionmc.sdlinklib.config.ModConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DiscordEventHandler extends ListenerAdapter {

    private final MinecraftEventHandler eventHandler;
    private final ModConfig modConfig;
    private final ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

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

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        ConfigEngine.logger.info("Successfully connected to discord");

        threadPool.scheduleAtFixedRate(() -> {
            try {
                if (event.getJDA().getStatus() == JDA.Status.CONNECTED) {
                    Activity act = Activity.of(Activity.ActivityType.DEFAULT, modConfig.general.botStatus
                            .replace("%players%", String.valueOf(eventHandler.getPlayerCount()))
                            .replace("%maxplayers%", String.valueOf(eventHandler.getMaxPlayerCount())));

                    event.getJDA().getPresence().setActivity(act);
                }
            } catch (Exception e) {
                if (modConfig.general.debugging) {
                    ConfigEngine.logger.error(e.getMessage());
                }
            }
        }, 0, modConfig.general.activityUpdateInterval, TimeUnit.SECONDS);
    }
}
