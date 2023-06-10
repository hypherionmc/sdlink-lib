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

import com.hypherionmc.craterlib.core.config.ModuleConfig;
import com.hypherionmc.craterlib.core.config.annotations.NoConfigScreen;
import me.hypherionmc.moonconfig.core.CommentedConfig;
import me.hypherionmc.moonconfig.core.conversion.ObjectConverter;
import me.hypherionmc.moonconfig.core.conversion.Path;
import me.hypherionmc.moonconfig.core.conversion.SpecComment;
import me.hypherionmc.moonconfig.core.file.CommentedFileConfig;
import me.hypherionmc.sdlinklib.config.configobjects.*;
import me.hypherionmc.sdlinklib.discord.BotController;

import java.io.File;

/**
 * @author HypherionSA
 * The main config Structure.
 */
@NoConfigScreen
public class ModConfig extends ModuleConfig {

    // DO NOT REMOVE TRANSIENT HERE... OTHERWISE THE STUPID CONFIG LIBRARY
    // WILL TRY TO WRITE THESE TO THE CONFIG
    public transient static ModConfig INSTANCE;
    public transient static int configVer = 24;

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

    public ModConfig() {
        super("sdlink", "simple-discord-bot");
        registerAndSetup(this);
    }

    @Override
    public void migrateConfig(ModuleConfig conf) {
        CommentedFileConfig config = CommentedFileConfig.builder(getConfigPath()).build();
        CommentedFileConfig newConfig = CommentedFileConfig.builder(getConfigPath()).build();
        config.load();

        if (config.contains("general.configVersion") && config.getInt("general.configVersion") < 11) {
            getConfigPath().renameTo(new File(getConfigPath().getAbsolutePath().replace(".toml", ".old")));
            saveConfig(this);
            BotController.LOGGER.info("Your config file cannot be auto-upgraded. The old one has been backed up and a new one created. Please re-configure the mod");
            return;
        }

        if (!config.contains("general.configVersion") || config.getInt("general.configVersion") != configVer) {
            new ObjectConverter().toConfig(conf, newConfig);
            this.updateConfigValues(config, newConfig, newConfig, "");
            newConfig.save();
        }

        config.close();
        newConfig.close();
    }

    @Override
    public void configReloaded() {
        INSTANCE = loadConfig(this);
    }
}
