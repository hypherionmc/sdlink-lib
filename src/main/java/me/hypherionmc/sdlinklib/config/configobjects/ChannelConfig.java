package me.hypherionmc.sdlinklib.config.configobjects;

import me.hypherionmc.moonconfig.core.conversion.Path;
import me.hypherionmc.moonconfig.core.conversion.SpecComment;

/**
 * @author HypherionSA
 * @date 09/10/2022
 */
public class ChannelConfig {

    @Path("chatChannelID")
    @SpecComment("The ID of the channel to post in and relay messages from. This is still needed, even in webhook mode")
    public long channelID = 0;

    @Path("eventsChannelID")
    @SpecComment("If this ID is set, event messages will be posted in this channel instead of the chat channel")
    public long eventsID = 0;

    @Path("chatEmbeds")
    @SpecComment("Use EMBED style messages for chat channel messages")
    public boolean chatEmbeds = false;

    @Path("eventEmbeds")
    @SpecComment("Use EMBED style messages for event channel messages")
    public boolean eventEmbeds = false;
}
