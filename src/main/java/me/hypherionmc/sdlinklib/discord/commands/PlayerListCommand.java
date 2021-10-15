package me.hypherionmc.sdlinklib.discord.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.hypherionmc.sdlinklib.discord.utils.MinecraftEventHandler;

import java.util.List;

public class PlayerListCommand extends Command {

    private final MinecraftEventHandler eventHandler;

    public PlayerListCommand(MinecraftEventHandler eventHandler) {
        this.eventHandler = eventHandler;
        this.name = "list";
        this.help = "List players on the server";
    }

    @Override
    protected void execute(CommandEvent event) {
        List<String> players = eventHandler.getOnlinePlayers();

        StringBuilder builder = new StringBuilder();
        builder.append("Players Online (").append(eventHandler.getPlayerCount()).append("/").append(eventHandler.getMaxPlayerCount()).append("):\r\n\r\n");

        for (String player : players) {
            builder.append(player).append("\r\n");
        }

        event.reply(builder.toString());

    }
}
