/*
 * This file is part of sdlink-lib, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2021 - 2023 HypherionSA and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.hypherionmc.sdlinklib.discord;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.SlashCommand;
import me.hypherionmc.sdlinklib.config.ModConfig;
import me.hypherionmc.sdlinklib.discord.commands.*;
import me.hypherionmc.sdlinklib.discord.slashcommands.*;

import java.util.HashMap;

/**
 * @author HypherionSA
 * @date 09/10/2022
 * Command Manager class to register commands as and when needed
 */
public class CommandManager {

    private final HashMap<Command, SlashCommand> commands = new HashMap<>();
    private final BotController botController;

    public CommandManager(BotController controller) {
        this.botController = controller;
        this.addCommands();
    }

    private void addCommands() {
        if (ModConfig.INSTANCE.botCommands.allowPlayerList) {
            commands.put(new PlayerListCommand(botController), new PlayerListSlashCommand(botController));
        }

        if (ModConfig.INSTANCE.botCommands.allowServerStatus) {
            commands.put(new ServerStatusCommand(botController), new ServerStatusSlashCommand(botController));
        }

        if (ModConfig.INSTANCE.generalConfig.offlinewhitelist) {
            commands.put(new OfflineWhitelist(botController), new OfflineWhitelistSlashCommand(botController));
        }

        if (ModConfig.INSTANCE.generalConfig.whitelisting) {
            commands.put(new WhitelistCommand(botController), new WhitelistSlashCommand(botController));
        }

        if (ModConfig.INSTANCE.botCommands.accountLinking) {
            commands.put(new LinkCommand(botController), new LinkSlashCommand(botController));
            commands.put(new UnLinkCommand(botController), new LinkSlashCommand.RemoveLinkSlashCommand(botController));
            commands.put(new LinkedCommand(botController), new LinkedAccountsSlashCommand(botController));
        }

        commands.put(new HelpCommand(botController), new HelpSlashCommand(botController));

        if (ModConfig.INSTANCE.linkedCommands.enabled) {
            commands.put(new MCCommand(botController), new MCSlashCommand(botController));
        }
    }

    public void register(CommandClient commandClient) {
        commands.forEach((cmd, slash) -> {
            commandClient.addCommand(cmd);
            if (slash != null && ModConfig.INSTANCE.botConfig.slashCommands) {
                commandClient.addSlashCommand(slash);
            }
        });
    }
}
