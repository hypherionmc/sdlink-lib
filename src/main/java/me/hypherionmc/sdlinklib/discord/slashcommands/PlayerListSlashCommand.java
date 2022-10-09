package me.hypherionmc.sdlinklib.discord.slashcommands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import me.hypherionmc.sdlinklib.discord.BotController;
import me.hypherionmc.sdlinklib.services.helpers.IMinecraftHelper;

import java.util.List;

/**
 * @author HypherionSA
 * @date 09/10/2022
 */
public class PlayerListSlashCommand extends SlashCommand {

    private final IMinecraftHelper minecraftHelper;

    public PlayerListSlashCommand(BotController controller) {
        this.name = "list";
        this.help = "List players on the server";
        this.minecraftHelper = controller.getMinecraftHelper();
        this.guildOnly = true;
    }


    @Override
    protected void execute(SlashCommandEvent event) {
        List<String> players = minecraftHelper.getOnlinePlayerNames();

        StringBuilder builder = new StringBuilder();
        builder.append("Players Online (").append(minecraftHelper.getOnlinePlayerCount()).append("/").append(minecraftHelper.getMaxPlayerCount()).append("):\r\n\r\n");

        for (String player : players) {
            builder.append(player).append("\r\n");
        }

        event.reply(builder.toString()).setEphemeral(true).queue();
    }
}
