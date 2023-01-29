package me.hypherionmc.sdlinklib.discord;

import club.minnced.discord.webhook.WebhookClient;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import me.hypherionmc.jqlite.DatabaseEngine;
import me.hypherionmc.sdlinklib.config.ConfigController;
import me.hypherionmc.sdlinklib.database.UserTable;
import me.hypherionmc.sdlinklib.database.WhitelistTable;
import me.hypherionmc.sdlinklib.services.helpers.IMinecraftHelper;
import me.hypherionmc.sdlinklib.utils.SDWebhookClient;
import me.hypherionmc.sdlinklib.utils.ThreadedEventManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static me.hypherionmc.sdlinklib.config.ConfigController.modConfig;

public final class BotController {

    // Required Variables
    private JDA _jda;
    public static Logger LOGGER;

    // Common Variables
    private CommandClient commandClient;
    private WebhookClient chatWebhookClient, eventWebhookClient;
    private String adminRole = "";

    // Database
    private final DatabaseEngine databaseEngine = new DatabaseEngine("sdlink-whitelist");
    private WhitelistTable whitelistTable = new WhitelistTable();
    private UserTable userTable = new UserTable();

    // Mod Specific Variables
    private final ConfigController configController;
    private final IMinecraftHelper minecraftHelper;
    private DiscordEventHandler discordEventHandler;

    // Thread Manager
    public static final ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

    // Invite URL for bot shown in server logs
    private final String DISCORD_INVITE = "https://discord.com/api/oauth2/authorize?client_id={bot_id}&permissions=2886028304&scope=bot%20applications.commands";

    public BotController(IMinecraftHelper minecraftHelper, Logger logger) {
        LOGGER = logger;

        // Initialize Config and set Minecraft Helper Class
        configController = new ConfigController("./config");
        this.minecraftHelper = minecraftHelper;

        // Register Database Tables
        databaseEngine.registerTable(whitelistTable);
        databaseEngine.registerTable(userTable);

        // Initialize Webhook Clients
        if (modConfig.webhookConfig.enabled) {
            if (!modConfig.webhookConfig.chatWebhook.isEmpty()) {
                chatWebhookClient = new SDWebhookClient(modConfig.webhookConfig.chatWebhook).build();
                LOGGER.info("Using Webhook for Chat Messages");
            }

            if (!modConfig.webhookConfig.eventsWebhook.isEmpty()) {
                eventWebhookClient = new SDWebhookClient(modConfig.webhookConfig.eventsWebhook).build();
                LOGGER.info("Using Webhook for Event Messages");
            }
        }
    }

    /**
     * Check if the Bot is ready to send and receive messages
     * @return true or false
     */
    public boolean isBotReady() {
        if (modConfig == null)
            return false;

        if (!modConfig.generalConfig.enabled)
            return false;

        if (_jda == null)
            return false;

        if (_jda.getStatus() == JDA.Status.SHUTTING_DOWN || _jda.getStatus() == JDA.Status.SHUTDOWN)
            return false;

        return _jda.getStatus() == JDA.Status.CONNECTED;
    }

    /**
     * Attempt to open a connection to discord and bring the bot online.
     * Must be called from mod, AFTER the server is initializing
     */
    public void initializeBot() {
        // Check if the Config is loaded and Bot Token is specified
        if (modConfig == null || modConfig.botConfig.botToken.isEmpty()) {
            LOGGER.error("Could not initialize bot. Could not load config or your Bot Token is missing. Bot will be disabled");
            return;
        }

        try {
            // Only attempt to connect if mod is enabled
            if (modConfig.generalConfig.enabled) {
                // Setup the Discord API
                _jda = JDABuilder.createLight(
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

                // Setup Commands
                CommandClientBuilder clientBuilder = new CommandClientBuilder();
                clientBuilder.setOwnerId("354707828298088459");
                clientBuilder.setPrefix(modConfig.botConfig.botPrefix);
                clientBuilder.setHelpWord("help");
                clientBuilder.useHelpBuilder(false);

                discordEventHandler = new DiscordEventHandler(this);
                CommandManager commandManager = new CommandManager(this);

                commandClient = clientBuilder.build();
                commandManager.register(commandClient);

                // Register Event Handlers
                _jda.addEventListener(commandClient, discordEventHandler);
                _jda.setAutoReconnect(true);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to connect to discord", e);
        }

        threadPool.schedule(this::checkBotSetup, 5, TimeUnit.SECONDS);
    }

    /**
     * INTERNAL
     * Check that the bot has all the required permissions and channels it needs
     */
    private void checkBotSetup() {
        StringBuilder builder = new StringBuilder();
        builder.append("\r\n").append("******************* Simple Discord Link Errors *******************").append("\r\n");
        AtomicInteger errCount = new AtomicInteger();

        if (isBotReady()) {
            LOGGER.info("Discord Invite Link for Bot: {}", DISCORD_INVITE.replace("{bot_id}", _jda.getSelfUser().getId()));

            if (_jda.getGuilds().isEmpty()) {
                errCount.incrementAndGet();
                builder.append(errCount.get()).append(") ").append("Bot does not appear to be in any servers. You need to invite the bot to your discord server before chat relays will work. Use link ").append(DISCORD_INVITE.replace("{bot_id}", _jda.getSelfUser().getId())).append(" to invite the bot.").append("\r\n");
            } else {
                if (_jda.getGuilds().size() > 1) {
                    errCount.incrementAndGet();
                    builder.append(errCount.get()).append(") ").append("Bot appears to be in multiple discord servers. This mod is only designed to work with a single discord server").append("\r\n");
                } else {
                    Guild guild = _jda.getGuilds().get(0);

                    if (guild != null) {
                        Member bot = guild.getMemberById(_jda.getSelfUser().getIdLong());
                        EnumSet<Permission> botPerms = bot.getPermissionsExplicit();

                        // Find staff roles, and add them to list
                        if (!modConfig.botConfig.staffRole.isEmpty()) {
                            List<Role> roles = guild.getRolesByName(modConfig.botConfig.staffRole, true);

                            if (!roles.isEmpty()) {
                                adminRole = modConfig.botConfig.staffRole;
                            } else {
                                errCount.incrementAndGet();
                                builder.append(errCount.get()).append(") ").append("Missing Staff Role. Role :").append(modConfig.botConfig.staffRole).append(" cannot be found in the server").append("\r\n");
                            }
                        }

                        if (!botPerms.contains(Permission.ADMINISTRATOR)) {
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

                                if (!chatPerms.contains(Permission.ADMINISTRATOR)) {
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
                        }

                        if (modConfig.channelConfig.eventsID != 0) {
                            GuildChannel eventChannel = guild.getGuildChannelById(modConfig.channelConfig.eventsID);

                            if (eventChannel == null) {
                                errCount.incrementAndGet();
                                builder.append(errCount.get()).append(") ").append("logChannelID does not point to a valid Discord Text Channel. Please double check this").append("\r\n");
                            } else {
                                EnumSet<Permission> eventPerms = bot.getPermissionsExplicit(eventChannel);

                                if (!eventPerms.contains(Permission.ADMINISTRATOR)) {
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
                        }

                        if (modConfig.channelConfig.consoleChannelID != 0) {
                            GuildChannel eventChannel = guild.getGuildChannelById(modConfig.channelConfig.consoleChannelID);

                            if (eventChannel == null) {
                                errCount.incrementAndGet();
                                builder.append(errCount.get()).append(") ").append("consoleChannelID does not point to a valid Discord Text Channel. Please double check this").append("\r\n");
                            } else {
                                EnumSet<Permission> eventPerms = bot.getPermissionsExplicit(eventChannel);

                                if (!eventPerms.contains(Permission.ADMINISTRATOR)) {
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
        }

        if (errCount.get() > 0) {
            builder.append("******************* Simple Discord Link Errors *******************").append("\r\n");
            LOGGER.info(builder.toString());
        }
    }

    /**
     * Check if whitelisting can be used, and send a message to the console
     */
    public void checkWhitelisting() {
        if (modConfig.generalConfig.whitelisting || modConfig.generalConfig.offlinewhitelist) {
            if (!minecraftHelper.isWhitelistingEnabled()) {
                LOGGER.error("Server Side Whitelist is disabled. Whitelist commands will not work!");
            } else {
                LOGGER.info("Whitelisting enabled and ready");
            }
        }
    }

    /**
     * Get the Discord Name of a Minecraft player, if a linked account exists
     * @param mcName The Minecraft Username of the player
     * @return The linked name of the player, or an error
     */
    public String getDiscordName(String mcName) {
        if (isBotReady()) {
            userTable = new UserTable();
            userTable.fetch("username = '" + mcName + "'");

            if (userTable.discordID != 0 && _jda.getUserById(userTable.discordID) != null) {
                User discordUser = _jda.getUserById(userTable.discordID);
                return mcName + " is linked to discord account: " + discordUser.getName() + "#" + discordUser.getDiscriminator();
            }
        }
        return "Could not find result for " + mcName;
    }

    /**
     * Check if a player is whitelisted by the bot
     * @param username The Minecraft Username of the Player
     * @param uuid The UUID of the Minecraft Player
     * @return True if whitelisted, false if not
     */
    public boolean isPlayerWhitelisted(String username, String uuid) {
        if (modConfig.generalConfig.whitelisting) {
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

    public ConfigController getConfigController() {
        return configController;
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

    WebhookClient getChatWebhookClient() {
        return chatWebhookClient;
    }

    WebhookClient getEventWebhookClient() {
        return eventWebhookClient;
    }

    public JDA get_jda() {
        return _jda;
    }

    public String getAdminRole() {
        return adminRole;
    }
}
