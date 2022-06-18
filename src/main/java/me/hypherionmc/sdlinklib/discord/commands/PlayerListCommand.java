package me.hypherionmc.sdlinklib.discord.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.hypherionmc.sdlinklib.services.PlatformServices;

import java.util.List;

public class PlayerListCommand extends Command {

    public PlayerListCommand() {
        this.name = "list";
        this.help = "List players on the server";
    }

    @Override
    protected void execute(CommandEvent event) {
        List<String> players = PlatformServices.mc.getOnlinePlayerNames();

        StringBuilder builder = new StringBuilder();
        builder.append("Players Online (").append(PlatformServices.mc.getOnlinePlayerCount()).append("/").append(PlatformServices.mc.getMaxPlayerCount()).append("):\r\n\r\n");

        for (String player : players) {
            builder.append(player).append("\r\n");
        }

        event.reply(builder.toString());

    }
}
