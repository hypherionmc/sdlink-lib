package me.hypherionmc.sdlinklib.config;

import me.hypherionmc.nightconfig.core.CommentedConfig;
import me.hypherionmc.nightconfig.core.Config;
import me.hypherionmc.nightconfig.core.conversion.ObjectConverter;
import me.hypherionmc.nightconfig.core.file.CommentedFileConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class ConfigController {

    private final File configPath;
    public static final Logger logger = LogManager.getLogger("Simple Discord Link");
    public static int configVer = 7;

    private ModConfig modConfig;

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
    }

    private void configUpgrade() {
        CommentedFileConfig oldConfig = CommentedFileConfig.builder(configPath).build();
        CommentedFileConfig newConfig = CommentedFileConfig.builder(configPath).build();

        newConfig.load();
        newConfig.clear();
        oldConfig.load();

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

    private void loadConfig() {
        Config.setInsertionOrderPreserved(true);

        ObjectConverter converter = new ObjectConverter();
        CommentedFileConfig config = CommentedFileConfig.builder(configPath).build();
        config.load();

        modConfig = converter.toObject(config, ModConfig::new);
    }

    private void saveConfig(Object conf) {
        Config.setInsertionOrderPreserved(true);

        ObjectConverter converter = new ObjectConverter();
        CommentedFileConfig config = CommentedFileConfig.builder(configPath).build();

        converter.toConfig(conf, config);
        config.save();
    }

    public ModConfig getModConfig() {
        return modConfig;
    }

}
