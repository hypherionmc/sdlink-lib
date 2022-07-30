package me.hypherionmc.sdlinklib.discord.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.hypherionmc.sdlinklib.discord.BotController;
import me.hypherionmc.sdlinklib.services.helpers.IMinecraftHelper;

import java.util.List;

public class PlayerListCommand extends Command {

    private final IMinecraftHelper minecraftHelper;

    public PlayerListCommand(BotController controller) {
        this.name = "list";
        this.help = "List players on the server";
        this.minecraftHelper = controller.getMinecraftHelper();
    }

    @Override
    protected void execute(CommandEvent event) {
        List<String> players = minecraftHelper.getOnlinePlayerNames();

        StringBuilder builder = new StringBuilder();
        builder.append("Players Online (").append(minecraftHelper.getOnlinePlayerCount()).append("/").append(minecraftHelper.getMaxPlayerCount()).append("):\r\n\r\n");

        for (String player : players) {
            builder.append(player).append("\r\n");
        }

        event.reply(builder.toString());
    }
}
