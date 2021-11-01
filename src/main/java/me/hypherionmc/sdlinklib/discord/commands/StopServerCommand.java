package me.hypherionmc.sdlinklib.discord.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.hypherionmc.sdlinklib.discord.utils.MinecraftEventHandler;
import net.dv8tion.jda.api.Permission;

public class StopServerCommand extends Command {

    private final MinecraftEventHandler eventHandler;

    public StopServerCommand(MinecraftEventHandler eventHandler) {
        this.eventHandler = eventHandler;

        this.name = "stop";
        this.help = "Stop the server";
        this.userPermissions = new Permission[] { Permission.ADMINISTRATOR, Permission.KICK_MEMBERS };
        this.guildOnly = true;
    }


    @Override
    protected void execute(CommandEvent event) {
        eventHandler.sendStopCommand();
    }
}
