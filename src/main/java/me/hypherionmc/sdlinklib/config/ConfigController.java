package me.hypherionmc.sdlinklib.config;

import me.hypherionmc.moonconfig.core.CommentedConfig;
import me.hypherionmc.moonconfig.core.Config;
import me.hypherionmc.moonconfig.core.conversion.ObjectConverter;
import me.hypherionmc.moonconfig.core.file.CommentedFileConfig;
import me.hypherionmc.moonconfig.core.file.FileWatcher;
import me.hypherionmc.sdlinklib.discord.BotController;

import java.io.File;

public class ConfigController {

    private final File configPath;
    public static int configVer = 16;

    public static ModConfig modConfig;

    public ConfigController(String configPath) {
        File path = new File(configPath);
        if (!path.exists()) {
            path.mkdirs();
        }
        this.configPath = new File(configPath + "/simple-discord-bot.toml");
        initConfig();
    }

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

    private void loadConfig() {
        Config.setInsertionOrderPreserved(true);

        ObjectConverter converter = new ObjectConverter();
        CommentedFileConfig config = CommentedFileConfig.builder(configPath).build();
        config.load();
        modConfig = converter.toObject(config, ModConfig::new);
        config.close();
    }

    private void saveConfig(Object conf) {
        Config.setInsertionOrderPreserved(true);

        ObjectConverter converter = new ObjectConverter();
        CommentedFileConfig config = CommentedFileConfig.builder(configPath).build();

        converter.toConfig(conf, config);
        config.save();
        config.close();
    }
}
