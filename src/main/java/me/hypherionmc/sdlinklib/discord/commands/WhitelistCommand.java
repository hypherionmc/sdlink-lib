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
                    "This command allows players to whitelist/un-whitelist themselves on your server. Admins/Members with Kick Members permission can un-whitelist anyone\r\n\r\n" +
                    "Command Usage:\r\n" +
                    "`whitelist add playername` -> Add a player to the whitelist\r\n" +
                    "`whitelist remove playername` -> Remove a player from the whitelist\r\n" +
                    "`whitelist list` -> View whitelisted players (Admins/Moderators only)"
                    );

            event.reply(embedBuilder.build());

        } else {
            if (modConfig.general.whitelisting) {
                String[] args = event.getArgs().split(" ");

                if (args[0].equalsIgnoreCase("add")) {
                    if (modConfig.general.adminWhitelistOnly && (!event.getMember().hasPermission(Permission.ADMINISTRATOR) || !event.getMember().hasPermission(Permission.KICK_MEMBERS))) {
                        event.reply("Sorry, only Admins can whitelist players");
                    } else {
                        Pair<String, String> player = PlayerUtils.fetchUUID(args[1]);

                        if (player.getLeft().isEmpty() || player.getRight().isEmpty()) {
                            event.reply("Failed to fetch info for player " + args[1]);
                        } else {
                            whitelistTable = new WhitelistTable();
                            whitelistTable.username = player.getLeft();
                            whitelistTable.UUID = player.getRight();
                            whitelistTable.discordID = event.getAuthor().getIdLong();

                            List<WhitelistTable> tables = whitelistTable.fetchAll("discordID = '" + event.getAuthor().getIdLong() + "'");
                            if (tables.isEmpty()) {
                                whitelistTable.insert();
                                event.reply(name + " has been whitelisted");
                            } else {
                                event.reply(name + " has already been whitelisted");
                            }
                        }
                    }
                }

                if (args[0].equalsIgnoreCase("remove")) {
                    whitelistTable = new WhitelistTable();
                    whitelistTable.fetch("discordID = '" + event.getAuthor().getIdLong() + "'");

                    if ((whitelistTable.username == null || !whitelistTable.username.equalsIgnoreCase(args[1])) && (!event.getMember().hasPermission(Permission.ADMINISTRATOR) || !event.getMember().hasPermission(Permission.KICK_MEMBERS))) {
                        event.reply("Sorry, you cannot un-whitelist this player");
                    } else {
                        Pair<String, String> player = PlayerUtils.fetchUUID(args[1]);

                        if (player.getLeft().isEmpty() || player.getRight().isEmpty()) {
                            event.reply("Failed to fetch info for player " + args[1]);
                        } else {
                            if (whitelistTable.delete()) {
                                event.reply(whitelistTable.username + " has been removed from the whitelist");
                            }
                        }
                    }
                }

                if (args[0].equalsIgnoreCase("list") && (event.getMember().hasPermission(Permission.ADMINISTRATOR) || event.getMember().hasPermission(Permission.KICK_MEMBERS))) {
                    whitelistTable = new WhitelistTable();
                    List<WhitelistTable> whitelistTables = whitelistTable.fetchAll();

                    StringBuilder builder = new StringBuilder();
                    whitelistTables.forEach(user -> {
                        builder.append(user.username).append("\r\n");
                    });
                    event.reply("**Whitelisted Players:**\n\n" + builder.toString());
                }
            } else {
                event.reply("Whitelisting is not enabled");
            }
        }
    }
}
