package me.hypherionmc.sdlinklib.discord;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import me.hypherionmc.jqlite.DatabaseEngine;
import me.hypherionmc.sdlinklib.config.ConfigEngine;
import me.hypherionmc.sdlinklib.config.ModConfig;
import me.hypherionmc.sdlinklib.database.WhitelistTable;
import me.hypherionmc.sdlinklib.discord.commands.PlayerListCommand;
import me.hypherionmc.sdlinklib.discord.commands.WhitelistCommand;
import me.hypherionmc.sdlinklib.discord.utils.DiscordEventHandler;
import me.hypherionmc.sdlinklib.discord.utils.MinecraftEventHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.InterfacedEventManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import javax.annotation.Nonnull;
import java.util.concurrent.*;

public class BotEngine {

    private final ModConfig modConfig;
    private JDA jda;
    private CommandClient commandClient;
    private WebhookClient webhookClient;
    private final MinecraftEventHandler minecraftEventHandler;
    private final ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(1);

    private final DatabaseEngine databaseEngine = new DatabaseEngine("sdlink-whitelist");
    private WhitelistTable whitelistTable = new WhitelistTable();

    public BotEngine(ModConfig modConfig, MinecraftEventHandler minecraftEventHandler) {
        this.modConfig = modConfig;
        this.minecraftEventHandler = minecraftEventHandler;

        databaseEngine.registerTable(whitelistTable);

        if (modConfig.webhookConfig.enabled && !modConfig.webhookConfig.webhookurl.isEmpty()) {
            ConfigEngine.logger.info("[SDLink] Webhooks will be enabled");
            WebhookClientBuilder builder = new WebhookClientBuilder(modConfig.webhookConfig.webhookurl);
            builder.setThreadFactory((job) -> {
                Thread thread = new Thread(job);
                thread.setName("Webhook Thread");
                thread.setDaemon(true);
                return thread;
            });
            builder.setWait(true);
            webhookClient = builder.build();
        }
    }

    public boolean isBotReady() {
        return modConfig != null && modConfig.general.enabled && jda != null && jda.getStatus() == JDA.Status.CONNECTED;
    }

    public void initBot() {

        if (modConfig == null || modConfig.general.botToken.isEmpty()) {
            ConfigEngine.logger.error("[SDLink] Could not load Config or your BotToken is empty. Bot will not start");
        } else {

            try {
                if (modConfig.general.enabled) {
                    jda = JDABuilder.createLight(modConfig.general.botToken, GatewayIntent.GUILD_MESSAGES)
                            .setMemberCachePolicy(MemberCachePolicy.NONE)
                            .setChunkingFilter(ChunkingFilter.ALL)
                            .setBulkDeleteSplittingEnabled(false)
                            .setEventManager(new ThreadedEventManager())
                            .build();

                    CommandClientBuilder clientBuilder = new CommandClientBuilder();
                    clientBuilder.setOwnerId("");
                    clientBuilder.setPrefix(modConfig.general.botPrefix);

                    //clientBuilder.setActivity(Activity.playing(""));
                    commandClient = clientBuilder.build();
                    commandClient.addCommand(new PlayerListCommand(minecraftEventHandler));
                    commandClient.addCommand(new WhitelistCommand(whitelistTable, minecraftEventHandler, modConfig));
                    jda.addEventListener(commandClient, new DiscordEventHandler(minecraftEventHandler, modConfig));
                    jda.setAutoReconnect(true);

                    if (jda.getStatus() == JDA.Status.CONNECTED) {
                        ConfigEngine.logger.info("Successfully Connected to Discord");

                        threadPool.scheduleAtFixedRate(() -> {
                            Activity act = Activity.of(Activity.ActivityType.DEFAULT, modConfig.general.botStatus
                                    .replace("%players%", String.valueOf(minecraftEventHandler.getPlayerCount()))
                                    .replace("%maxplayers%", String.valueOf(minecraftEventHandler.getMaxPlayerCount())));

                            jda.getPresence().setActivity(act);

                        }, 0, modConfig.general.activityUpdateInterval, TimeUnit.SECONDS);
                    }
                }
            } catch (Exception e) {
                if (modConfig.general.debugging) {
                    e.printStackTrace();
                    ConfigEngine.logger.error("Failed to connect to discord. Error: " + e.getMessage());
                }
            }
        }

    }

    private static class ThreadedEventManager extends InterfacedEventManager {
        private final ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);

        @Override
        public void handle(@Nonnull GenericEvent event) {
            threadPool.submit(() -> super.handle(event));
        }
    }

    public void initWhitelisting() {
        if (jda != null && modConfig.general.whitelisting) {
            if (!minecraftEventHandler.whiteListingEnabled()) {
                ConfigEngine.logger.warn("Serverside Whitelisting is disabled. Whitelist command will not work");
            }
        }
    }

    public void shutdownBot() {
        if (jda != null && jda.getStatus() == JDA.Status.CONNECTED) {
            jda.shutdownNow();
        }
    }

    public void sendToDiscord(String message, String username, String uuid, boolean isChat) {
        if (jda != null && jda.getStatus() == JDA.Status.CONNECTED) {
            if (modConfig.webhookConfig.enabled && !modConfig.webhookConfig.webhookurl.isEmpty() && webhookClient != null) {
                sendWebhookMessage(username, message, uuid, isChat);
            } else {
                TextChannel channel;

                if (isChat) {
                    channel = jda.getTextChannelById(modConfig.chatConfig.channelID);
                } else {
                    channel = jda.getTextChannelById(modConfig.chatConfig.logChannelID != 0 ? modConfig.chatConfig.logChannelID : modConfig.chatConfig.channelID);
                }

                if (channel != null) {
                    channel.sendMessage(isChat ? "**" + username + "**: " + message : "*" + message + "*").complete();
                } else {
                    if (modConfig.general.debugging) {
                        ConfigEngine.logger.error("Could not find channel with ID " + modConfig.chatConfig.channelID);
                    }
                }
            }
        }
    }

    private void sendWebhookMessage(String username, String message, String uuid, boolean isChat) {
        String avatarUrl = modConfig.webhookConfig.serverAvatar;

        if (!uuid.isEmpty() && !username.equalsIgnoreCase("server")) {
            avatarUrl = "https://crafatar.com/avatars/" + uuid;
        }

        WebhookMessageBuilder builder = new WebhookMessageBuilder();
        builder.setUsername(isChat ? username : "Minecraft Server");
        builder.setAvatarUrl(avatarUrl);
        builder.setContent(isChat ? message : "*" + message + "*");
        webhookClient.send(builder.build());
    }
}
