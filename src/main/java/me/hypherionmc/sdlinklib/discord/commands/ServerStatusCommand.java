package me.hypherionmc.sdlinklib.discord.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.hypherionmc.sdlinklib.config.ModConfig;
import me.hypherionmc.sdlinklib.services.helpers.IMinecraftHelper;
import me.hypherionmc.sdlinklib.utils.SystemUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;

public class ServerStatusCommand extends Command {

    private final ModConfig config;
    private final IMinecraftHelper minecraftHelper;

    public ServerStatusCommand(IMinecraftHelper helper, ModConfig config) {
        this.config = config;
        this.minecraftHelper = helper;

        this.name = "status";
        this.help = "View information about your server";
        this.guildOnly = true;
        this.userPermissions = new Permission[] { Permission.ADMINISTRATOR, Permission.KICK_MEMBERS };
        this.cooldown = 5;
    }

    @Override
    protected void execute(CommandEvent event) {
        SystemInfo systemInfo = new SystemInfo();
        HardwareAbstractionLayer hal = systemInfo.getHardware();
        CentralProcessor cpu = hal.getProcessor();

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Server Information / Status");

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("**__System Information__**\r\n\r\n");

        stringBuilder
                .append("**CPU:**\r\n```\r\n")
                .append(cpu.toString())
                .append("```")
                .append("\r\n");

        stringBuilder
                .append("**Memory:**\r\n```\r\n")
                .append(SystemUtils.byteToHuman(hal.getMemory().getAvailable()))
                .append(" free of ")
                .append(SystemUtils.byteToHuman(hal.getMemory().getTotal()))
                .append("```\r\n");

        stringBuilder
                .append("**OS:**\r\n```\r\n")
                .append(systemInfo.getOperatingSystem().toString())
                .append(" (")
                .append(systemInfo.getOperatingSystem().getBitness())
                .append(" bit)\r\n")
                .append("Version: ")
                .append(systemInfo.getOperatingSystem().getVersionInfo().toString())
                .append("```\r\n");

        stringBuilder
                .append("**System Uptime:**\r\n```\r\n")
                .append(SystemUtils.secondsToTimestamp(systemInfo.getOperatingSystem().getSystemUptime()))
                .append("```\r\n");

        stringBuilder.append("**__Minecraft Information__**\r\n\r\n");

        stringBuilder
                .append("**Server Uptime:**\r\n```\r\n")
                .append(SystemUtils.secondsToTimestamp(minecraftHelper.getServerUptime()))
                .append("```\r\n");

        stringBuilder
                .append("**Server Version:**\r\n```\r\n")
                .append(minecraftHelper.getServerVersion())
                .append("```\r\n");

        stringBuilder
                .append("**Players Online:**\r\n```\r\n")
                .append(minecraftHelper.getOnlinePlayerCount() + "/" + minecraftHelper.getMaxPlayerCount())
                .append("```\r\n");

        stringBuilder
                .append("**Whitelisting:**\r\n```\r\n")
                .append(minecraftHelper.isWhitelistingEnabled() ? "Enabled" : "Disabled")
                .append("```\r\n");

        builder.setDescription(stringBuilder.toString());

        event.reply(builder.build());
    }

}
