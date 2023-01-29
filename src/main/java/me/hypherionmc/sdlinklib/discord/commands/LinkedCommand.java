package me.hypherionmc.sdlinklib.discord.commands;

import com.jagrosh.jdautilities.command.CommandEvent;
import me.hypherionmc.sdlinklib.database.UserTable;
import me.hypherionmc.sdlinklib.discord.BotController;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.List;

public class LinkedCommand extends BaseCommand {

    private UserTable table = new UserTable();

    public LinkedCommand(BotController controller) {
        super(controller, true);
        this.name = "linkedacc";
        this.help = "View a list of linked Discord and MC accounts";
    }

    @Override
    protected void execute(CommandEvent event) {
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
        event.reply(builder.build());
    }
}
