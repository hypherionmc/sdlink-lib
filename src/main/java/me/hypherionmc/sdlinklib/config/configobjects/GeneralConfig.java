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
