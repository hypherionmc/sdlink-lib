package me.hypherionmc.sdlinklib.discord;

import me.hypherionmc.sdlinklib.config.ConfigController;
import me.hypherionmc.sdlinklib.config.ModConfig;
import me.hypherionmc.sdlinklib.services.PlatformServices;
import me.hypherionmc.sdlinklib.utils.SystemUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static me.hypherionmc.sdlinklib.discord.BotEngine.threadPool;

public class DiscordEventHandler extends ListenerAdapter {

    private final ModConfig modConfig;

    public DiscordEventHandler(ModConfig config) {
        this.modConfig = config;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getChannel().getIdLong() == modConfig.chatConfig.channelID) {
            if ((modConfig.chatConfig.ignoreBots && !event.getAuthor().isBot()) && !event.isWebhookMessage() && event.getAuthor() != event.getJDA().getSelfUser()) {
                PlatformServices.mc.discordMessageEvent(event.getAuthor().getName(), event.getMessage().getContentStripped());
            }
        }
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        ConfigController.logger.info("Successfully connected to discord");

        threadPool.scheduleAtFixedRate(() -> {
            try {
                if (event.getJDA().getStatus() == JDA.Status.CONNECTED) {
                    Activity act = Activity.of(Activity.ActivityType.DEFAULT, modConfig.general.botStatus
                            .replace("%players%", String.valueOf(PlatformServices.mc.getOnlinePlayerCount()))
                            .replace("%maxplayers%", String.valueOf(PlatformServices.mc.getMaxPlayerCount())));

                    event.getJDA().getPresence().setActivity(act);
                }
            } catch (Exception e) {
                if (modConfig.general.debugging) {
                    ConfigController.logger.error(e.getMessage());
                }
            }
        }, 0, modConfig.general.activityUpdateInterval, TimeUnit.SECONDS);

        threadPool.scheduleAtFixedRate(() -> {
            try {
                if (event.getJDA().getStatus() == JDA.Status.CONNECTED && (modConfig.general.channelTopic != null && !modConfig.general.channelTopic.isEmpty())) {
                    TextChannel channel = event.getJDA().getTextChannelById(modConfig.chatConfig.channelID);
                    if (channel != null) {
                        String topic = modConfig.general.channelTopic
                                .replace("%players%", String.valueOf(PlatformServices.mc.getOnlinePlayerCount()))
                                .replace("%maxplayers%", String.valueOf(PlatformServices.mc.getMaxPlayerCount()))
                                .replace("%uptime%", SystemUtils.secondsToTimestamp(PlatformServices.mc.getServerUptime()));
                        channel.getManager().setTopic(topic).queue();
                    }
                }
            } catch (Exception e) {
                if (modConfig.general.debugging) {
                    ConfigController.logger.error(e.getMessage());
                }
            }
        }, 11, 11, TimeUnit.MINUTES);
    }

    public void shutdown() {
        threadPool.shutdownNow();
    }

    public ScheduledExecutorService getThreadPool() {
        return threadPool;
    }
}
