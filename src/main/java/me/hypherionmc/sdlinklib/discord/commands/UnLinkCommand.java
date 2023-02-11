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
import me.hypherionmc.jqlite.data.SQLiteTable;
import me.hypherionmc.sdlinklib.database.UserTable;
import me.hypherionmc.sdlinklib.discord.BotController;
import me.hypherionmc.sdlinklib.utils.SystemUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.UserSnowflake;

import java.util.List;
import java.util.regex.Pattern;

import static me.hypherionmc.sdlinklib.config.ConfigController.modConfig;

public class UnLinkCommand extends Command {

    private UserTable userTable = new UserTable();
    public static final Pattern pattern = Pattern.compile("\\[MC: [a-zA-Z]+]\\s+", Pattern.CASE_INSENSITIVE);

    private final BotController controller;

    public UnLinkCommand(BotController controller) {
        this.guildOnly = true;
        this.controller = controller;

        this.name = "unlink";
        this.help = "Unlink your Minecraft and Discord account";
        this.botPermissions = new Permission[] { Permission.NICKNAME_MANAGE };
    }

    @Override
    protected void execute(CommandEvent event) {
        userTable = new UserTable();
        List<UserTable> tables = userTable.fetchAll("discordID = '" + event.getAuthor().getIdLong() + "'");

        if (tables.isEmpty()) {
            event.reply("Your discord account does not appear to be linked to a minecraft account");
        } else {
            tables.forEach(SQLiteTable::delete);

            String nickName = (event.getMember().getNickname() == null || event.getMember().getNickname().isEmpty()) ? event.getAuthor().getName() : event.getMember().getNickname();
            if (pattern.matcher(nickName).matches()) {
                nickName = pattern.matcher(nickName).replaceAll("");
            }

            try {
                event.getMember().modifyNickname(nickName).queue();
            } catch (Exception e) {
                if (modConfig.generalConfig.debugging) {
                    e.printStackTrace();
                }
            }
            event.reply("Your discord and MC account have been unlinked");

            if (controller.getLinkedRole() != null && !SystemUtils.hasPermission(controller, event.getMember())) {
                event.getGuild().removeRoleFromMember(UserSnowflake.fromId(event.getMember().getId()), controller.getLinkedRole()).queue();
            }
        }
    }
}
