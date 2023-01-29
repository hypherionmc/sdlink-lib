package me.hypherionmc.sdlinklib.discord.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.hypherionmc.sdlinklib.discord.BotController;
import net.dv8tion.jda.api.Permission;

public class BaseCommand extends Command {

    public BaseCommand(BotController controller, boolean requiresPerms) {
        if (requiresPerms) {
            if (!controller.getAdminRole().isEmpty()) {
                this.requiredRole = controller.getAdminRole();
            } else {
                this.userPermissions = new Permission[] { Permission.ADMINISTRATOR, Permission.KICK_MEMBERS };
            }
        }
    }

    @Override
    protected void execute(CommandEvent event) {

    }
}
