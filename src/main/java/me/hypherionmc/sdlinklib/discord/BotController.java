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
import me.hypherionmc.sdlinklib.utils.ThreadedEventManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
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

    private final String DISCORD_INVITE = "https://discord.com/api/oauth2/authorize?client_id={bot_id}&permissions=738543616&scope=bot";

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
            if (!modConfig.webhookConfig.webhookurl.isEmpty()) {
                LOGGER.info("Chat Channel Webhooks will be enabled");
                WebhookClientBuilder builder = new WebhookClientBuilder(modConfig.webhookConfig.webhookurl);
                builder.setThreadFactory((job) -> {
                    Thread thread = new Thread(job);
                    thread.setName("Webhook Thread");
                    thread.setDaemon(true);
                    return thread;
                });
                builder.setWait(true);
                chatWebhookClient = builder.build();
            }

            if (!modConfig.webhookConfig.webhookurlLogs.isEmpty()) {
                LOGGER.info("Events Channel Webhooks will be enabled");
                WebhookClientBuilder builder = new WebhookClientBuilder(modConfig.webhookConfig.webhookurlLogs);
                builder.setThreadFactory((job) -> {
                    Thread thread = new Thread(job);
                    thread.setName("Webhook Thread 2");
                    thread.setDaemon(true);
                    return thread;
                });
                builder.setWait(true);
                eventWebhookClient = builder.build();
            }
        }
    }

    /**
     * Check if the bot is connected and ready to work
     * @return True or False
     */
    public boolean isBotReady() {
        return modConfig != null && modConfig.general.enabled && jda != null && jda.getStatus() == JDA.Status.CONNECTED;
    }

    public void initializeBot() {
        if (modConfig == null || modConfig.general.botToken.isEmpty()) {
            LOGGER.error("Could not initialize bot. Could not load config or your Bot Token is missing. Bot will be disabled");
        } else {
            try {
                if (modConfig.general.enabled) {
                    // Setup the discord API
                    jda = JDABuilder.createLight(
                            modConfig.general.botToken,
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.GUILD_MESSAGES
                    )
                    .setMemberCachePolicy(MemberCachePolicy.ONLINE)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .setBulkDeleteSplittingEnabled(true)
                    .setEventManager(new ThreadedEventManager())
                    .build();

                    CommandClientBuilder clientBuilder = new CommandClientBuilder();
                    clientBuilder.setOwnerId("354707828298088459");
                    clientBuilder.setPrefix(modConfig.general.botPrefix);
                    clientBuilder.setHelpWord("help");
                    clientBuilder.useHelpBuilder(false);

                    discordEventHandler = new DiscordEventHandler(this);

                    commandClient = clientBuilder.build();
                    commandClient.addCommand(new PlayerListCommand(this));
                    commandClient.addCommand(new WhitelistCommand(this));
                    commandClient.addCommand(new ServerStatusCommand(this));
                    commandClient.addCommand(new LinkCommand(this));
                    commandClient.addCommand(new UnLinkCommand(this));
                    commandClient.addCommand(new LinkedCommand());
                    commandClient.addCommand(new HelpCommand(this));

                    jda.addEventListener(commandClient, discordEventHandler);
                    jda.setAutoReconnect(true);
                }
            } catch (Exception e) {
                if (modConfig.general.debugging) {
                    e.printStackTrace();
                    LOGGER.error("Failed to connect to discord. Error: {}", e.getMessage());
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
                        if (!botPerms.contains(Permission.MESSAGE_WRITE)) {
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

                        if (modConfig.chatConfig.channelID == 0) {
                            errCount.incrementAndGet();
                            builder.append(errCount.get()).append(") ").append("channelID is not set.... The bot requires this to know where to relay messages from").append("\r\n");
                        } else {
                            GuildChannel chatChannel = guild.getGuildChannelById(modConfig.chatConfig.channelID);

                            if (chatChannel == null) {
                                errCount.incrementAndGet();
                                builder.append(errCount.get()).append(") ").append("channelID does not point to a valid Discord Text Channel. Please double check this").append("\r\n");
                            } else {
                                EnumSet<Permission> chatPerms = bot.getPermissionsExplicit(chatChannel);

                                if (!chatPerms.contains(Permission.MESSAGE_READ)) {
                                    errCount.incrementAndGet();
                                    builder.append(errCount.get()).append(") ").append("Missing Chat Channel Permission: View Channel").append("\r\n");
                                }
                                if (!chatPerms.contains(Permission.MESSAGE_WRITE)) {
                                    errCount.incrementAndGet();
                                    builder.append(errCount.get()).append(") ").append("Missing Chat Channel Permission: Send Messages").append("\r\n");
                                }
                                if (!chatPerms.contains(Permission.MESSAGE_EMBED_LINKS)) {
                                    errCount.incrementAndGet();
                                    builder.append(errCount.get()).append(") ").append("Missing Chat Channel Permission: Embed Links").append("\r\n");
                                }
                                if (!chatPerms.contains(Permission.MANAGE_CHANNEL)) {
                                    errCount.incrementAndGet();
                                    builder.append(errCount.get()).append(") ").append("Missing Chat Channel Permission: Manage Channel. Topic updates will not work").append("\r\n");
                                }
                            }
                        }

                        if (modConfig.chatConfig.logChannelID != 0) {
                            GuildChannel eventChannel = guild.getGuildChannelById(modConfig.chatConfig.logChannelID);

                            if (eventChannel == null) {
                                errCount.incrementAndGet();
                                builder.append(errCount.get()).append(") ").append("logChannelID does not point to a valid Discord Text Channel. Please double check this").append("\r\n");
                            } else {
                                EnumSet<Permission> eventPerms = bot.getPermissionsExplicit(eventChannel);

                                if (!eventPerms.contains(Permission.MESSAGE_READ)) {
                                    errCount.incrementAndGet();
                                    builder.append(errCount.get()).append(") ").append("Missing Event Channel Permission: View Channel").append("\r\n");
                                }
                                if (!eventPerms.contains(Permission.MESSAGE_EMBED_LINKS)) {
                                    errCount.incrementAndGet();
                                    builder.append(errCount.get()).append(") ").append("Missing Event Channel Permission: Embed Links").append("\r\n");
                                }
                                if (!eventPerms.contains(Permission.MESSAGE_WRITE)) {
                                    errCount.incrementAndGet();
                                    builder.append(errCount.get()).append(") ").append("Missing Event Channel Permission: Send Messages").append("\r\n");
                                }
                                if (!eventPerms.contains(Permission.MANAGE_CHANNEL)) {
                                    errCount.incrementAndGet();
                                    builder.append(errCount.get()).append(") ").append("Missing Event Channel Permission: Manage Channel. Topic updates will not work").append("\r\n");
                                }
                            }
                        }
                    }
                }
            }
        }

        if (errCount.get() > 0) {
            builder.append("******************* Simple Discord Link Errors *******************").append("\r\n");
            LOGGER.error(builder.toString());
        }
    }

    public void checkWhitelisting() {
        if (modConfig.general.whitelisting) {
            if (!minecraftHelper.isWhitelistingEnabled()) {
                LOGGER.warn("Server-side Whitelist is disabled. Whitelist commands will not work");
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
            jda.shutdown();
        }
        if (chatWebhookClient != null) {
            chatWebhookClient.close();
        }
        if (eventWebhookClient != null) {
            eventWebhookClient.close();
        }

        // Workaround for Bot thread hanging after server shutdown
        threadPool.schedule(() -> {
            if (discordEventHandler != null) {
                discordEventHandler.shutdown();
            }
            System.exit(1);
        }, 10, TimeUnit.SECONDS);
    }

    public void sendToDiscord(String message, String username, String uuid, boolean isChat) {
        try {
            if (isBotReady()) {
                if (modConfig.webhookConfig.enabled) {
                    sendWebhookMessage(username, message, uuid, isChat);
                } else {
                    sendEmbedMessage(username, message, uuid, isChat);
                }
            }
        } catch (Exception e) {
            if (modConfig != null && modConfig.general.debugging) {
                LOGGER.error("Failed to send message: {}", e.getMessage());
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

        builder.setUsername(user);
        builder.setAvatarUrl(avatarUrl);

        if ((isChat && modConfig.chatConfig.useEmbeds) || (!isChat && modConfig.chatConfig.useEmbedsLog)) {
            EmbedBuilder eb = getEmbed(isChat, username, message, avatarUrl, false);
            WebhookEmbed web = WebhookEmbedBuilder.fromJDA(eb.build()).build();
            builder.addEmbeds(web);
        } else {
            builder.setContent(isChat ? message : "*" + message + "*");
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

        TextChannel channel = jda.getTextChannelById(isChat ? modConfig.chatConfig.channelID : (modConfig.chatConfig.logChannelID != 0 ? modConfig.chatConfig.logChannelID : modConfig.chatConfig.channelID));
        if (channel != null) {
            if (modConfig.chatConfig.useEmbeds) {
                channel.sendMessageEmbeds(builder.build()).complete();
            } else {
                channel.sendMessage(isChat ? "**" + username + "**: " + message : "*" + message + "*").complete();
            }
        }
    }

    private EmbedBuilder getEmbed(boolean isChat, String username, String message, String avatarUrl, boolean withAuthor) {
        String user = modConfig.webhookConfig.serverName;

        if (isChat && !modConfig.messageConfig.chat.contains("%player%")) {
            user = username;
        }

        EmbedBuilder builder = new EmbedBuilder();
        if (withAuthor) {
            builder.setAuthor(
                    user,
                    null,
                    avatarUrl.isEmpty() ? null : avatarUrl);
        }
        builder.setDescription(isChat ? message : "*" + message + "*");
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
        if (modConfig.general.whitelisting) {
            whitelistTable = new WhitelistTable();
            List<WhitelistTable> tableList = whitelistTable.fetchAll("username = '" + username + "' AND uuid = '" + uuid + "'");
            return !tableList.isEmpty();
        } else {
            return true;
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
