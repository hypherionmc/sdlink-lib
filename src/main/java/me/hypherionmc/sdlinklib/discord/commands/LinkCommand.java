package me.hypherionmc.sdlinklib.discord.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.hypherionmc.sdlinklib.config.ModConfig;
import me.hypherionmc.sdlinklib.database.UserTable;
import me.hypherionmc.sdlinklib.utils.PlayerUtils;
import net.dv8tion.jda.api.Permission;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class LinkCommand extends Command {

    private UserTable userTable;
    private ModConfig config;

    public LinkCommand(UserTable userTable, ModConfig config) {
        this.userTable = userTable;
        this.config = config;
        this.guildOnly = true;

        this.name = "link";
        this.help = "Link your Minecraft and Discord account together";
        this.botPermissions = new Permission[] { Permission.NICKNAME_MANAGE };
    }

    @Override
    protected void execute(CommandEvent event) {
        userTable = new UserTable();

        if (event.getArgs().isEmpty()) {
            event.reply("You need to supply your Minecraft username");
        } else {
            String[] args = event.getArgs().split(" ");
            Pair<String, String> player = PlayerUtils.fetchUUID(args[0]);
            if (player.getLeft().isEmpty() || player.getRight().isEmpty()) {
                event.reply("Failed to fetch info for player " + args[0]);
            } else {
                userTable.username = player.getLeft();
                userTable.UUID = player.getRight();
                userTable.discordID = event.getAuthor().getIdLong();

                List<UserTable> tables = userTable.fetchAll("discordID = '" + event.getAuthor().getIdLong() + "'");
                if (tables.isEmpty()) {
                    userTable.insert();
                } else {
                    userTable.update();
                }

                String nickName = (event.getMember().getNickname() == null || event.getMember().getNickname().isEmpty()) ? event.getAuthor().getName() : event.getMember().getNickname();
                nickName = nickName + " [MC: " + player.getLeft() + "]";

               try {
                   event.getMember().modifyNickname(nickName).queue();
               } catch (Exception e) {
                   if (config.general.debugging) {
                       e.printStackTrace();
                   }
               }
                event.reply("Your discord and MC account have been linked");
            }
        }
    }
}
