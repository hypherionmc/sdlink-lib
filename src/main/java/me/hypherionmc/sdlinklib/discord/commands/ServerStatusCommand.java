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

import com.jagrosh.jdautilities.command.CommandEvent;
import me.hypherionmc.sdlinklib.discord.BotController;
import me.hypherionmc.sdlinklib.discord.slashcommands.ServerStatusSlashCommand;
import me.hypherionmc.sdlinklib.services.helpers.IMinecraftHelper;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class ServerStatusCommand extends BaseCommand {

    private final IMinecraftHelper minecraftHelper;

    public ServerStatusCommand(BotController controller) {
        super(controller, true);
        this.minecraftHelper = controller.getMinecraftHelper();

        this.name = "status";
        this.help = "View information about your server";
        this.guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        Button refreshButton = Button.danger("refreshbtn", "Refresh");
        event.getChannel().sendMessageEmbeds(ServerStatusSlashCommand.runStatusCommand(minecraftHelper)).addActionRow(refreshButton).queue();
    }

}
