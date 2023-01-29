/*
 * This file is part of sdlink-lib, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2021 - 2023 HypherionSA and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.hypherionmc.sdlinklib.config.configobjects;

import me.hypherionmc.moonconfig.core.conversion.Path;
import me.hypherionmc.moonconfig.core.conversion.SpecComment;

public class MessageConfig {

    @Path("formatting")
    @SpecComment("Convert Discord to MC, and MC to Discord Formatting")
    public boolean formatting = true;

    @Path("sendConsoleMessages")
    @SpecComment("Should console messages be sent to the Console Channel")
    public boolean sendConsoleMessages = true;

    @Path("serverStarting")
    @SpecComment("Server Starting Message")
    public String serverStarting = "*Server is starting...*";

    @Path("serverStarted")
    @SpecComment("Server Started Message")
    public String serverStarted = "*Server has started. Enjoy!*";

    @Path("serverStopping")
    @SpecComment("Server Stopping Message")
    public String serverStopping = "*Server is stopping...*";

    @Path("serverStopped")
    @SpecComment("Server Stopped Message")
    public String serverStopped = "*Server has stopped...*";

    @Path("playerJoined")
    @SpecComment("Player Joined Message. Use %player% to display the player name")
    public String playerJoined = "*%player% has joined the server!*";

    @Path("playerLeft")
    @SpecComment("Player Left Message. Use %player% to display the player name")
    public String playerLeft = "*%player% has left the server!*";

    @Path("achievements")
    @SpecComment("Achievement Messages. Available variables: %player%, %title%, %description%")
    public String achievements = "*%player% has made the advancement [%title%]: %description%*";

    @Path("chat")
    @SpecComment("Chat Messages. Available variables: %player%, %message%")
    public String chat = "%message%";

    @Path("commands")
    @SpecComment("Command Messages. Available variables: %player%, %command%")
    public String commands = "%player% **executed command: %command%**";

    @Path("relayFullCommands")
    @SpecComment("Should the entire command executed be relayed to discord, or only the name of the command")
    public boolean relayFullCommands = false;

    @Path("inviteMessage")
    @SpecComment("The message to show when someone uses /discord command. You can use %inviteurl%")
    public String inviteMessage = "Hey, check out our discord server here -> %inviteurl%";

}
