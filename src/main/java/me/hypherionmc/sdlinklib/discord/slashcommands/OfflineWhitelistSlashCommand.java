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
import me.hypherionmc.sdlinklib.database.WhitelistTable;
import me.hypherionmc.sdlinklib.discord.BotController;
import me.hypherionmc.sdlinklib.services.helpers.IMinecraftHelper;
import me.hypherionmc.sdlinklib.utils.MinecraftPlayer;
import me.hypherionmc.sdlinklib.utils.SystemUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Collections;
import java.util.List;

import static me.hypherionmc.sdlinklib.config.ConfigController.modConfig;
import static me.hypherionmc.sdlinklib.discord.commands.UnLinkCommand.pattern;

/**
 * @author HypherionSA
 * @date 09/10/2022
 */
public class OfflineWhitelistSlashCommand extends SlashCommand {

    public OfflineWhitelistSlashCommand(BotController controller) {
        this.name = "olwhitelist";
        this.help = "Whitelist/Un-Whitelist Offline Minecraft Users";
        this.guildOnly = true;

        if (modConfig.generalConfig.adminWhitelistOnly) {
            this.userPermissions = new Permission[] { Permission.ADMINISTRATOR, Permission.KICK_MEMBERS };
        }

        if (modConfig.generalConfig.offlinewhitelist) {
            this.children = new SlashCommand[] {
                    new AddWhitelistSlashCommand(controller),
                    new RemoveWhitelistSlashCommand(controller),
                    new ListWhitelistSlashCommand(controller)
            };
        }
    }

    @Override
    protected void execute(SlashCommandEvent event) {}

    public static class AddWhitelistSlashCommand extends SlashCommand {

        private final BotController controller;
        private WhitelistTable whitelistTable = new WhitelistTable();
        private final IMinecraftHelper minecraftHelper;

        public AddWhitelistSlashCommand(BotController controller) {
            this.controller = controller;
            this.name = "add";
            this.help = "Add a player to the whitelist";
            this.guildOnly = true;
            this.minecraftHelper = controller.getMinecraftHelper();

            if (modConfig.generalConfig.adminWhitelistOnly) {
                this.userPermissions = new Permission[]{Permission.ADMINISTRATOR, Permission.KICK_MEMBERS};
            }

            this.options = Collections.singletonList(new OptionData(OptionType.STRING, "mcname", "Your minecraft username"));
        }


        @Override
        protected void execute(SlashCommandEvent event) {
            String mcName = event.getOption("mcname") != null ? event.getOption("mcname").getAsString() : "";

            if (!modConfig.generalConfig.offlinewhitelist) {
                event.reply("Offline Whitelisting is disabled").setEphemeral(true).queue();
                return;
            }
            if (!minecraftHelper.isWhitelistingEnabled()) {
                event.reply("Server Side whitelisting is disabled").setEphemeral(true).queue();
                return;
            }
            if (modConfig.generalConfig.adminWhitelistOnly && !SystemUtils.hasPermission(controller, event.getMember())) {
                event.reply("Sorry, only staff members can use this command").setEphemeral(true).queue();
                return;
            }

            // Add Player To List
            MinecraftPlayer player = MinecraftPlayer.offline(mcName);

            if (minecraftHelper.isPlayerWhitelisted(player)) {
                event.reply("Player " + player.getUsername() + " is already whitelisted on this server").setEphemeral(true).queue();
                return;
            }

            whitelistTable = new WhitelistTable();
            List<WhitelistTable> tables = whitelistTable.fetchAll("discordID = '" + event.getUser().getIdLong() + "'");

            if (!tables.isEmpty() && !SystemUtils.hasPermission(controller, event.getMember())) {
                event.reply("You have already whitelisted a player on this server! Only one whitelist per player is allowed. Please ask a staff member for assistance").setEphemeral(true).queue();
                return;
            }

            whitelistTable.username = player.getUsername();
            whitelistTable.UUID = player.getUuid().toString();
            whitelistTable.discordID = event.getUser().getIdLong();
            if (minecraftHelper.whitelistPlayer(player) && whitelistTable.insert()) {
                event.reply("Player " + mcName + " is now whitelisted!").setEphemeral(true).queue();

                if (controller.getWhitelistedRole() != null && !SystemUtils.hasPermission(controller, event.getMember())) {
                    event.getGuild().addRoleToMember(UserSnowflake.fromId(event.getMember().getId()), controller.getWhitelistedRole()).queue();
                }

                if (modConfig.generalConfig.linkedWhitelist && !SystemUtils.hasPermission(controller, event.getMember())) {
                    String nickName = (event.getMember().getNickname() == null || event.getMember().getNickname().isEmpty()) ? event.getUser().getName() : event.getMember().getNickname();
                    nickName = nickName + " [MC: " + mcName + "]";
                    player.linkAccount(nickName, event.getMember(), event.getGuild(), controller);
                }
            } else {
                event.reply("Player " + mcName + " could not be whitelisted. Either they are already whitelisted, or an error occurred").setEphemeral(true).queue();
            }
        }
    }

    public static class RemoveWhitelistSlashCommand extends SlashCommand {

        private final BotController controller;
        private WhitelistTable whitelistTable = new WhitelistTable();
        private final IMinecraftHelper minecraftHelper;

        public RemoveWhitelistSlashCommand(BotController controller) {
            this.controller = controller;
            this.name = "remove";
            this.help = "Remove a player from the whitelist";
            this.guildOnly = true;
            this.minecraftHelper = controller.getMinecraftHelper();

            if (modConfig.generalConfig.adminWhitelistOnly) {
                this.userPermissions = new Permission[] { Permission.ADMINISTRATOR, Permission.KICK_MEMBERS };
            }

            this.options = Collections.singletonList(new OptionData(OptionType.STRING, "mcname", "Your minecraft username"));
        }


        @Override
        protected void execute(SlashCommandEvent event) {
            String mcName = event.getOption("mcname") != null ? event.getOption("mcname").getAsString() : "";
            whitelistTable = new WhitelistTable();
            whitelistTable.fetch("username = '" + mcName + "'");

            if (whitelistTable.username == null) {
                event.reply("Failed to find player " + mcName + " in the whitelist. Keep in mind, the bot can only un-whitelist players whitelisted through the bot").setEphemeral(true).queue();
                return;
            }

            if (event.getUser().getIdLong() != whitelistTable.discordID && !SystemUtils.hasPermission(controller, event.getMember())) {
                event.reply("Sorry, you cannot un-whitelist this player").setEphemeral(true).queue();
                return;
            }

            MinecraftPlayer player = MinecraftPlayer.offline(mcName);

            if (!minecraftHelper.isPlayerWhitelisted(player)) {
                event.reply("Player " + mcName + " is not whitelisted on this server").setEphemeral(true).queue();
                return;
            }

            if (minecraftHelper.unWhitelistPlayer(player)) {
                whitelistTable.delete();
                event.reply("Player " + mcName + " has been removed from the whitelist").setEphemeral(true).queue();

                if (controller.getWhitelistedRole() != null && !SystemUtils.hasPermission(controller, event.getMember())) {
                    event.getGuild().removeRoleFromMember(UserSnowflake.fromId(event.getMember().getId()), controller.getWhitelistedRole()).queue();
                }

                if (modConfig.generalConfig.linkedWhitelist && !SystemUtils.hasPermission(controller, event.getMember())) {
                    UserTable userTable = new UserTable();
                    List<UserTable> tables = userTable.fetchAll("discordID = '" + event.getUser().getIdLong() + "'");

                    if (!tables.isEmpty()) {
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
                    }
                }
            } else {
                event.reply("Player " + mcName + " could not be un-whitelisted. Either they are not whitelisted, or an error occurred").setEphemeral(true).queue();
            }
        }
    }

    public static class ListWhitelistSlashCommand extends BaseSlashCommand {

        private final IMinecraftHelper minecraftHelper;

        public ListWhitelistSlashCommand(BotController controller) {
            super(controller, true);
            this.name = "list";
            this.help = "List all Whitelisted Players";
            this.guildOnly = true;
            this.minecraftHelper = controller.getMinecraftHelper();
        }


        @Override
        protected void execute(SlashCommandEvent event) {
            List<String> string = minecraftHelper.getWhitelistedPlayers();
            event.reply("**Whitelisted Players:**\n\n" + ArrayUtils.toString(string)).setEphemeral(true).queue();
        }
    }
}
