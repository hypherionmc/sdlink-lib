package me.hypherionmc.sdlinklib.config.configobjects;

import me.hypherionmc.moonconfig.core.conversion.Path;
import me.hypherionmc.moonconfig.core.conversion.SpecComment;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author HypherionSA
 */
public class LinkedCommandsConfig {

    @Path("enabled")
    @SpecComment("Should linked commands be enabled")
    public boolean enabled = false;

    @Path("commands")
    @SpecComment("Commands to be linked")
    public List<Command> commands = new ArrayList<>();

    public static class Command {
        @Path("mcCommand")
        @SpecComment("The Minecraft Command. Use %args% to pass everything after the discordCommand to Minecraft")
        public String mcCommand;

        @Path("discordCommand")
        @SpecComment("The command slug in discord. To be used as /mc slug or ~mc slug")
        public String discordCommand;

        @Path("discordRole")
        @SpecComment("If set, only users with the assigned role will be able to use the command")
        public String discordRole;
    }

}
