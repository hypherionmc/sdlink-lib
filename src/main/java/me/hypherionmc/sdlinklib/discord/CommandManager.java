package me.hypherionmc.sdlinklib.discord;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.SlashCommand;
import me.hypherionmc.sdlinklib.discord.commands.*;
import me.hypherionmc.sdlinklib.discord.slashcommands.*;

import java.util.HashMap;

import static me.hypherionmc.sdlinklib.config.ConfigController.modConfig;

/**
 * @author HypherionSA
 * @date 09/10/2022
 */
public class CommandManager {

    private HashMap<Command, SlashCommand> commands = new HashMap<>();
    private final BotController botController;

    public CommandManager(BotController controller) {
        this.botController = controller;
        this.addCommands();
    }

    private void addCommands() {
        commands.put(new PlayerListCommand(botController), new PlayerListSlashCommand(botController));
        commands.put(new ServerStatusCommand(botController), new ServerStatusSlashCommand(botController));

        if (modConfig.generalConfig.offlinewhitelist) {
            commands.put(new OfflineWhitelist(botController), new OfflineWhitelistSlashCommand(botController));
        }

        commands.put(new WhitelistCommand(botController), new WhitelistSlashCommand(botController));
        commands.put(new LinkCommand(botController), new LinkSlashCommand(botController));
        commands.put(new UnLinkCommand(botController), new LinkSlashCommand.RemoveLinkSlashCommand(botController));
        commands.put(new LinkedCommand(botController), new LinkedAccountsSlashCommand(botController));
        commands.put(new HelpCommand(botController), new HelpSlashCommand(botController));

        if (modConfig.linkedCommands.enabled) {
            commands.put(new MCCommand(botController), new MCSlashCommand(botController));
        }
    }

    public void register(CommandClient commandClient) {
        commands.forEach((cmd, slash) -> {
            commandClient.addCommand(cmd);
            if (slash != null && modConfig.botConfig.slashCommands) {
                commandClient.addSlashCommand(slash);
            }
        });
    }
}
