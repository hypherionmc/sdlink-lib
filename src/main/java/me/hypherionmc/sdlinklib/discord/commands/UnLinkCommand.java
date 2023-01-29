package me.hypherionmc.sdlinklib.discord.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.hypherionmc.jqlite.data.SQLiteTable;
import me.hypherionmc.sdlinklib.database.UserTable;
import me.hypherionmc.sdlinklib.discord.BotController;
import net.dv8tion.jda.api.Permission;

import java.util.List;
import java.util.regex.Pattern;

import static me.hypherionmc.sdlinklib.config.ConfigController.modConfig;

public class UnLinkCommand extends Command {

    private UserTable userTable = new UserTable();
    public static final Pattern pattern = Pattern.compile("\\[MC: [a-zA-Z]+]\\s+", Pattern.CASE_INSENSITIVE);

    public UnLinkCommand(BotController controller) {
        this.guildOnly = true;

        this.name = "unlink";
        this.help = "Unlink your Minecraft and Discord account";
        this.botPermissions = new Permission[] { Permission.NICKNAME_MANAGE };
    }

    @Override
    protected void execute(CommandEvent event) {
        userTable = new UserTable();
        List<UserTable> tables = userTable.fetchAll("discordID = '" + event.getAuthor().getIdLong() + "'");

        if (tables.isEmpty()) {
            event.reply("Your discord account does not appear to be linked to a minecraft account");
        } else {
            tables.forEach(SQLiteTable::delete);

            String nickName = (event.getMember().getNickname() == null || event.getMember().getNickname().isEmpty()) ? event.getAuthor().getName() : event.getMember().getNickname();
            if (pattern.matcher(nickName).matches()) {
                nickName = pattern.matcher(nickName).replaceAll("");
            }

            try {
                event.getMember().modifyNickname(nickName).queue();
            } catch (Exception e) {
                if (modConfig.generalConfig.debugging) {
                    e.printStackTrace();
                }
            }
            event.reply("Your discord and MC account have been unlinked");
        }
    }
}
