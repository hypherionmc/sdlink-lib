package me.hypherionmc.sdlinklib.discord.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.hypherionmc.sdlinklib.config.ModConfig;
import me.hypherionmc.sdlinklib.discord.utils.MinecraftEventHandler;
import me.hypherionmc.sdlinklib.utils.SystemUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;

public class ServerStatusCommand extends Command {

    private final ModConfig config;
    private final MinecraftEventHandler eventHandler;

    public ServerStatusCommand(ModConfig config, MinecraftEventHandler eventHandler) {
        this.config = config;
        this.eventHandler = eventHandler;

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
                .append(SystemUtils.secondsToTimestamp(eventHandler.getServerUptime()))
                .append("```\r\n");

        stringBuilder
                .append("**Average Ticks Per Second:**\r\n```\r\n")
                .append(String.valueOf(eventHandler.getTPS()).replace(".0", ""))
                .append(" tps")
                .append("```\r\n");

        stringBuilder
                .append("**Server Version:**\r\n```\r\n")
                .append(eventHandler.getServerVersion())
                .append("```\r\n");

        stringBuilder
                .append("**Players Online:**\r\n```\r\n")
                .append(eventHandler.getPlayerCount() + "/" + eventHandler.getMaxPlayerCount())
                .append("```\r\n");

        stringBuilder
                .append("**Whitelisting:**\r\n```\r\n")
                .append(config.general.whitelisting ? "Enabled" : "Disabled")
                .append("```\r\n");

        builder.setDescription(stringBuilder.toString());

        event.reply(builder.build());
    }

}
