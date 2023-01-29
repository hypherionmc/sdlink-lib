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

import me.hypherionmc.moonconfig.core.CommentedConfig;
import me.hypherionmc.moonconfig.core.Config;
import me.hypherionmc.moonconfig.core.conversion.ObjectConverter;
import me.hypherionmc.moonconfig.core.file.CommentedFileConfig;
import me.hypherionmc.moonconfig.core.file.FileWatcher;
import me.hypherionmc.sdlinklib.discord.BotController;

import java.io.File;

/**
 * @author HypherionSA
 * Main Config class for Loading, Saving and Upgrading configs
 */
public class ConfigController {

    // Private Variables used internally
    private final File configPath;
    public static int configVer = 20;

    // Instance of loaded config
    public static ModConfig modConfig;

    public ConfigController(String configPath) {
        File path = new File(configPath);
        if (!path.exists()) {
            path.mkdirs();
        }
        this.configPath = new File(configPath + "/simple-discord-bot.toml");
        initConfig();
    }

    /**
     * Setup the Config file.
     * This will create, upgrade or load the config file based on the state of the file
     */
    private void initConfig() {
        Config.setInsertionOrderPreserved(true);
        if (!configPath.exists() || configPath.length() < 10) {
            ModConfig modConfig = new ModConfig();
            saveConfig(modConfig);
        } else {
            configUpgrade();
        }
        loadConfig();
        FileWatcher watcher = new FileWatcher();

        try {
            watcher.addWatch(configPath, this::loadConfig);
        } catch (Exception e) {
            if (modConfig.generalConfig.debugging) {
                BotController.LOGGER.info("Failed to register config watcher: {}", e.getMessage());
            }
        }
    }

    /**
     * Upgrades the config between structure changes.
     */
    private void configUpgrade() {
        CommentedFileConfig oldConfig = CommentedFileConfig.builder(configPath).build();
        CommentedFileConfig newConfig = CommentedFileConfig.builder(configPath).build();

        newConfig.load();
        newConfig.clear();
        oldConfig.load();

        if (oldConfig.contains("general.configVersion") && oldConfig.getInt("general.configVersion") < 11) {
            configPath.renameTo(new File(configPath.getAbsolutePath().replace(".toml", ".old")));
            saveConfig(new ModConfig());
            BotController.LOGGER.info("Your config file cannot be auto-upgraded. The old one has been backed up and a new one created. Please re-configure the mod");
        } else {
            if (!oldConfig.contains("general.configVersion") || oldConfig.getInt("general.configVersion") != configVer) {

                ObjectConverter objectConverter = new ObjectConverter();
                objectConverter.toConfig(new ModConfig(), newConfig);

                oldConfig.valueMap().forEach((key, value) -> {
                    if (value instanceof CommentedConfig) {
                        CommentedConfig commentedConfig = (CommentedConfig) value;
                        commentedConfig.valueMap().forEach((subKey, subValue) -> {
                            newConfig.set(key + "." + subKey, subValue);
                        });
                    } else {
                        newConfig.set(key, value);
                    }
                });

                configPath.renameTo(new File(configPath.getAbsolutePath().replace(".toml", ".bak")));
                newConfig.set("general.configVersion", configVer);
                newConfig.save();
                newConfig.close();
                oldConfig.close();
            }
        }

    }

    /**
     * Load existing config file from the disk
     */
    private void loadConfig() {
        Config.setInsertionOrderPreserved(true);

        ObjectConverter converter = new ObjectConverter();
        CommentedFileConfig config = CommentedFileConfig.builder(configPath).build();
        config.load();
        modConfig = converter.toObject(config, ModConfig::new);
        config.close();
    }

    /**
     * Save the config to the drive when required. Mostly used after upgrading and
     * when creating a new file
     * @param conf The config that will be serialized into the file
     */
    private void saveConfig(Object conf) {
        Config.setInsertionOrderPreserved(true);

        ObjectConverter converter = new ObjectConverter();
        CommentedFileConfig config = CommentedFileConfig.builder(configPath).build();

        converter.toConfig(conf, config);
        config.save();
        config.close();
    }
}
