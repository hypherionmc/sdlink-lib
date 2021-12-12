package me.hypherionmc.sdlinklib.config.configobjects;

import me.hypherionmc.nightconfig.core.conversion.Path;
import me.hypherionmc.nightconfig.core.conversion.SpecComment;
import me.hypherionmc.sdlinklib.config.ConfigEngine;

public class GeneralConfig {

    @Path("botToken")
    @SpecComment("The Token of the Bot to use. KEEP THIS PRIVATE")
    public String botToken = "";

    @Path("enabled")
    @SpecComment("Should the bot be enabled or not")
    public boolean enabled = true;

    @Path("debugging")
    @SpecComment("Should debug logging be enabled? WARNING: THIS CAN SPAM YOUR LOG!")
    public boolean debugging = false;

    @Path("activityUpdateInterval")
    @SpecComment("How quickly the bot status should update")
    public int activityUpdateInterval = 30;

    @Path("botPrefix")
    @SpecComment("The prefix to use for bot commands. Example: ~players")
    public String botPrefix = "~";

    @Path("whitelisting")
    @SpecComment("Should the bot be allowed to whitelist/un-whitelist players")
    public boolean whitelisting = false;

    @Path("onlyAdminsWhitelist")
    @SpecComment("Should only admins be allowed to whitelist players")
    public boolean adminWhitelistOnly = false;

    @Path("botStatus")
    @SpecComment("Do not add Playing. A status to display on the bot. You can use %players% and %maxplayers% to show the number of players on the server")
    public String botStatus = "Minecraft";

    @Path("channelTopic")
    @SpecComment("A topic for the Chat Relay channel. You can use %player%, %maxplayers%, %uptime%, %tps% or just leave it empty.")
    public String channelTopic = "Playing Minecraft with %players%/%maxplayers% people | Uptime: %uptime%";

    @Path("inviteLink")
    @SpecComment("Discord Invite Link")
    public String inviteLink = "";

    @Path("configVersion")
    @SpecComment("Internal version control. DO NOT TOUCH!")
    public int configVersion = ConfigEngine.configVer;

}
