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
package me.hypherionmc.sdlinklib.services.helpers;

import me.hypherionmc.sdlinklib.utils.MinecraftPlayer;

import java.util.List;

/**
 * @author HypherionSA
 * @date 18/06/2022
 */
public interface IMinecraftHelper {

    public void discordMessageEvent(String username, String message);

    public boolean isWhitelistingEnabled();

    public boolean isPlayerWhitelisted(MinecraftPlayer player);

    public boolean whitelistPlayer(MinecraftPlayer player);

    public boolean unWhitelistPlayer(MinecraftPlayer player);

    public List<String> getWhitelistedPlayers();

    public int getOnlinePlayerCount();

    public int getMaxPlayerCount();

    public List<String> getOnlinePlayerNames();

    public long getServerUptime();

    public String getServerVersion();

    public void executeMcCommand(String command, String args);

    public boolean isOnlineMode();

}
