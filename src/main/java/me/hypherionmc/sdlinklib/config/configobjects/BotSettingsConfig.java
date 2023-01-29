package me.hypherionmc.sdlinklib.config.configobjects;

import me.hypherionmc.moonconfig.core.conversion.Path;
import me.hypherionmc.moonconfig.core.conversion.SpecComment;

/**
 * @author HypherionSA
 * @date 09/10/2022
 */
public class BotSettingsConfig {

    @Path("botToken")
    @SpecComment("The Token of the Bot to use. KEEP THIS PRIVATE. See https://readme.firstdarkdev.xyz/simple-discord-link/initial-setup/ to find this")
    public String botToken = "";

    @Path("activityUpdateInterval")
    @SpecComment("How quickly the bot status should update")
    public int activityUpdateInterval = 30;

    @Path("staffRole")
    @SpecComment("If defined, only this role can use Staff Functions. Otherwise, it defaults back to admin/kick perms")
    public String staffRole;

    @Path("slashCommands")
    @SpecComment("Should the bot use / commands. NOTE: THIS MAY TAKE UP TO 24 HOURS TO SHOW UP IN YOUR DISCORD")
    public boolean slashCommands = false;

    @Path("botPrefix")
    @SpecComment("The prefix to use for bot commands. Example: ~players. THIS HAS NO EFFECT WHEN USING SLASH COMMANDS")
    public String botPrefix = "~";

    @Path("botStatus")
    @SpecComment("Do not add Playing. A status to display on the bot. You can use %players% and %maxplayers% to show the number of players on the server")
    public String botStatus = "Minecraft";

    @Path("doTopicUpdates")
    @SpecComment("Should the bot update the topic of your chat channel automatically")
    public boolean doTopicUpdates = true;

    @Path("channelTopic")
    @SpecComment("A topic for the Chat Relay channel. You can use %player%, %maxplayers%, %uptime% or just leave it empty.")
    public String channelTopic = "Playing Minecraft with %players%/%maxplayers% people | Uptime: %uptime%";
}
