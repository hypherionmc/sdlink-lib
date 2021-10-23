package me.hypherionmc.sdlinklib.config.configobjects;

import me.hypherionmc.nightconfig.core.conversion.Path;
import me.hypherionmc.nightconfig.core.conversion.SpecComment;

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
    public int activityUpdateInterval = 30;

    @Path("botPrefix")
    @SpecComment("The prefix to use for bot commands. Example: ~players")
    public String botPrefix = "~";

    @Path("whitelisting")
    @SpecComment("Should the bot be allowed to whitelist/un-whitelist players")
    public boolean whitelisting = false;

    @Path("botStatus")
    @SpecComment("Do not add Playing. A status to display on the bot. You can use %players% and %maxplayers% to show the number of players on the server")
    public String botStatus = "Minecraft";

}
