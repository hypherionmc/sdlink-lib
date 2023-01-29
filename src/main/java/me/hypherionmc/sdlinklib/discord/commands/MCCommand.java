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
package me.hypherionmc.sdlinklib.discord.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.hypherionmc.sdlinklib.config.configobjects.LinkedCommandsConfig;
import me.hypherionmc.sdlinklib.discord.BotController;
import net.dv8tion.jda.api.entities.Role;

import java.util.Optional;

import static me.hypherionmc.sdlinklib.config.ConfigController.modConfig;

public class MCCommand extends Command {

    private final BotController engine;

    public MCCommand(BotController engine) {
        this.engine = engine;
        this.name = "mc";
        this.arguments = "slug <args>";
        this.help = "Execute Minecraft commands from Discord";
    }

    private void executeCommand(LinkedCommandsConfig.Command mcCommand, String args) {
        engine.getMinecraftHelper().executeMcCommand(mcCommand.mcCommand, args);
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getChannel().getIdLong() != modConfig.channelConfig.consoleChannelID) {
            event.reply("You can only execute MC commands in the Console Channel");
            return;
        }

        if (modConfig.linkedCommands.enabled) {
            String[] cmdArgs = event.getArgs().split(" ");

            String slug = cmdArgs[0] != null ? cmdArgs[0] : "";
            StringBuilder args = new StringBuilder();

            if (cmdArgs.length > 1) {
                for (int i = 1; i < cmdArgs.length; i++) {
                    args.append(cmdArgs[i]);

                    if (i < cmdArgs.length - 1) {
                        args.append(" ");
                    }
                }
            }

            Optional<LinkedCommandsConfig.Command> linkedCommand = modConfig.linkedCommands.commands.stream().filter(c -> c.discordCommand.equalsIgnoreCase(slug)).findFirst();

            linkedCommand.ifPresent(command -> {
                if (!command.discordRole.isEmpty()) {
                    Optional<Role> role = event.getMember().getRoles()
                            .stream().filter(r -> r.getName().equalsIgnoreCase(command.discordRole))
                            .findFirst();

                    if (role.isPresent()) {
                        executeCommand(command, args.toString());
                    } else {
                        event.reply("You need the " + command.discordRole + " role to perform this action");
                    }
                } else {
                    executeCommand(command, args.toString());
                }
            });

            if (!linkedCommand.isPresent()) {
                event.reply("Cannot find linked command " + slug);
            }

        } else {
            event.reply("Linked commands are not enabled!");
        }
    }
}
