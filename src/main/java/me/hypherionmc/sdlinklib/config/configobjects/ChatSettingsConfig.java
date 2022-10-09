package me.hypherionmc.sdlinklib.config.configobjects;

import me.hypherionmc.moonconfig.core.conversion.Path;
import me.hypherionmc.moonconfig.core.conversion.SpecComment;

import java.util.ArrayList;
import java.util.List;

public class ChatSettingsConfig {

    @Path("playerAvatarType")
    @SpecComment("The type of image to use as the player icon in messages. Valid entries are: AVATAR, HEAD, BODY, COMBO")
    public ImageType playerAvatarType = ImageType.COMBO;

    @Path("mcPrefix")
    @SpecComment("Prefix to add to Minecraft when a message is relayed from Discord. Supports MC formatting. Use %user% for the Discord Username")
    public String mcPrefix = "\u00A7e[Discord]\u00A7r %user%: ";

    @Path("ignoreBots")
    @SpecComment("Should messages from bots be relayed")
    public boolean ignoreBots = true;

    @Path("serverStarting")
    @SpecComment("Should SERVER STARTING messages be shown")
    public boolean serverStarting = true;

    @Path("serverStarted")
    @SpecComment("Should SERVER STARTED messages be shown")
    public boolean serverStarted = true;

    @Path("serverStopping")
    @SpecComment("Should SERVER STOPPING messages be shown")
    public boolean serverStopping = true;

    @Path("serverStopped")
    @SpecComment("Should SERVER STOPPED messages be shown")
    public boolean serverStopped = true;

    @Path("playerMessages")
    @SpecComment("Should the chat be relayed")
    public boolean playerMessages = true;

    @Path("joinAndLeaveMessages")
    @SpecComment("Should Join and Leave messages be posted")
    public boolean joinAndLeaveMessages = true;

    @Path("advancementMessages")
    @SpecComment("Should Advancement messages be posted")
    public boolean advancementMessages = true;

    @Path("deathMessages")
    @SpecComment("Should Death Announcements be posted")
    public boolean deathMessages = true;

    @Path("sendSayCommand")
    @SpecComment("Should Messages from the /say command be posted")
    public boolean sendSayCommand = true;

    @Path("broadcastCommands")
    @SpecComment("Should commands be posted to discord")
    public boolean broadcastCommands = true;

    @Path("ignoredCommands")
    @SpecComment("Commands that should not be broadcasted to discord")
    public List<String> ignoredCommands = new ArrayList<String>() {{ add("particle"); add("login"); }};

    @Path("sendTellRaw")
    @SpecComment("Should Tell Raw messages be posted")
    public boolean sendTellRaw = true;

}
