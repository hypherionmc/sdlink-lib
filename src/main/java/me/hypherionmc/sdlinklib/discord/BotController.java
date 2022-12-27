package me.hypherionmc.sdlinklib.discord;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import me.hypherionmc.jqlite.DatabaseEngine;
import me.hypherionmc.sdlinklib.config.ConfigController;
import me.hypherionmc.sdlinklib.database.UserTable;
import me.hypherionmc.sdlinklib.database.WhitelistTable;
import me.hypherionmc.sdlinklib.discord.commands.*;
import me.hypherionmc.sdlinklib.services.helpers.IMinecraftHelper;
import me.hypherionmc.sdlinklib.utils.SDWebhookClient;
import me.hypherionmc.sdlinklib.utils.ThreadedEventManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import okhttp3.OkHttpClient;
import org.apache.logging.log4j.Logger;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static me.hypherionmc.sdlinklib.config.ConfigController.modConfig;

/**
 * @author HypherionSA
 * @date 30/07/2022
 */
public class BotController {

    // Common Variables
    private JDA jda;
    private CommandClient commandClient;
    private WebhookClient chatWebhookClient, eventWebhookClient;
    public static Logger LOGGER;

    // Database
    private final DatabaseEngine databaseEngine = new DatabaseEngine("sdlink-whitelist");
    private WhitelistTable whitelistTable = new WhitelistTable();
    private UserTable userTable = new UserTable();

    // Mod Specific
    private final ConfigController configController;
    private final IMinecraftHelper minecraftHelper;
    private DiscordEventHandler discordEventHandler;

    private final String DISCORD_INVITE = "https://discord.com/api/oauth2/authorize?client_id={bot_id}&permissions=2886028304&scope=bot%20applications.commands";

    // Thread Manager
    public static final ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

    public BotController(IMinecraftHelper minecraftHelper, Logger logger) {
        LOGGER = logger;

        // Initialize Config and set Minecraft helper class
        configController = new ConfigController("./config");
        this.minecraftHelper = minecraftHelper;

        // Register Database Tables
        databaseEngine.registerTable(whitelistTable);
        databaseEngine.registerTable(userTable);

        // Initialize Webhook Clients
        if (modConfig.webhookConfig.enabled) {
            if (!modConfig.webhookConfig.chatWebhook.isEmpty()) {
                LOGGER.info("Chat Channel Webhooks will be enabled");
                chatWebhookClient = new SDWebhookClient(modConfig.webhookConfig.chatWebhook).build();
            }

            if (!modConfig.webhookConfig.eventsWebhook.isEmpty()) {
                LOGGER.info("Events Channel Webhooks will be enabled");
                eventWebhookClient = new SDWebhookClient(modConfig.webhookConfig.eventsWebhook).build();
            }
        }
    }

    /**
     * Check if the bot is connected and ready to work
     * @return True or False
     */
    public boolean isBotReady() {
        return modConfig != null && modConfig.generalConfig.enabled && jda != null && jda.getStatus() == JDA.Status.CONNECTED && jda.getStatus() != JDA.Status.SHUTTING_DOWN;
    }

    public void initializeBot() {
        if (modConfig == null || modConfig.botConfig.botToken.isEmpty()) {
            LOGGER.info("Could not initialize bot. Could not load config or your Bot Token is missing. Bot will be disabled");
        } else {
            try {
                if (modConfig.generalConfig.enabled) {
                    // Setup the discord API
                    jda = JDABuilder.createLight(
                            modConfig.botConfig.botToken,
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.MESSAGE_CONTENT
                    )
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .setBulkDeleteSplittingEnabled(true)
                    .setEventManager(new ThreadedEventManager())
                    .build();

                    CommandClientBuilder clientBuilder = new CommandClientBuilder();
                    clientBuilder.setOwnerId("354707828298088459");
                    clientBuilder.setPrefix(modConfig.botConfig.botPrefix);
                    clientBuilder.setHelpWord("help");
                    clientBuilder.useHelpBuilder(false);

                    discordEventHandler = new DiscordEventHandler(this);
                    CommandManager commandManager = new CommandManager(this);

                    commandClient = clientBuilder.build();
                    commandManager.register(commandClient);

                    jda.addEventListener(commandClient, discordEventHandler);
                    jda.setAutoReconnect(true);
                }
            } catch (Exception e) {
                if (modConfig.generalConfig.debugging) {
                    e.printStackTrace();
                    LOGGER.info("Failed to connect to discord. Error: {}", e.getMessage());
                }
            }
        }
        threadPool.schedule(this::checkBotSetup, 5, TimeUnit.SECONDS);
    }

    private void checkBotSetup() {
        StringBuilder builder = new StringBuilder();
        builder.append("\r\n").append("******************* Simple Discord Link Errors *******************").append("\r\n");
        AtomicInteger errCount = new AtomicInteger();

        if (isBotReady()) {
            LOGGER.info("Discord Invite Link for Bot: {}", DISCORD_INVITE.replace("{bot_id}", jda.getSelfUser().getId()));
            if (jda.getGuilds().isEmpty()) {
                errCount.incrementAndGet();
                builder.append(errCount.get()).append(") ").append("Bot does not appear to be in any servers. You need to invite the bot to your discord server before chat relays will work. Use link ").append(DISCORD_INVITE.replace("{bot_id}", jda.getSelfUser().getId())).append(" to invite the bot.").append("\r\n");
            } else {
                if (jda.getGuilds().size() > 1) {
                    errCount.incrementAndGet();
                    builder.append(errCount.get()).append(") ").append("Bot appears to be in multiple discord servers. This mod is only designed to work with a single discord server").append("\r\n");
                } else {
                    Guild guild = jda.getGuilds().get(0);

                    if (guild != null) {
                        Member bot = guild.getMemberById(jda.getSelfUser().getIdLong());
                        EnumSet<Permission> botPerms = bot.getPermissionsExplicit();

                        if (botPerms.contains(Permission.ADMINISTRATOR))
                            return;

                        if (!botPerms.contains(Permission.NICKNAME_CHANGE)) {
                            errCount.incrementAndGet();
                            builder.append(errCount.get()).append(") ").append("Missing Bot Permission: Change Nickname").append("\r\n");
                        }
                        if (!botPerms.contains(Permission.NICKNAME_MANAGE)) {
                            errCount.incrementAndGet();
                            builder.append(errCount.get()).append(") ").append("Missing Bot Permission: Manage Nicknames").append("\r\n");
                        }
                        if (!botPerms.contains(Permission.MANAGE_WEBHOOKS)) {
                            errCount.incrementAndGet();
                            builder.append(errCount.get()).append(") ").append("Missing Bot Permission: Manage Webhooks").append("\r\n");
                        }
                        if (!botPerms.contains(Permission.MESSAGE_SEND)) {
                            errCount.incrementAndGet();
                            builder.append(errCount.get()).append(") ").append("Missing Bot Permission: Send Messages").append("\r\n");
                        }
                        if (!botPerms.contains(Permission.MESSAGE_EMBED_LINKS)) {
                            errCount.incrementAndGet();
                            builder.append(errCount.get()).append(") ").append("Missing Bot Permission: Embed Links").append("\r\n");
                        }
                        if (!botPerms.contains(Permission.MESSAGE_HISTORY)) {
                            errCount.incrementAndGet();
                            builder.append(errCount.get()).append(") ").append("Missing Bot Permission: Read Message History").append("\r\n");
                        }
                        if (!botPerms.contains(Permission.MESSAGE_EXT_EMOJI)) {
                            errCount.incrementAndGet();
                            builder.append(errCount.get()).append(") ").append("Missing Bot Permission: Use External Emojis").append("\r\n");
                        }

                        if (modConfig.channelConfig.channelID == 0) {
                            errCount.incrementAndGet();
                            builder.append(errCount.get()).append(") ").append("channelID is not set.... The bot requires this to know where to relay messages from").append("\r\n");
                        } else {
                            GuildChannel chatChannel = guild.getGuildChannelById(modConfig.channelConfig.channelID);

                            if (chatChannel == null) {
                                errCount.incrementAndGet();
                                builder.append(errCount.get()).append(") ").append("channelID does not point to a valid Discord Text Channel. Please double check this").append("\r\n");
                            } else {
                                EnumSet<Permission> chatPerms = bot.getPermissionsExplicit(chatChannel);

                                if (!chatPerms.contains(Permission.VIEW_CHANNEL)) {
                                    errCount.incrementAndGet();
                                    builder.append(errCount.get()).append(") ").append("Missing Chat Channel Permission: View Channel").append("\r\n");
                                }
                                if (!chatPerms.contains(Permission.MESSAGE_SEND)) {
                                    errCount.incrementAndGet();
                                    builder.append(errCount.get()).append(") ").append("Missing Chat Channel Permission: Send Messages").append("\r\n");
                                }
                                if (!chatPerms.contains(Permission.MESSAGE_EMBED_LINKS)) {
                                    errCount.incrementAndGet();
                                    builder.append(errCount.get()).append(") ").append("Missing Chat Channel Permission: Embed Links").append("\r\n");
                                }
                                if (modConfig.botConfig.doTopicUpdates && !chatPerms.contains(Permission.MANAGE_CHANNEL)) {
                                    errCount.incrementAndGet();
                                    builder.append(errCount.get()).append(") ").append("Missing Chat Channel Permission: Manage Channel. Topic updates will not work").append("\r\n");
                                }
                            }
                        }

                        if (modConfig.channelConfig.eventsID != 0) {
                            GuildChannel eventChannel = guild.getGuildChannelById(modConfig.channelConfig.eventsID);

                            if (eventChannel == null) {
                                errCount.incrementAndGet();
                                builder.append(errCount.get()).append(") ").append("logChannelID does not point to a valid Discord Text Channel. Please double check this").append("\r\n");
                            } else {
                                EnumSet<Permission> eventPerms = bot.getPermissionsExplicit(eventChannel);

                                if (!eventPerms.contains(Permission.VIEW_CHANNEL)) {
                                    errCount.incrementAndGet();
                                    builder.append(errCount.get()).append(") ").append("Missing Event Channel Permission: View Channel").append("\r\n");
                                }
                                if (!eventPerms.contains(Permission.MESSAGE_EMBED_LINKS)) {
                                    errCount.incrementAndGet();
                                    builder.append(errCount.get()).append(") ").append("Missing Event Channel Permission: Embed Links").append("\r\n");
                                }
                                if (!eventPerms.contains(Permission.MESSAGE_SEND)) {
                                    errCount.incrementAndGet();
                                    builder.append(errCount.get()).append(") ").append("Missing Event Channel Permission: Send Messages").append("\r\n");
                                }
                                if (modConfig.botConfig.doTopicUpdates && !eventPerms.contains(Permission.MANAGE_CHANNEL)) {
                                    errCount.incrementAndGet();
                                    builder.append(errCount.get()).append(") ").append("Missing Event Channel Permission: Manage Channel. Topic updates will not work").append("\r\n");
                                }
                            }
                        }

                        if (modConfig.channelConfig.consoleChannelID != 0) {
                            GuildChannel eventChannel = guild.getGuildChannelById(modConfig.channelConfig.consoleChannelID);

                            if (eventChannel == null) {
                                errCount.incrementAndGet();
                                builder.append(errCount.get()).append(") ").append("consoleChannelID does not point to a valid Discord Text Channel. Please double check this").append("\r\n");
                            } else {
                                EnumSet<Permission> eventPerms = bot.getPermissionsExplicit(eventChannel);

                                if (!eventPerms.contains(Permission.VIEW_CHANNEL)) {
                                    errCount.incrementAndGet();
                                    builder.append(errCount.get()).append(") ").append("Missing Console Channel Permission: View Channel").append("\r\n");
                                }
                                if (!eventPerms.contains(Permission.MESSAGE_SEND)) {
                                    errCount.incrementAndGet();
                                    builder.append(errCount.get()).append(") ").append("Missing Console Channel Permission: Send Messages").append("\r\n");
                                }
                            }
                        }
                    }
                }
            }
        }

        if (errCount.get() > 0) {
            builder.append("******************* Simple Discord Link Errors *******************").append("\r\n");
            LOGGER.info(builder.toString());
        }
    }

    public void checkWhitelisting() {
        if (modConfig.generalConfig.whitelisting) {
            if (!minecraftHelper.isWhitelistingEnabled()) {
                LOGGER.warn("Server-Side Whitelist is disabled. Whitelist commands will not work");
            }
        }
    }

    public void shutdownBot() {
        this.shutdownBot(true);
    }

    public void shutdownBot(boolean forced) {
        if (jda != null) {
            OkHttpClient client = jda.getHttpClient();
            client.connectionPool().evictAll();
            client.dispatcher().cancelAll();
            client.dispatcher().executorService().shutdownNow();
            jda.shutdownNow();
            jda.shutdown();
        }
        if (chatWebhookClient != null) {
            chatWebhookClient.close();
        }
        if (eventWebhookClient != null) {
            eventWebhookClient.close();
        }

        if (forced) {
            // Workaround for Bot thread hanging after server shutdown
            threadPool.schedule(() -> {
                if (discordEventHandler != null) {
                    discordEventHandler.shutdown();
                }
                System.exit(1);
            }, 10, TimeUnit.SECONDS);
        }
    }

    public void sendToDiscord(String message, String username, String uuid, boolean isChat) {
        sendToDiscord(message, username, uuid, "", isChat);
    }

    public void sendToDiscord(String message, String username, String uuid, String textureID, boolean isChat) {
        try {
            if (isBotReady()) {
                if (modConfig.webhookConfig.enabled) {
                    sendWebhookMessage(username, message, textureID.isEmpty() ? uuid : textureID, isChat);
                } else {
                    sendEmbedMessage(username, message, textureID.isEmpty() ? uuid : textureID, isChat);
                }
            }
        } catch (Exception e) {
            if (modConfig != null && modConfig.generalConfig.debugging) {
                LOGGER.info("Failed to send message: {}", e.getMessage());
            }
        }
    }

    private void sendWebhookMessage(String username, String message, String uuid, boolean isChat) {
        String avatarUrl = modConfig.webhookConfig.serverAvatar;
        message = message.replace("<@", "");

        if (!uuid.isEmpty() && !username.equalsIgnoreCase("server")) {
            avatarUrl = modConfig.chatConfig.playerAvatarType.getUrl().replace("{uuid}", uuid);
        }

        WebhookMessageBuilder builder = new WebhookMessageBuilder();

        String user = modConfig.webhookConfig.serverName;

        if (isChat && !modConfig.messageConfig.chat.contains("%player%")) {
            user = username;
        }

        if (username.equalsIgnoreCase("server")) {
            user = modConfig.webhookConfig.serverName;
        }

        builder.setUsername(user);
        builder.setAvatarUrl(avatarUrl);

        if ((isChat && modConfig.webhookConfig.chatEmbeds) || (!isChat && modConfig.webhookConfig.eventEmbeds)) {
            EmbedBuilder eb = getEmbed(isChat, username, message, avatarUrl, false);
            WebhookEmbed web = WebhookEmbedBuilder.fromJDA(eb.build()).build();
            builder.addEmbeds(web);
        } else {
            builder.setContent(message);
        }

        if (isChat) {
            if (chatWebhookClient != null) {
                chatWebhookClient.send(builder.build());
            }
        } else {
            if (eventWebhookClient != null) {
                eventWebhookClient.send(builder.build());
            } else {
                chatWebhookClient.send(builder.build());
            }
        }
    }

    private void sendEmbedMessage(String username, String message, String uuid, boolean isChat) {
        String avatarUrl = modConfig.webhookConfig.serverAvatar;
        message = message.replace("<@", "");

        if (!uuid.isEmpty() && !username.equalsIgnoreCase("server")) {
            avatarUrl = modConfig.chatConfig.playerAvatarType.getUrl().replace("{uuid}", uuid);
        }

        EmbedBuilder builder = getEmbed(isChat, username, message, avatarUrl, true);

        TextChannel channel = jda.getTextChannelById(isChat ? modConfig.channelConfig.channelID : (modConfig.channelConfig.eventsID != 0 ? modConfig.channelConfig.eventsID : modConfig.channelConfig.channelID));
        if (channel != null) {
            if (isChat && modConfig.channelConfig.chatEmbeds) {
                channel.sendMessageEmbeds(builder.build()).complete();
            } else if (!isChat && modConfig.channelConfig.eventEmbeds) {
                channel.sendMessageEmbeds(builder.build()).complete();
            } else {
                if (username.equalsIgnoreCase("server")) {
                    username = modConfig.webhookConfig.serverName;
                }
                channel.sendMessage(isChat ? username + ": " + message : message).complete();
            }
        }
    }

    private EmbedBuilder getEmbed(boolean isChat, String username, String message, String avatarUrl, boolean withAuthor) {
        String user = modConfig.webhookConfig.serverName;

        if (isChat && !modConfig.messageConfig.chat.contains("%player%")) {
            user = username;
        }

        if (username.equalsIgnoreCase("server")) {
            user = modConfig.webhookConfig.serverName;
        }

        EmbedBuilder builder = new EmbedBuilder();
        if (withAuthor) {
            builder.setAuthor(
                    user,
                    null,
                    avatarUrl.isEmpty() ? null : avatarUrl);
        }
        builder.setDescription(message);
        return builder;
    }

    public String getDiscordName(String mcName) {
        if (isBotReady()) {
            userTable = new UserTable();
            userTable.fetch("username = '" + mcName + "'");

            if (userTable.discordID != 0 && jda.getUserById(userTable.discordID) != null) {
                User discordUser = jda.getUserById(userTable.discordID);
                return mcName + " is linked to discord account: " + discordUser.getName() + "#" + discordUser.getDiscriminator();
            }
        }
        return "Could not find result for " + mcName;
    }

    public boolean isPlayerWhitelisted(String username, String uuid) {
        if (modConfig.generalConfig.whitelisting) {
            whitelistTable = new WhitelistTable();
            List<WhitelistTable> tableList = whitelistTable.fetchAll("username = '" + username + "' AND uuid = '" + uuid + "'");
            return !tableList.isEmpty();
        } else {
            return true;
        }
    }

    public void sendConsoleMessage(String username, String message) {
        if (isBotReady() && modConfig.messageConfig.sendConsoleMessages) {
            TextChannel channel = jda.getTextChannelById(modConfig.channelConfig.consoleChannelID);
            if (channel != null) {
                channel.sendMessage(message).queue();
            }
        }
    }

    public CommandClient getCommandClient() {
        return commandClient;
    }

    public DatabaseEngine getDatabaseEngine() {
        return databaseEngine;
    }

    public DiscordEventHandler getDiscordEventHandler() {
        return discordEventHandler;
    }

    public IMinecraftHelper getMinecraftHelper() {
        return minecraftHelper;
    }
}
