package me.hypherionmc.sdlinklib.config.configobjects;

import me.hypherionmc.moonconfig.core.conversion.Path;
import me.hypherionmc.moonconfig.core.conversion.SpecComment;

public class MessageConfig {

    @Path("serverStarting")
    @SpecComment("Server Starting Message")
    public String serverStarting = "Server is starting...";

    @Path("serverStarted")
    @SpecComment("Server Started Message")
    public String serverStarted = "Server has started. Enjoy!";

    @Path("serverStopping")
    @SpecComment("Server Stopping Message")
    public String serverStopping = "Server is stopping...";

    @Path("serverStopped")
    @SpecComment("Server Stopped Message")
    public String serverStopped = "Server has stopped...";

    @Path("playerJoined")
    @SpecComment("Player Joined Message. Use %player% to display the player name")
    public String playerJoined = "%player% has joined the server!";

    @Path("playerLeft")
    @SpecComment("Player Left Message. Use %player% to display the player name")
    public String playerLeft = "%player% has left the server!";

    @Path("achievements")
    @SpecComment("Achievement Messages. Available variables: %player%, %title%, %description%")
    public String achievements = "%player% has made the advancement [%title%]: %description%";

    @Path("chat")
    @SpecComment("Chat Messages. Available variables: %player%, %message%")
    public String chat = "%message%";

    @Path("inviteMessage")
    @SpecComment("The message to show when someone uses /discord command. You can use %inviteurl%")
    public String inviteMessage = "Hey, check out our discord server here -> %inviteurl%";

}
