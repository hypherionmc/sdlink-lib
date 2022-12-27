package me.hypherionmc.sdlinklib.discord.slashcommands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import me.hypherionmc.sdlinklib.config.configobjects.LinkedCommandsConfig;
import me.hypherionmc.sdlinklib.discord.BotController;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.Optional;

import static me.hypherionmc.sdlinklib.config.ConfigController.modConfig;

public class MCSlashCommand extends SlashCommand {

    private final BotController engine;

    public MCSlashCommand(BotController engine) {
        this.engine = engine;
        this.name = "mc";
        this.help = "Execute Minecraft commands from Discord";

        this.options = new ArrayList<OptionData>() {{
            add(new OptionData(OptionType.STRING, "slug", "The discordCommand slug defined in the config").setRequired(true));
            add(new OptionData(OptionType.STRING, "args", "Additional arguments to pass to the %args% variable").setRequired(false));
        }};
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        if (event.getChannel().getIdLong() != modConfig.channelConfig.consoleChannelID) {
            event.reply("You can only execute MC commands in the Console Channel").setEphemeral(true).queue();
            return;
        }

        if (modConfig.linkedCommands.enabled) {
            String slug = event.getOption("slug") != null ? event.getOption("slug").getAsString() : "";
            String args = event.getOption("args") != null ? event.getOption("args").getAsString() : "";
            Optional<LinkedCommandsConfig.Command> linkedCommand = modConfig.linkedCommands.commands.stream().filter(c -> c.discordCommand.equalsIgnoreCase(slug)).findFirst();

            linkedCommand.ifPresent(command -> {
                if (!command.discordRole.isEmpty()) {
                    Optional<Role> role = event.getMember().getRoles()
                            .stream().filter(r -> r.getName().equalsIgnoreCase(command.discordRole))
                            .findFirst();

                    if (role.isPresent()) {
                        executeCommand(command, args);
                    } else {
                        event.reply("You need the " + command.discordRole + " role to perform this action").setEphemeral(true).queue();
                    }
                } else {
                    executeCommand(command, args);
                }
            });

            if (!linkedCommand.isPresent()) {
                event.reply("Cannot find linked command " + slug).setEphemeral(true).queue();
            }

        } else {
            event.reply("Linked commands are not enabled!").setEphemeral(true).queue();
        }
    }

    private void executeCommand(LinkedCommandsConfig.Command mcCommand, String args) {
        engine.getMinecraftHelper().executeMcCommand(mcCommand.mcCommand, args);
    }

}
