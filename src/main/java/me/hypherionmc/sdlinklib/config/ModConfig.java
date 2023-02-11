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
package me.hypherionmc.sdlinklib.config;

import me.hypherionmc.moonconfig.core.conversion.Path;
import me.hypherionmc.moonconfig.core.conversion.SpecComment;
import me.hypherionmc.sdlinklib.config.configobjects.*;

/**
 * @author HypherionSA
 * The main config Structure.
 */
public class ModConfig {

    @Path("general")
    @SpecComment("General Mod Config")
    public GeneralConfig generalConfig = new GeneralConfig();

    @Path("botConfig")
    @SpecComment("Config specific to the discord bot")
    public BotSettingsConfig botConfig = new BotSettingsConfig();

    @Path("channels")
    @SpecComment("Config relating to the discord channels to use with the mod")
    public ChannelConfig channelConfig = new ChannelConfig();

    @Path("webhooks")
    @SpecComment("Webhook Config")
    public WebhookConfig webhookConfig = new WebhookConfig();

    @Path("chat")
    @SpecComment("Chat Config")
    public ChatSettingsConfig chatConfig = new ChatSettingsConfig();

    @Path("messages")
    @SpecComment("Change the contents of certain event messages")
    public MessageConfig messageConfig = new MessageConfig();

    @Path("messageDestinations")
    @SpecComment("Change in which channel messages appear")
    public MessageChannelsConfig messageDestinations = new MessageChannelsConfig();

    @Path("botCommands")
    @SpecComment("Enable or Disable certain bot commands")
    public BotCommandsConfig botCommands = new BotCommandsConfig();

    @Path("linkedCommands")
    @SpecComment("Execute Minecraft commands in Discord")
    public LinkedCommandsConfig linkedCommands = new LinkedCommandsConfig();

}
