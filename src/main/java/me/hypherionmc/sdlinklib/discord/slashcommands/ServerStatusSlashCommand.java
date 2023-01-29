package me.hypherionmc.sdlinklib.discord.slashcommands;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import me.hypherionmc.sdlinklib.discord.BotController;
import me.hypherionmc.sdlinklib.services.helpers.IMinecraftHelper;
import me.hypherionmc.sdlinklib.utils.SystemUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;

/**
 * @author HypherionSA
 * @date 09/10/2022
 */
public class ServerStatusSlashCommand extends BaseSlashCommand {

    private final IMinecraftHelper minecraftHelper;

    public ServerStatusSlashCommand(BotController controller) {
        super(controller, true);
        this.minecraftHelper = controller.getMinecraftHelper();

        this.name = "status";
        this.help = "View information about your server";
        this.guildOnly = true;
    }


    @Override
    protected void execute(SlashCommandEvent event) {
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

        try {
            stringBuilder
                    .append("**Memory:**\r\n```\r\n")
                    .append(SystemUtils.byteToHuman(hal.getMemory().getAvailable()))
                    .append(" free of ")
                    .append(SystemUtils.byteToHuman(hal.getMemory().getTotal()))
                    .append("```\r\n");
        } catch (Exception e) {}

        stringBuilder
                .append("**OS:**\r\n```\r\n")
                .append(systemInfo.getOperatingSystem().toString())
                .append(" (")
                .append(systemInfo.getOperatingSystem().getBitness())
                .append(" bit)\r\n")
                .append("Version: ")
                .append(systemInfo.getOperatingSystem().getVersionInfo().getVersion())
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

        event.replyEmbeds(builder.build()).setEphemeral(true).queue();
    }
}
