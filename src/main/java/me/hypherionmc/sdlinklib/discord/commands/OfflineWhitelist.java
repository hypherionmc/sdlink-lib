package me.hypherionmc.sdlinklib.discord.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.hypherionmc.jqlite.data.SQLiteTable;
import me.hypherionmc.sdlinklib.database.UserTable;
import me.hypherionmc.sdlinklib.database.WhitelistTable;
import me.hypherionmc.sdlinklib.discord.BotController;
import me.hypherionmc.sdlinklib.services.helpers.IMinecraftHelper;
import me.hypherionmc.sdlinklib.utils.PlayerUtils;
import me.hypherionmc.sdlinklib.utils.SystemUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;
import java.util.UUID;

import static me.hypherionmc.sdlinklib.config.ConfigController.modConfig;
import static me.hypherionmc.sdlinklib.discord.commands.UnLinkCommand.pattern;

public class OfflineWhitelist extends Command {

    private WhitelistTable whitelistTable = new WhitelistTable();

    private IMinecraftHelper minecraftHelper;

    public OfflineWhitelist(BotController controller) {
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
            if (modConfig.generalConfig.offlinewhitelist) {
                if (minecraftHelper.isWhitelistingEnabled()) {
                    String[] args = event.getArgs().split(" ");

                    if (args[0].equalsIgnoreCase("add")) {
                        UUID uuid = PlayerUtils.offlineNameToUUID(args[1]);

                        if (args[1].isEmpty() || uuid.toString().isEmpty()) {
                            event.reply("Failed to fetch info for player " + args[1]);
                        } else {
                            if (minecraftHelper.isPlayerWhitelisted(args[1], uuid)) {
                                event.reply("Player " + args[1] + " is already whitelisted on this server");
                            } else {
                                whitelistTable = new WhitelistTable();
                                List<WhitelistTable> tables = whitelistTable.fetchAll("discordID = '" + event.getAuthor().getIdLong() + "'");
                                if (!tables.isEmpty() && !SystemUtils.hasPermission(event.getMember())) {
                                    event.reply("You have already whitelisted a player on this server! Only one whitelist per player is allowed. Please ask an admin for assistance");
                                } else {
                                    whitelistTable.username = args[1];
                                    whitelistTable.UUID = uuid.toString();
                                    whitelistTable.discordID = event.getAuthor().getIdLong();
                                    if (minecraftHelper.whitelistPlayer(args[1], uuid) && whitelistTable.insert()) {
                                        event.reply("Player " + args[1] + " is now whitelisted!");
                                    } else {
                                        event.reply("Player " + args[1] + " could not be whitelisted. Either they are already whitelisted, or an error occurred");
                                    }

                                    if (modConfig.generalConfig.linkedWhitelist && !SystemUtils.hasPermission(event.getMember())) {
                                        UserTable userTable = new UserTable();
                                        userTable.username = args[1];
                                        userTable.UUID = uuid.toString();
                                        userTable.discordID = event.getAuthor().getIdLong();

                                        List<UserTable> userTables = userTable.fetchAll("discordID = '" + event.getAuthor().getIdLong() + "'");
                                        if (userTables.isEmpty()) {
                                            userTable.insert();
                                        } else {
                                            userTable.update();
                                        }

                                        String nickName = (event.getMember().getNickname() == null || event.getMember().getNickname().isEmpty()) ? event.getAuthor().getName() : event.getMember().getNickname();
                                        nickName = nickName + " [MC: " + args[1] + "]";

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

                    if (args[0].equalsIgnoreCase("remove")) {
                        whitelistTable = new WhitelistTable();
                        whitelistTable.fetch("discordID = '" + event.getAuthor().getIdLong() + "'");

                        if ((whitelistTable.username == null || !whitelistTable.username.equalsIgnoreCase(args[1])) && !SystemUtils.hasPermission(event.getMember())) {
                            event.reply("Sorry, you cannot un-whitelist this player");
                        } else {
                            UUID uuid = PlayerUtils.offlineNameToUUID(args[1]);

                            if (args[1].isEmpty() || uuid.toString().isEmpty()) {
                                event.reply("Failed to fetch info for player " + args[1]);
                            } else if (!minecraftHelper.isPlayerWhitelisted(args[1], uuid)) {
                                event.reply("Player " + args[1] + " is not whitelisted on this server");
                            } else {
                                if (minecraftHelper.unWhitelistPlayer(args[1], uuid)) {
                                    whitelistTable.delete();
                                    event.reply("Player " + args[1] + " has been removed from the whitelist");
                                } else {
                                    event.reply("Player " + args[1] + " could not be un-whitelisted. Either they are not whitelisted, or an error occurred");
                                }

                                if (modConfig.generalConfig.linkedWhitelist && !SystemUtils.hasPermission(event.getMember())) {
                                    UserTable userTable = new UserTable();
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
                                    }
                                }
                            }
                        }
                    }

                    if (args[0].equalsIgnoreCase("list") && (event.getMember().hasPermission(Permission.ADMINISTRATOR) || event.getMember().hasPermission(Permission.KICK_MEMBERS))) {
                        List<String> string = minecraftHelper.getWhitelistedPlayers();
                        event.reply("**Whitelisted Players:**\n\n" + ArrayUtils.toString(string));
                    }
                } else {
                    event.reply("Whitelisting is not enabled on your server");
                }
            } else {
                event.reply("Whitelisting is not enabled");
            }
        }
    }
}

