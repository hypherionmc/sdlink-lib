package me.hypherionmc.sdlinklib.discord.slashcommands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import me.hypherionmc.sdlinklib.discord.BotController;
import net.dv8tion.jda.api.Permission;

public class BaseSlashCommand extends SlashCommand {

    public BaseSlashCommand(BotController controller, boolean requiresPerms) {
        if (requiresPerms) {
            if (!controller.getAdminRole().isEmpty()) {
                this.requiredRole = controller.getAdminRole();
            } else {
                this.userPermissions = new Permission[] { Permission.ADMINISTRATOR, Permission.KICK_MEMBERS };
            }
        }
    }

    @Override
    protected void execute(SlashCommandEvent event) {

    }
}
