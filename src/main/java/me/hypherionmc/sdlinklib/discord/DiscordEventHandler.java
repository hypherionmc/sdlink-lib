package me.hypherionmc.sdlinklib.discord;

import me.hypherionmc.sdlinklib.services.helpers.IMinecraftHelper;
import me.hypherionmc.sdlinklib.utils.SystemUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

import static me.hypherionmc.sdlinklib.config.ConfigController.modConfig;
import static me.hypherionmc.sdlinklib.discord.BotController.threadPool;


public class DiscordEventHandler extends ListenerAdapter {

    private final IMinecraftHelper minecraftHelper;

    public DiscordEventHandler(BotController controller) {
        this.minecraftHelper = controller.getMinecraftHelper();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getChannel().getIdLong() == modConfig.channelConfig.channelID) {
            if (
                    (modConfig.chatConfig.ignoreBots && !event.getAuthor().isBot()) &&
                            !event.isWebhookMessage() &&
                            event.getAuthor() != event.getJDA().getSelfUser()
            ) {
                minecraftHelper.discordMessageEvent(event.getAuthor().getName(), event.getMessage().getContentStripped());
            }
        }
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        BotController.LOGGER.info("Successfully connected to discord");

        threadPool.scheduleAtFixedRate(() -> {
            try {
                if (event.getJDA().getStatus() == JDA.Status.CONNECTED) {
                    Activity act = Activity.of(Activity.ActivityType.PLAYING, modConfig.botConfig.botStatus
                            .replace("%players%", String.valueOf(minecraftHelper.getOnlinePlayerCount()))
                            .replace("%maxplayers%", String.valueOf(minecraftHelper.getMaxPlayerCount())));

                    event.getJDA().getPresence().setActivity(act);
                }
            } catch (Exception e) {
                if (modConfig.generalConfig.debugging) {
                    BotController.LOGGER.info(e.getMessage());
                }
            }
        }, modConfig.botConfig.activityUpdateInterval, modConfig.botConfig.activityUpdateInterval, TimeUnit.SECONDS);

        threadPool.scheduleAtFixedRate(() -> {
            try {
                if (event.getJDA().getStatus() == JDA.Status.CONNECTED && (modConfig.botConfig.channelTopic != null && !modConfig.botConfig.channelTopic.isEmpty())) {
                    TextChannel channel = event.getJDA().getTextChannelById(modConfig.channelConfig.channelID);
                    if (channel != null) {
                        String topic = modConfig.botConfig.channelTopic
                                .replace("%players%", String.valueOf(minecraftHelper.getOnlinePlayerCount()))
                                .replace("%maxplayers%", String.valueOf(minecraftHelper.getMaxPlayerCount()))
                                .replace("%uptime%", SystemUtils.secondsToTimestamp(minecraftHelper.getServerUptime()));
                        channel.getManager().setTopic(topic).queue();
                    }
                }
            } catch (Exception e) {
                if (modConfig.generalConfig.debugging) {
                    BotController.LOGGER.info(e.getMessage());
                }
            }
        }, 6, 6, TimeUnit.MINUTES);
    }

    public void shutdown() {
        threadPool.shutdownNow();
    }
}
