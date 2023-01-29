package me.hypherionmc.sdlinklib.discord.commands;

import com.jagrosh.jdautilities.command.CommandEvent;
import me.hypherionmc.sdlinklib.discord.BotController;
import me.hypherionmc.sdlinklib.utils.MinecraftPlayer;
import me.hypherionmc.sdlinklib.utils.Result;
import net.dv8tion.jda.api.Permission;

public class LinkCommand extends BaseCommand {

    public LinkCommand(BotController controller) {
        super(controller, false);
        this.guildOnly = true;

        this.name = "link";
        this.help = "Link your Minecraft and Discord account together";
        this.botPermissions = new Permission[] { Permission.NICKNAME_MANAGE };
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.reply("You need to supply your Minecraft username");
        } else {
            String[] args = event.getArgs().split(" ");
            MinecraftPlayer player = MinecraftPlayer.standard(args[0]);

            if (!player.isValid()) {
                event.reply("Failed to fetch info for player " + args[0]);
                return;
            }

            String nickName = (event.getMember().getNickname() == null || event.getMember().getNickname().isEmpty()) ? event.getAuthor().getName() : event.getMember().getNickname();
            nickName = nickName + " [MC: " + args[1] + "]";
            Result result = player.linkAccount(nickName, event.getMember());
            event.reply(result.getMessage());
        }
    }
}
