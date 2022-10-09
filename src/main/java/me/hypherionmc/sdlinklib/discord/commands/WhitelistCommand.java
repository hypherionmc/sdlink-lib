package me.hypherionmc.sdlinklib.discord.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.hypherionmc.sdlinklib.database.WhitelistTable;
import me.hypherionmc.sdlinklib.discord.BotController;
import me.hypherionmc.sdlinklib.services.helpers.IMinecraftHelper;
import me.hypherionmc.sdlinklib.utils.PlayerUtils;
import me.hypherionmc.sdlinklib.utils.SystemUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

import static me.hypherionmc.sdlinklib.config.ConfigController.modConfig;

public class WhitelistCommand extends Command {

    private WhitelistTable whitelistTable = new WhitelistTable();

    private IMinecraftHelper minecraftHelper;

    public WhitelistCommand(BotController controller) {
        this.minecraftHelper = controller.getMinecraftHelper();

        this.name = "whitelist";
        this.help = "Control Whitelisting if enabled";
    }

    @Override
    protected void execute(CommandEvent event) {

        if (event.getArgs().isEmpty()) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Whitelist Command Help");
            embedBuilder.setDescription(
                    "This command allows players to whitelist/un-whitelist themselves on your server. Admins/Members with Kick Members permission can un-whitelist anyone\r\n\r\n" +
                            "Command Usage:\r\n" +
                            "`whitelist add playername` -> Add a player to the whitelist\r\n" +
                            "`whitelist remove playername` -> Remove a player from the whitelist\r\n" +
                            "`whitelist list` -> View whitelisted players (Admins/Moderators only)"
            );

            event.reply(embedBuilder.build());
        } else {
            if (modConfig.generalConfig.whitelisting) {
                if (minecraftHelper.isWhitelistingEnabled()) {
                    if (modConfig.generalConfig.adminWhitelistOnly && !SystemUtils.hasPermission(event.getMember())) {
                        event.reply("Sorry, only Admins/Members with Kick Permissions can whitelist players");
                    } else {
                        String[] args = event.getArgs().split(" ");

                        if (args[0].equalsIgnoreCase("add")) {
                            Pair<String, String> player = PlayerUtils.fetchUUID(args[1]);

                            if (player.getLeft().isEmpty() || player.getRight().isEmpty()) {
                                event.reply("Failed to fetch info for player " + args[1]);
                            } else {
                                if (minecraftHelper.isPlayerWhitelisted(player.getLeft(), PlayerUtils.mojangIdToUUID(player.getRight()))) {
                                    event.reply("Player " + player.getLeft() + " is already whitelisted on this server");
                                } else {
                                    whitelistTable = new WhitelistTable();
                                    List<WhitelistTable> tables = whitelistTable.fetchAll("discordID = '" + event.getAuthor().getIdLong() + "'");
                                    if (!tables.isEmpty() && !SystemUtils.hasPermission(event.getMember())) {
                                        event.reply("You have already whitelisted a player on this server! Only one whitelist per player is allowed. Please ask an admin for assistance");
                                    } else {
                                        whitelistTable.username = player.getLeft();
                                        whitelistTable.UUID = player.getRight();
                                        whitelistTable.discordID = event.getAuthor().getIdLong();
                                        if (minecraftHelper.whitelistPlayer(player.getLeft(), PlayerUtils.mojangIdToUUID(player.getRight())) && whitelistTable.insert()) {
                                            event.reply("Player " + player.getLeft() + " is now whitelisted!");
                                        } else {
                                            event.reply("Player " + player.getLeft() + " could not be whitelisted. Either they are already whitelisted, or an error occurred");
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
                                Pair<String, String> player = PlayerUtils.fetchUUID(args[1]);

                                if (player.getLeft().isEmpty() || player.getRight().isEmpty()) {
                                    event.reply("Failed to fetch info for player " + args[1]);
                                } else if (!minecraftHelper.isPlayerWhitelisted(player.getLeft(), PlayerUtils.mojangIdToUUID(player.getRight()))) {
                                    event.reply("Player " + player.getLeft() + " is not whitelisted on this server");
                                } else {
                                    if (minecraftHelper.unWhitelistPlayer(player.getLeft(), PlayerUtils.mojangIdToUUID(player.getRight()))) {
                                        whitelistTable.delete();
                                        event.reply("Player " + player.getLeft() + " has been removed from the whitelist");
                                    } else {
                                        event.reply("Player " + player.getLeft() + " could not be un-whitelisted. Either they are not whitelisted, or an error occurred");
                                    }
                                }
                            }
                        }

                        if (args[0].equalsIgnoreCase("list") && (event.getMember().hasPermission(Permission.ADMINISTRATOR) || event.getMember().hasPermission(Permission.KICK_MEMBERS))) {
                            List<String> string = minecraftHelper.getWhitelistedPlayers();
                            event.reply("**Whitelisted Players:**\n\n" + ArrayUtils.toString(string));
                        }
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
