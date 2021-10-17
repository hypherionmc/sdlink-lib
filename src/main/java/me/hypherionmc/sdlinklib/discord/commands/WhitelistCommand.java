package me.hypherionmc.sdlinklib.discord.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.hypherionmc.sdlinklib.config.ModConfig;
import me.hypherionmc.sdlinklib.database.WhitelistTable;
import me.hypherionmc.sdlinklib.discord.utils.MinecraftEventHandler;
import me.hypherionmc.sdlinklib.utils.PlayerUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class WhitelistCommand extends Command {

    private WhitelistTable whitelistTable;
    private MinecraftEventHandler eventHandler;
    private ModConfig modConfig;

    public WhitelistCommand(WhitelistTable whitelistTable, MinecraftEventHandler eventHandler, ModConfig modConfig) {
        this.whitelistTable = whitelistTable;
        this.eventHandler = eventHandler;
        this.modConfig = modConfig;

        this.name = "whitelist";
        this.help = "Control Whitelisting if enabled";
    }

    @Override
    protected void execute(CommandEvent event) {

        if (event.getArgs().isEmpty()) {

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Whitelist Command Help");
            embedBuilder.setDescription(
                    "This command allows players to whitelist/unwhitelist themselves on your server. Admins/Members with Kick Members permission can unwhitelist anyone\r\n\r\n" +
                    "Command Usage:\r\n" +
                    "`whitelist add playername` -> Add a player to the whitelist\r\n" +
                    "`whitelist remove playername` -> Remove a player from the whitelist\r\n" +
                    "`whitelist list` -> View whitelisted players (Admins/Moderators only)"
                    );

            event.reply(embedBuilder.build());

        } else {
            if (modConfig.general.whitelisting) {

                if (eventHandler.whiteListingEnabled()) {

                    String[] args = event.getArgs().split(" ");

                    if (args[0].equalsIgnoreCase("add")) {
                        Pair<String, String> player = PlayerUtils.fetchUUID(args[1]);

                        if (player.getLeft().isEmpty() || player.getRight().isEmpty()) {
                            event.reply("Failed to fetch info for player " + args[1]);
                        } else {
                            String response = eventHandler.whitelistPlayer(player.getLeft(), PlayerUtils.mojangIdToUUID(player.getRight()));

                            if (response.toLowerCase().contains("now whitelisted")) {
                                whitelistTable = new WhitelistTable();
                                whitelistTable.username = player.getLeft();
                                whitelistTable.UUID = player.getRight();
                                whitelistTable.discordID = event.getAuthor().getIdLong();

                                List<WhitelistTable> tables = whitelistTable.fetchAll("discordID = '" + event.getAuthor().getIdLong() + "'");
                                if (tables.isEmpty()) {
                                    whitelistTable.insert();
                                } else {
                                    whitelistTable.update();
                                }

                                event.reply(response);
                            } else {
                                event.reply(response);
                            }
                        }
                    }

                    if (args[0].equalsIgnoreCase("remove")) {
                        whitelistTable = new WhitelistTable();
                        whitelistTable.fetch("discordID = '" + event.getAuthor().getIdLong() + "'");

                        if ((whitelistTable.username == null || !whitelistTable.username.equalsIgnoreCase(args[1])) && (!event.getMember().hasPermission(Permission.ADMINISTRATOR) || !event.getMember().hasPermission(Permission.KICK_MEMBERS))) {
                            event.reply("Sorry, you cannot unwhitelist this player");
                        } else {
                            Pair<String, String> player = PlayerUtils.fetchUUID(args[1]);

                            if (player.getLeft().isEmpty() || player.getRight().isEmpty()) {
                                event.reply("Failed to fetch info for player " + args[1]);
                            } else {
                                String response = eventHandler.unWhitelistPlayer(player.getLeft(), PlayerUtils.mojangIdToUUID(player.getRight()));
                                if (response.toLowerCase().contains("has been removed from the whitelist")) {

                                    whitelistTable.delete();
                                    event.reply(response);
                                } else {
                                    event.reply(response);
                                }
                            }
                        }
                    }

                    if (args[0].equalsIgnoreCase("list") && (event.getMember().hasPermission(Permission.ADMINISTRATOR) || event.getMember().hasPermission(Permission.KICK_MEMBERS))) {
                        List<String> string = eventHandler.getWhitelistedPlayers();
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
