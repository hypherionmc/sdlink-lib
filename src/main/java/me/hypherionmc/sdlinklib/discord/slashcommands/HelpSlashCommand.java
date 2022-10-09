package me.hypherionmc.sdlinklib.discord.slashcommands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import me.hypherionmc.sdlinklib.discord.BotController;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.util.List;

/**
 * @author HypherionSA
 * @date 09/10/2022
 */
public class HelpSlashCommand extends SlashCommand {

    private final BotController engine;

    public HelpSlashCommand(BotController engine) {
        this.engine = engine;
        this.name = "help";
        this.help = "Bot commands and help";
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        List<SlashCommand> commands = engine.getCommandClient().getSlashCommands();

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Bot commands");
        builder.setColor(Color.BLUE);

        commands.forEach(cmd -> builder.addField(cmd.getName(), cmd.getHelp(), false));
        builder.setFooter("Requested by " + event.getMember().getEffectiveName());
        event.replyEmbeds(builder.build()).setEphemeral(true).queue();
    }
}
