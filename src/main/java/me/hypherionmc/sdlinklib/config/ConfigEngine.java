package me.hypherionmc.sdlinklib.config;

import me.hypherionmc.nightconfig.core.Config;
import me.hypherionmc.nightconfig.core.conversion.ObjectConverter;
import me.hypherionmc.nightconfig.core.file.CommentedFileConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class ConfigEngine {

    private final File configPath;
    public static final Logger logger = LogManager.getLogger("Simple Discord Link");

    private ModConfig modConfig;

    public ConfigEngine(String configPath) {
        this.configPath = new File(configPath + "/simple-discord-bot.toml");
        initConfig();
    }

    private void initConfig() {
        if (!configPath.exists()) {
            ModConfig modConfig = new ModConfig();
            saveConfig(modConfig);
        }
        loadConfig();
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
