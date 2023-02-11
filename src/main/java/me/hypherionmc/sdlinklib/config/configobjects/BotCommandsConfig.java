package me.hypherionmc.sdlinklib.config.configobjects;

import me.hypherionmc.moonconfig.core.conversion.Path;
import me.hypherionmc.moonconfig.core.conversion.SpecComment;

public class BotCommandsConfig {

    @Path("accountLinking")
    @SpecComment("Allow members to link their MC and Discord accounts")
    public boolean accountLinking = true;

    @Path("allowPlayerList")
    @SpecComment("Enable/Disable the Player List command")
    public boolean allowPlayerList = true;

    @Path("allowServerStatus")
    @SpecComment("Enable/Disable the Server Status command")
    public boolean allowServerStatus = true;

}
