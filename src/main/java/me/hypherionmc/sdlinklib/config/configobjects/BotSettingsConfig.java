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
import net.dv8tion.jda.api.entities.Activity.ActivityType;

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
    @SpecComment("If a role ID (or name) is defined here, only members with this role can use Staff Functions. Otherwise, it defaults back to admin/kick perms")
    public String staffRole = "";

    @Path("slashCommands")
    @SpecComment("Should the bot use / commands. NOTE: THIS MAY TAKE UP TO 24 HOURS TO SHOW UP IN YOUR DISCORD")
    public boolean slashCommands = false;

    @Path("botPrefix")
    @SpecComment("The prefix to use for bot commands. Example: ~players. THIS HAS NO EFFECT WHEN USING SLASH COMMANDS")
    public String botPrefix = "~";

    @Path("botStatus")
    @SpecComment("Do not add Playing. A status to display on the bot. You can use %players% and %maxplayers% to show the number of players on the server")
    public String botStatus = "Minecraft";

    @Path("botStatusType")
    @SpecComment("The type of the status displayed on the bot. Valid entries are: PLAYING, STREAMING, WATCHING, LISTENING")
    public ActivityType botStatusType = ActivityType.PLAYING;

    @Path("botStatusStreamingURL")
    @SpecComment("The URL that will be used when the \"botStatusType\" is set to \"STREAMING\", required to display as \"streaming\".")
    public String botStatusStreamingURL = "https://twitch.tv/KuryKat";

    @Path("doTopicUpdates")
    @SpecComment("Should the bot update the topic of your chat channel automatically")
    public boolean doTopicUpdates = true;

    @Path("channelTopic")
    @SpecComment("A topic for the Chat Relay channel. You can use %player%, %maxplayers%, %uptime% or just leave it empty.")
    public String channelTopic = "Playing Minecraft with %players%/%maxplayers% people | Uptime: %uptime%";
}
