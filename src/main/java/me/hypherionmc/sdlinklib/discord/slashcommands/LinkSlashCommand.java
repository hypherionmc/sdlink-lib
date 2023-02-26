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
package me.hypherionmc.sdlinklib.discord.slashcommands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import me.hypherionmc.jqlite.data.SQLiteTable;
import me.hypherionmc.sdlinklib.database.UserTable;
import me.hypherionmc.sdlinklib.discord.BotController;
import me.hypherionmc.sdlinklib.utils.MinecraftPlayer;
import me.hypherionmc.sdlinklib.utils.Result;
import me.hypherionmc.sdlinklib.utils.SystemUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static me.hypherionmc.sdlinklib.config.ConfigController.modConfig;

/**
 * @author HypherionSA
 * @date 09/10/2022
 */
public class LinkSlashCommand extends SlashCommand {

    private final BotController controller;

    public LinkSlashCommand(BotController controller) {
        this.controller = controller;
        this.guildOnly = true;
        this.name = "link";
        this.help = "Link your Minecraft and Discord account together";
        this.botPermissions = new Permission[] { Permission.NICKNAME_MANAGE };

        this.options = Collections.singletonList(new OptionData(OptionType.STRING, "mcname", "Your Minecraft Username").setRequired(true));
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String mcName = event.getOption("mcname") != null ? event.getOption("mcname").getAsString() : "";

        if (mcName.isEmpty()) {
            event.reply("You need to supply your Minecraft username").setEphemeral(true).queue();
        } else {
            MinecraftPlayer player = MinecraftPlayer.standard(mcName);

            if (!player.isValid()) {
                event.reply("Failed to fetch info for player " + mcName);
                return;
            }

            String nickName = (event.getMember().getNickname() == null || event.getMember().getNickname().isEmpty()) ? event.getUser().getName() : event.getMember().getNickname();
            nickName = nickName + " [MC: " + player.getUsername() + "]";
            Result result = player.linkAccount(nickName, event.getMember(), event.getGuild(), controller);
            event.reply(result.getMessage()).setEphemeral(true).queue();
        }
    }

    public static class RemoveLinkSlashCommand extends SlashCommand {
        private final BotController controller;

        final Pattern pattern = Pattern.compile("\\[MC: [a-zA-Z]+]\\s+", Pattern.CASE_INSENSITIVE);

        public RemoveLinkSlashCommand(BotController controller) {
            this.controller = controller;

            this.guildOnly = true;
            this.name = "unlink";
            this.help = "Unlink your Minecraft and Discord account";
            this.botPermissions = new Permission[] { Permission.NICKNAME_MANAGE };
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            UserTable userTable = new UserTable();
            List<UserTable> tables = userTable.fetchAll("discordID = '" + event.getUser().getIdLong() + "'");

            if (tables.isEmpty()) {
                event.reply("Your discord account does not appear to be linked to a minecraft account");
            } else {
                tables.forEach(SQLiteTable::delete);

                String nickName = (event.getMember().getNickname() == null || event.getMember().getNickname().isEmpty()) ? event.getUser().getName() : event.getMember().getNickname();
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

 }
