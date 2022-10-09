package me.hypherionmc.sdlinklib.discord.slashcommands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import me.hypherionmc.sdlinklib.database.UserTable;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

import java.util.List;

/**
 * @author HypherionSA
 * @date 09/10/2022
 */
public class LinkedAccountsSlashCommand extends SlashCommand {

    private UserTable table = new UserTable();

    public LinkedAccountsSlashCommand() {
        this.name = "linkedacc";
        this.help = "View a list of linked Discord and MC accounts";
        this.userPermissions = new Permission[] { Permission.ADMINISTRATOR, Permission.KICK_MEMBERS };
        this.guildOnly = true;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        table = new UserTable();
        List<UserTable> tables = table.fetchAll();

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Linked Discord and Minecraft Accounts");

        StringBuilder content = new StringBuilder();

        tables.forEach(user -> {
            if (event.getGuild().getMemberById(user.discordID) != null) {
                content.append(event.getGuild().getMemberById(user.discordID).getAsMention()).append(" - MC: ").append(user.username).append("\r\n");
            }
        });

        builder.setDescription(content.toString());
        event.replyEmbeds(builder.build()).setEphemeral(true).queue();
    }

}
