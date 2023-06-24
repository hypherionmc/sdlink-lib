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
package me.hypherionmc.sdlinklib.discord.messages;

import me.hypherionmc.sdlinklib.config.ModConfig;
import me.hypherionmc.sdlinklib.services.helpers.IMinecraftHelper;

public class MessageAuthor {

    public static final MessageAuthor SERVER = new MessageAuthor(ModConfig.INSTANCE.webhookConfig.serverName, ModConfig.INSTANCE.webhookConfig.serverAvatar, "server", true);

    private final String displayName;
    private final String username;
    private final String avatar;
    private final boolean isServer;

    private MessageAuthor(String displayName, String avatar, String username, boolean isServer) {
        this.displayName = displayName;
        this.username = username;
        this.avatar = avatar;
        this.isServer = isServer;
    }

    public static MessageAuthor of(String displayName, String uuid, String username, IMinecraftHelper minecraftHelper) {
        if (minecraftHelper.isOnlineMode()) {
            return new MessageAuthor(displayName, ModConfig.INSTANCE.chatConfig.playerAvatarType.getUrl().replace("{uuid}", uuid), username, false);
        } else {
            return new MessageAuthor(displayName, ModConfig.INSTANCE.chatConfig.playerAvatarType.getUrl().replace("{uuid}", username), username, false);
        }
    }

    public String getAvatar() {
        return avatar;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUsername() {
        return username;
    }

    public boolean isServer() {
        return isServer;
    }
}
