package me.hypherionmc.sdlinklib.discord;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import me.hypherionmc.jqlite.DatabaseEngine;
import me.hypherionmc.sdlinklib.config.ConfigController;
import me.hypherionmc.sdlinklib.config.ModConfig;
import me.hypherionmc.sdlinklib.database.UserTable;
import me.hypherionmc.sdlinklib.database.WhitelistTable;
import me.hypherionmc.sdlinklib.discord.commands.*;
import me.hypherionmc.sdlinklib.services.PlatformServices;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.InterfacedEventManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import okhttp3.OkHttpClient;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BotEngine {

    private final ModConfig modConfig;
    private JDA jda;
    private CommandClient commandClient;
    private WebhookClient webhookClient, webhookClient2;
    private DiscordEventHandler discordEventHandler;
    private final DatabaseEngine databaseEngine = new DatabaseEngine("sdlink-whitelist");
    private WhitelistTable whitelistTable = new WhitelistTable();
    private UserTable userTable = new UserTable();

    public static final ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());


    public BotEngine(ModConfig modConfig) {
        this.modConfig = modConfig;
        databaseEngine.registerTable(whitelistTable, userTable);

        if (modConfig.webhookConfig.enabled) {
            if (!modConfig.webhookConfig.webhookurl.isEmpty()) {
                ConfigController.logger.info("[SDLink] Chat Channel Webhooks will be enabled");
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

            if (!modConfig.webhookConfig.webhookurlLogs.isEmpty()) {
                ConfigController.logger.info("[SDLink] Events Channel Webhooks will be enabled");
                WebhookClientBuilder builder = new WebhookClientBuilder(modConfig.webhookConfig.webhookurlLogs);
                builder.setThreadFactory((job) -> {
                    Thread thread = new Thread(job);
                    thread.setName("Webhook Thread 2");
                    thread.setDaemon(true);
                    return thread;
                });
                builder.setWait(true);
                webhookClient2 = builder.build();
            }
        }
    }

    public boolean isBotReady() {
        return modConfig != null && modConfig.general.enabled && jda != null && jda.getStatus() == JDA.Status.CONNECTED;
    }

    public void initBot() {
        if (modConfig == null || modConfig.general.botToken.isEmpty()) {
            ConfigController.logger.error("[SDLink] Could not load Config or your BotToken is empty. Bot will not start");
        } else {

            try {
                if (modConfig.general.enabled) {
                    jda = JDABuilder.createLight(
                                modConfig.general.botToken,
                                GatewayIntent.GUILD_MESSAGES,
                                GatewayIntent.GUILD_MEMBERS
                            )
                            .setMemberCachePolicy(MemberCachePolicy.NONE)
                            .setChunkingFilter(ChunkingFilter.ALL)
                            .setBulkDeleteSplittingEnabled(false)
                            .setEventManager(new ThreadedEventManager())
                            .build();

                    CommandClientBuilder clientBuilder = new CommandClientBuilder();
                    clientBuilder.setOwnerId("354707828298088459");
                    clientBuilder.setPrefix(modConfig.general.botPrefix);
                    clientBuilder.setHelpWord("help");
                    clientBuilder.useHelpBuilder(false);

                    discordEventHandler = new DiscordEventHandler(modConfig);

                    commandClient = clientBuilder.build();
                    commandClient.addCommand(new PlayerListCommand());
                    commandClient.addCommand(new WhitelistCommand(whitelistTable, modConfig));
                    commandClient.addCommand(new ServerStatusCommand(modConfig));
                    commandClient.addCommand(new LinkCommand(userTable, modConfig));
                    commandClient.addCommand(new UnLinkCommand(userTable, modConfig));
                    commandClient.addCommand(new LinkedCommand(userTable));
                    commandClient.addCommand(new HelpCommand(this));
                    jda.addEventListener(commandClient, discordEventHandler);
                    jda.setAutoReconnect(true);
                }
            } catch (Exception e) {
                if (modConfig.general.debugging) {
                    e.printStackTrace();
                    ConfigController.logger.error("Failed to connect to discord. Error: " + e.getMessage());
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
            if (!PlatformServices.mc.isWhitelistingEnabled()) {
                ConfigController.logger.warn("Serverside Whitelisting is disabled. Whitelist command will not work");
            }
        }
    }

    public void shutdownBot() {
        if (jda != null) {
            OkHttpClient client = jda.getHttpClient();
            client.connectionPool().evictAll();
            client.dispatcher().cancelAll();
            client.dispatcher().executorService().shutdownNow();
            jda.shutdownNow();
        }
        if (webhookClient != null) {
            webhookClient.close();
        }
        if (webhookClient2 != null) {
            webhookClient2.close();
        }
        if (discordEventHandler != null) {
            discordEventHandler.shutdown();
        }

        // Dirty Workaround for JDA not shutting down properly
        threadPool.schedule(() -> {
            System.exit(1);
        }, 10, TimeUnit.SECONDS);
    }

    public void sendToDiscord(String message, String username, String uuid, boolean isChat) {
        if (isBotReady()) {
            if (isChat) {
                if (modConfig.webhookConfig.enabled && webhookClient != null) {
                    sendWebhookMessage(username, message, uuid, true);
                } else {
                    sendEmbedMessage(message, username, uuid, true);
                }
            } else {
                if (modConfig.webhookConfig.enabled && webhookClient2 != null) {
                    sendWebhookMessage(username, message, uuid, false);
                } else {
                    sendEmbedMessage(message, username, uuid, false);
                }
            }
        }
    }

    private void sendWebhookMessage(String username, String message, String uuid, boolean isChat) {
        String avatarUrl = modConfig.webhookConfig.serverAvatar;
        message = message.replace("<@", "");

        if (!uuid.isEmpty() && !username.equalsIgnoreCase("server")) {
            avatarUrl = "https://crafatar.com/avatars/" + uuid;
        }

        WebhookMessageBuilder builder = new WebhookMessageBuilder();
        builder.setUsername(isChat && !username.equalsIgnoreCase("server") ? username : modConfig.webhookConfig.serverName);
        builder.setAvatarUrl(avatarUrl);
        builder.setContent(isChat ? message : "*" + message + "*");

        if (isChat) {
            webhookClient.send(builder.build());
        } else {
            webhookClient2.send(builder.build());
        }
    }

    private void sendEmbedMessage(String username, String message, String uuid, boolean isChat) {
        String avatarUrl = modConfig.webhookConfig.serverAvatar;
        message = message.replace("<@", "");

        if (!uuid.isEmpty() && !username.equalsIgnoreCase("server")) {
            avatarUrl = "https://crafatar.com/avatars/" + uuid;
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor(isChat && !username.equalsIgnoreCase("server") ? username : modConfig.webhookConfig.serverName, null, avatarUrl);
        builder.setDescription(isChat ? message : "*" + message + "*");

        TextChannel channel = jda.getTextChannelById(isChat ? modConfig.chatConfig.channelID : (modConfig.chatConfig.logChannelID != 0 ? modConfig.chatConfig.logChannelID : modConfig.chatConfig.channelID));
        if (channel != null) {
            if (modConfig.chatConfig.useEmbeds) {
                channel.sendMessageEmbeds(builder.build()).queue();
            } else {
                channel.sendMessage(isChat ? "**" + username + "**: " + message : "*" + message + "*").queue();
            }
        }
    }

    public String getDiscordName(String mcName) {
        if (jda != null && jda.getStatus() == JDA.Status.CONNECTED) {
            userTable = new UserTable();
            userTable.fetch("username = '" + mcName + "'");

            if (userTable.discordID != 0 && jda.getUserById(userTable.discordID) != null) {
                User discordUser = jda.getUserById(userTable.discordID);
                return mcName + " is linked to discord account: " + discordUser.getName() + "#" + discordUser.getDiscriminator();
            }
        }
        return "Could not find result for " + mcName;
    }

    public CommandClient getCommandClient() {
        return this.commandClient;
    }

    public boolean isPlayerWhitelisted(String username, String uuid) {
        if (modConfig.general.whitelisting) {
            whitelistTable = new WhitelistTable();
            List<WhitelistTable> tableList = whitelistTable.fetchAll("username = '" + username + "' AND uuid = '" + uuid + "'");
            return !tableList.isEmpty();
        } else {
            return true;
        }
    }
}
