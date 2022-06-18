package me.hypherionmc.sdlinklib.config.configobjects;

import me.hypherionmc.nightconfig.core.conversion.Path;
import me.hypherionmc.nightconfig.core.conversion.SpecComment;

/**
 * @author HypherionSA
 * @date 19/06/2022
 */
public class MessageChannelsConfig {

    @Path("statusInChat")
    @SpecComment("Should Server Starting/Started/Stopping/Stopped Messages be in chat. If false, it will appear in the log channel")
    public boolean stopStartInChat = true;

    @Path("joinLeaveInChat")
    @SpecComment("Should Join/Leave Messages be in chat. If false, it will appear in the log channel")
    public boolean joinLeaveInChat = true;

    @Path("advancementsInChat")
    @SpecComment("Should Advancement Messages be in chat. If false, it will appear in the log channel")
    public boolean advancementsInChat = true;

    @Path("deathInChat")
    @SpecComment("Should Death messages be in chat. If false, it will appear in the log channel")
    public boolean deathInChat = true;
}
