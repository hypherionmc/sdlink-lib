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
import me.hypherionmc.sdlinklib.utils.MinecraftPlayer;
import me.hypherionmc.sdlinklib.utils.Result;
import me.hypherionmc.sdlinklib.utils.SystemUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.UserSnowflake;

public class LinkCommand extends BaseCommand {

    private final BotController controller;

    public LinkCommand(BotController controller) {
        super(controller, false);
        this.controller =  controller;
        this.guildOnly = true;

        this.name = "link";
        this.help = "Link your Minecraft and Discord account together";
        this.botPermissions = new Permission[] { Permission.NICKNAME_MANAGE };
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.reply("You need to supply your Minecraft username");
        } else {
            String[] args = event.getArgs().split(" ");
            MinecraftPlayer player = MinecraftPlayer.standard(args[0]);

            if (!player.isValid()) {
                event.reply("Failed to fetch info for player " + args[0]);
                return;
            }

            String nickName = (event.getMember().getNickname() == null || event.getMember().getNickname().isEmpty()) ? event.getAuthor().getName() : event.getMember().getNickname();
            nickName = nickName + " [MC: " + args[0] + "]";
            Result result = player.linkAccount(nickName, event.getMember());
            event.reply(result.getMessage());

            if(controller.getLinkedRole() != null && !SystemUtils.hasPermission(controller, event.getMember())) {
                event.getGuild().addRoleToMember(UserSnowflake.fromId(event.getMember().getId()), controller.getLinkedRole()).queue();
            }
        }
    }
}
