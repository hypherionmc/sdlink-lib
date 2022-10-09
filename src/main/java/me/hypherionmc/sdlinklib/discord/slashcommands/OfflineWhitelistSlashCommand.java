package me.hypherionmc.sdlinklib.discord.slashcommands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import me.hypherionmc.jqlite.data.SQLiteTable;
import me.hypherionmc.sdlinklib.database.UserTable;
import me.hypherionmc.sdlinklib.database.WhitelistTable;
import me.hypherionmc.sdlinklib.discord.BotController;
import me.hypherionmc.sdlinklib.services.helpers.IMinecraftHelper;
import me.hypherionmc.sdlinklib.utils.PlayerUtils;
import me.hypherionmc.sdlinklib.utils.SystemUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

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

        private WhitelistTable whitelistTable = new WhitelistTable();
        private final IMinecraftHelper minecraftHelper;

        public AddWhitelistSlashCommand(BotController controller) {
            this.name = "add";
            this.help = "Add a player to the whitelist";
            this.guildOnly = true;
            this.minecraftHelper = controller.getMinecraftHelper();

            if (modConfig.generalConfig.adminWhitelistOnly) {
                this.userPermissions = new Permission[] { Permission.ADMINISTRATOR, Permission.KICK_MEMBERS };
            }

            this.options = Collections.singletonList(new OptionData(OptionType.STRING, "mcname", "Your minecraft username"));
        }


        @Override
        protected void execute(SlashCommandEvent event) {
            if (modConfig.generalConfig.offlinewhitelist && minecraftHelper.isWhitelistingEnabled()) {
                String mcName = event.getOption("mcname") != null ? event.getOption("mcname").getAsString() : "";

                Pair<String, String> player = PlayerUtils.offlineUUID(mcName);

                if (player.getLeft().isEmpty() || player.getRight().isEmpty()) {
                    event.reply("Failed to fetch info for player " + mcName).setEphemeral(true).queue();
                } else {
                    if (minecraftHelper.isPlayerWhitelisted(player.getLeft(), PlayerUtils.mojangIdToUUID(player.getRight()))) {
                        event.reply("Player " + player.getLeft() + " is already whitelisted on this server").setEphemeral(true).queue();
                    } else {
                        whitelistTable = new WhitelistTable();
                        List<WhitelistTable> tables = whitelistTable.fetchAll("discordID = '" + event.getMember().getIdLong() + "'");
                        if (!tables.isEmpty() && !SystemUtils.hasPermission(event.getMember())) {
                            event.reply("You have already whitelisted a player on this server! Only one whitelist per player is allowed. Please ask an admin for assistance").setEphemeral(true).queue();
                        } else {
                            whitelistTable.username = player.getLeft();
                            whitelistTable.UUID = player.getRight();
                            whitelistTable.discordID = event.getMember().getIdLong();
                            if (minecraftHelper.whitelistPlayer(player.getLeft(), PlayerUtils.mojangIdToUUID(player.getRight())) && whitelistTable.insert()) {
                                event.reply("Player " + player.getLeft() + " is now whitelisted!").setEphemeral(true).queue();
                            } else {
                                event.reply("Player " + player.getLeft() + " could not be whitelisted. Either they are already whitelisted, or an error occurred").setEphemeral(true).queue();
                            }

                            if (modConfig.generalConfig.linkedWhitelist && !SystemUtils.hasPermission(event.getMember())) {
                                UserTable userTable = new UserTable();
                                userTable.username = player.getLeft();
                                userTable.UUID = player.getRight();
                                userTable.discordID = event.getMember().getIdLong();

                                List<UserTable> userTables = userTable.fetchAll("discordID = '" + event.getMember().getIdLong() + "'");
                                if (userTables.isEmpty()) {
                                    userTable.insert();
                                } else {
                                    userTable.update();
                                }

                                String nickName = (event.getMember().getNickname() == null || event.getMember().getNickname().isEmpty()) ? event.getMember().getEffectiveName() : event.getMember().getNickname();
                                nickName = nickName + " [MC: " + player.getLeft() + "]";

                                try {
                                    event.getMember().modifyNickname(nickName).queue();
                                } catch (Exception e) {
                                    if (modConfig.generalConfig.debugging) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                event.reply("Whitelisting is not enabled, or it's disabled on your server").setEphemeral(true).queue();
            }
        }
    }

    public static class RemoveWhitelistSlashCommand extends SlashCommand {

        private WhitelistTable whitelistTable = new WhitelistTable();
        private final IMinecraftHelper minecraftHelper;

        public RemoveWhitelistSlashCommand(BotController controller) {
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
            whitelistTable = new WhitelistTable();
            String mcName = event.getOption("mcname") != null ? event.getOption("mcname").getAsString() : "";
            whitelistTable.fetch("discordID = '" + event.getMember().getIdLong() + "'");

            if ((whitelistTable.username == null || !whitelistTable.username.equalsIgnoreCase(mcName)) && !SystemUtils.hasPermission(event.getMember())) {
                event.reply("Sorry, you cannot un-whitelist this player").setEphemeral(true).queue();
            } else {
                Pair<String, String> player = PlayerUtils.offlineUUID(mcName);

                if (player.getLeft().isEmpty() || player.getRight().isEmpty()) {
                    event.reply("Failed to fetch info for player " + mcName).setEphemeral(true).queue();
                } else if (!minecraftHelper.isPlayerWhitelisted(player.getLeft(), PlayerUtils.mojangIdToUUID(player.getRight()))) {
                    event.reply("Player " + player.getLeft() + " is not whitelisted on this server").setEphemeral(true).queue();
                } else {
                    if (minecraftHelper.unWhitelistPlayer(player.getLeft(), PlayerUtils.mojangIdToUUID(player.getRight()))) {
                        whitelistTable.delete();
                        event.reply("Player " + player.getLeft() + " has been removed from the whitelist").setEphemeral(true).queue();
                    } else {
                        event.reply("Player " + player.getLeft() + " could not be un-whitelisted. Either they are not whitelisted, or an error occurred").setEphemeral(true).queue();
                    }

                    if (modConfig.generalConfig.linkedWhitelist && !SystemUtils.hasPermission(event.getMember())) {
                        UserTable userTable = new UserTable();
                        List<UserTable> tables = userTable.fetchAll("discordID = '" + event.getMember().getIdLong() + "'");

                        if (tables.isEmpty()) {
                            event.reply("Your discord account does not appear to be linked to a minecraft account");
                        } else {
                            tables.forEach(SQLiteTable::delete);

                            String nickName = (event.getMember().getNickname() == null || event.getMember().getNickname().isEmpty()) ? event.getMember().getEffectiveName() : event.getMember().getNickname();
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
                }
            }
        }
    }

    public static class ListWhitelistSlashCommand extends SlashCommand {

        private final IMinecraftHelper minecraftHelper;

        public ListWhitelistSlashCommand(BotController controller) {
            this.name = "list";
            this.help = "List all Whitelisted Players";
            this.guildOnly = true;
            this.minecraftHelper = controller.getMinecraftHelper();

            this.userPermissions = new Permission[] { Permission.ADMINISTRATOR, Permission.KICK_MEMBERS };
        }


        @Override
        protected void execute(SlashCommandEvent event) {
            List<String> string = minecraftHelper.getWhitelistedPlayers();
            event.reply("**Whitelisted Players:**\n\n" + ArrayUtils.toString(string)).setEphemeral(true).queue();
        }
    }
}
