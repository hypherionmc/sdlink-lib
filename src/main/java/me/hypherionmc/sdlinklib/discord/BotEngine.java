package me.hypherionmc.sdlinklib.discord;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import me.hypherionmc.jqlite.DatabaseEngine;
import me.hypherionmc.sdlinklib.config.ConfigEngine;
import me.hypherionmc.sdlinklib.config.ModConfig;
import me.hypherionmc.sdlinklib.database.UserTable;
import me.hypherionmc.sdlinklib.database.WhitelistTable;
import me.hypherionmc.sdlinklib.discord.commands.*;
import me.hypherionmc.sdlinklib.discord.utils.DiscordEventHandler;
import me.hypherionmc.sdlinklib.discord.utils.MinecraftEventHandler;
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
import java.util.concurrent.TimeUnit;

public class BotEngine {

    private final ModConfig modConfig;
    private JDA jda;
    private CommandClient commandClient;
    private final MinecraftEventHandler minecraftEventHandler;
    private DiscordEventHandler discordEventHandler;

    private final DatabaseEngine databaseEngine = new DatabaseEngine("sdlink-whitelist");
    private WhitelistTable whitelistTable = new WhitelistTable();
    private UserTable userTable = new UserTable();

    public BotEngine(ModConfig modConfig, MinecraftEventHandler minecraftEventHandler) {
        this.modConfig = modConfig;
        this.minecraftEventHandler = minecraftEventHandler;

        databaseEngine.registerTable(whitelistTable);
        databaseEngine.registerTable(userTable);
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
                    jda = JDABuilder.createLight(modConfig.general.botToken, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS)
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

                    discordEventHandler = new DiscordEventHandler(minecraftEventHandler, modConfig);

                    commandClient = clientBuilder.build();
                    commandClient.addCommand(new PlayerListCommand(minecraftEventHandler));
                    commandClient.addCommand(new WhitelistCommand(whitelistTable, minecraftEventHandler, modConfig));
                    commandClient.addCommand(new ServerStatusCommand(modConfig, minecraftEventHandler));
                    //commandClient.addCommand(new StopServerCommand(minecraftEventHandler));
                    commandClient.addCommand(new LinkCommand(userTable, modConfig));
                    commandClient.addCommand(new UnLinkCommand(userTable, modConfig));
                    commandClient.addCommand(new LinkedCommand(userTable));
                    commandClient.addCommand(new HelpCommand(this));
                    jda.addEventListener(commandClient, discordEventHandler);
                    jda.setAutoReconnect(true);

                    if (modConfig.webhookConfig.enabled && !modConfig.webhookConfig.webhookurl.isEmpty()) {
                        ConfigEngine.logger.info("[SDLink] Webhooks will be enabled");
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
        if (jda != null) {
            OkHttpClient client = jda.getHttpClient();
            client.connectionPool().evictAll();
            client.dispatcher().cancelAll();
            client.dispatcher().executorService().shutdownNow();
            jda.shutdownNow();
        }
        if (discordEventHandler != null) {
            // Dirty Workaround for JDA not shutting down properly
            discordEventHandler.getThreadPool().schedule(() -> {
                discordEventHandler.shutdown();
                System.exit(1);
            }, 10, TimeUnit.SECONDS);
        }
    }

    public void sendToDiscord(String message, String username, String uuid, boolean isChat) {
        if (jda != null && jda.getStatus() == JDA.Status.CONNECTED) {
            if (modConfig.webhookConfig.enabled && !modConfig.webhookConfig.webhookurl.isEmpty()) {
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
        message = message.replace("<@", "");

        if (!uuid.isEmpty() && !username.equalsIgnoreCase("server")) {
            avatarUrl = "https://crafatar.com/avatars/" + uuid;
        }

        WebhookMessageClient webhookMessageClient = new WebhookMessageClient(modConfig.webhookConfig.webhookurl);
        webhookMessageClient.setUsername(isChat ? username : modConfig.webhookConfig.serverName);
        webhookMessageClient.setAvatarUrl(avatarUrl);
        webhookMessageClient.setContent(isChat ? message : "*" + message + "*");
        try {
            webhookMessageClient.execute();
        } catch (Exception e) {
            if (modConfig.general.debugging) {
                e.printStackTrace();
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
