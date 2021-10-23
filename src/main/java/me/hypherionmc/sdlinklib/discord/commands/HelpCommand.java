package me.hypherionmc.sdlinklib.discord.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.hypherionmc.sdlinklib.discord.BotEngine;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.util.List;

public class HelpCommand extends Command {

    private final BotEngine engine;

    public HelpCommand(BotEngine engine) {
        this.engine = engine;
        this.name = "help";
        this.help = "Bot commands and help";
    }

    @Override
    protected void execute(CommandEvent event) {
        List<Command> commands = engine.getCommandClient().getCommands();

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Bot commands");
        builder.setColor(Color.BLUE);

        commands.forEach(cmd -> {
            builder.addField(cmd.getName(), cmd.getHelp(), false);
        });
        builder.setFooter("Requested by " + event.getMember().getEffectiveName());
        event.reply(builder.build());
    }

}
