package me.hypherionmc.sdlinklib.config.configobjects;

import me.hypherionmc.moonconfig.core.conversion.Path;
import me.hypherionmc.moonconfig.core.conversion.SpecComment;
import me.hypherionmc.sdlinklib.config.ConfigController;

public class GeneralConfig {

    @Path("enabled")
    @SpecComment("Should the bot be enabled or not")
    public boolean enabled = true;

    @Path("debugging")
    @SpecComment("Should debug logging be enabled? WARNING: THIS CAN SPAM YOUR LOG!")
    public boolean debugging = false;

    @Path("whitelisting")
    @SpecComment("Should the bot be allowed to whitelist/un-whitelist players. Whitelisting needs to be enabled on your server as well")
    public boolean whitelisting = false;

    @Path("offlinewhitelist")
    @SpecComment("Should the bot be allowed to whitelist/un-whitelist players in OFFLINE mode. Whitelisting needs to be enabled on your server as well")
    public boolean offlinewhitelist = false;

    @Path("linkedWhitelist")
    @SpecComment("Automatically link Minecraft and Discord Accounts when a user is whitelisted")
    public boolean linkedWhitelist = false;

    @Path("onlyAdminsWhitelist")
    @SpecComment("Should only admins be allowed to whitelist players")
    public boolean adminWhitelistOnly = false;

    @Path("inviteCommandEnabled")
    @SpecComment("Should the /discord command be enabled in game")
    public boolean inviteCommandEnabled = false;

    @Path("inviteLink")
    @SpecComment("Discord Invite Link used by the in-game invite command")
    public String inviteLink = "";

    @Path("configVersion")
    @SpecComment("Internal version control. DO NOT TOUCH!")
    public int configVersion = ConfigController.configVer;

}
