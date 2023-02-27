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
import me.hypherionmc.sdlinklib.database.WhitelistTable;
import me.hypherionmc.sdlinklib.discord.BotController;
import me.hypherionmc.sdlinklib.services.helpers.IMinecraftHelper;
import me.hypherionmc.sdlinklib.utils.MinecraftPlayer;
import me.hypherionmc.sdlinklib.utils.SystemUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

import static me.hypherionmc.sdlinklib.config.ConfigController.modConfig;
import static me.hypherionmc.sdlinklib.discord.commands.UnLinkCommand.pattern;

public class OfflineWhitelist extends Command {

    private final BotController controller;

    private WhitelistTable whitelistTable = new WhitelistTable();

    private IMinecraftHelper minecraftHelper;

    public OfflineWhitelist(BotController controller) {
        this.controller = controller;
        this.minecraftHelper = controller.getMinecraftHelper();

        this.name = "olwhitelist";
        this.help = "Control Offline Whitelisting if enabled";

        if (modConfig.generalConfig.adminWhitelistOnly) {
            this.userPermissions = new Permission[] { Permission.ADMINISTRATOR, Permission.KICK_MEMBERS };
        }
    }

    @Override
    protected void execute(CommandEvent event) {

        if (event.getArgs().isEmpty()) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Offline Whitelist Command Help");
            embedBuilder.setDescription(
                    "This command allows players to whitelist/un-whitelist themselves on your server. Admins/Members with Kick Members permission can un-whitelist anyone\r\n\r\n" +
                            "Command Usage:\r\n" +
                            "`olwhitelist add playername` -> Add a player to the whitelist\r\n" +
                            "`olwhitelist remove playername` -> Remove a player from the whitelist\r\n" +
                            "`olwhitelist list` -> View whitelisted players (Admins/Moderators only)"
            );

            event.reply(embedBuilder.build());
        } else {
            if (!modConfig.generalConfig.offlinewhitelist) {
                event.reply("Offline Whitelisting is disabled");
                return;
            }
            if (!minecraftHelper.isWhitelistingEnabled()) {
                event.reply("Server Side whitelisting is disabled");
                return;
            }
            if (modConfig.generalConfig.adminWhitelistOnly && !SystemUtils.hasPermission(controller, event.getMember())) {
                event.reply("Sorry, only staff members can use this command");
                return;
            }

            String[] args = event.getArgs().split(" ");

            // Add Player To List
            if (args[0].equalsIgnoreCase("add")) {
                MinecraftPlayer player = MinecraftPlayer.offline(args[1]);

                if (minecraftHelper.isPlayerWhitelisted(player)) {
                    event.reply("Player " + player.getUsername() + " is already whitelisted on this server");
                    return;
                }

                whitelistTable = new WhitelistTable();
                List<WhitelistTable> tables = whitelistTable.fetchAll("discordID = '" + event.getAuthor().getIdLong() + "'");

                if (!tables.isEmpty() && !SystemUtils.hasPermission(controller, event.getMember())) {
                    event.reply("You have already whitelisted a player on this server! Only one whitelist per player is allowed. Please ask a staff member for assistance");
                    return;
                }

                whitelistTable.username = player.getUsername();
                whitelistTable.UUID = player.getUuid().toString();
                whitelistTable.discordID = event.getAuthor().getIdLong();
                if (minecraftHelper.whitelistPlayer(player) && whitelistTable.insert()) {
                    event.reply("Player " + args[1] + " is now whitelisted!");

                    if (controller.getWhitelistedRole() != null && !SystemUtils.hasPermission(controller, event.getMember())) {
                        event.getGuild().addRoleToMember(UserSnowflake.fromId(event.getMember().getId()), controller.getWhitelistedRole()).queue();
                    }

                    if (modConfig.generalConfig.linkedWhitelist && !SystemUtils.hasPermission(controller, event.getMember())) {
                        String nickName = event.getMember().getEffectiveName();
                        player.linkAccount(nickName, event.getMember(), event.getGuild(), controller);
                        return;
                    }
                } else {
                    event.reply("Player " + args[1] + " could not be whitelisted. Either they are already whitelisted, or an error occurred");
                    return;
                }
            }

            // Remove from Whitelist
            if (args[0].equalsIgnoreCase("remove")) {
                whitelistTable = new WhitelistTable();
                whitelistTable.fetch("username = '" + args[1] + "'");

                if (whitelistTable.username == null) {
                    event.reply("Failed to find player " + args[1] + " in the whitelist. Keep in mind, the bot can only un-whitelist players whitelisted through the bot");
                    return;
                }

                if (event.getAuthor().getIdLong() != whitelistTable.discordID && !SystemUtils.hasPermission(controller, event.getMember())) {
                    event.reply("Sorry, you cannot un-whitelist this player");
                    return;
                }

                MinecraftPlayer player = MinecraftPlayer.offline(args[1]);

                if (!minecraftHelper.isPlayerWhitelisted(player)) {
                    event.reply("Player " + args[1] + " is not whitelisted on this server");
                    return;
                }

                if (minecraftHelper.unWhitelistPlayer(player)) {
                    whitelistTable.delete();
                    event.reply("Player " + args[1] + " has been removed from the whitelist");

                    if (controller.getWhitelistedRole() != null && !SystemUtils.hasPermission(controller, event.getMember())) {
                        event.getGuild().removeRoleFromMember(UserSnowflake.fromId(event.getMember().getId()), controller.getWhitelistedRole()).queue();
                    }

                    if (modConfig.generalConfig.linkedWhitelist && !SystemUtils.hasPermission(controller, event.getMember())) {
                        UserTable userTable = new UserTable();
                        List<UserTable> tables = userTable.fetchAll("discordID = '" + event.getAuthor().getIdLong() + "'");

                        if (!tables.isEmpty()) {
                            tables.forEach(SQLiteTable::delete);

                            String nickName = event.getMember().getEffectiveName();
                            if (pattern.matcher(nickName).find()) {
                                try {
                                    event.getMember().modifyNickname(null).queue();
                                } catch (Exception e) {
                                    if (modConfig.generalConfig.debugging) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }

                    return;
                } else {
                    event.reply("Player " + args[1] + " could not be un-whitelisted. Either they are not whitelisted, or an error occurred");
                    return;
                }
            }

            if (args[0].equalsIgnoreCase("list") && SystemUtils.hasPermission(controller, event.getMember())) {
                List<String> string = minecraftHelper.getWhitelistedPlayers();
                event.reply("**Whitelisted Players:**\n\n" + ArrayUtils.toString(string));
            }
        }
    }
}

