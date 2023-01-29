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
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

import static me.hypherionmc.sdlinklib.config.ConfigController.modConfig;
import static me.hypherionmc.sdlinklib.discord.commands.UnLinkCommand.pattern;

public class WhitelistCommand extends Command {

    private final BotController controller;
    private WhitelistTable whitelistTable = new WhitelistTable();

    private IMinecraftHelper minecraftHelper;

    public WhitelistCommand(BotController controller) {
        this.controller = controller;
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
            if (!modConfig.generalConfig.whitelisting) {
                event.reply("Whitelisting is disabled");
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
                MinecraftPlayer player = MinecraftPlayer.standard(args[1]);

                if (!player.isValid()) {
                    event.reply("Could not retrieve minecraft account for player " + args[1]);
                    return;
                }

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

                    if (modConfig.generalConfig.linkedWhitelist && !SystemUtils.hasPermission(controller, event.getMember())) {
                        String nickName = (event.getMember().getNickname() == null || event.getMember().getNickname().isEmpty()) ? event.getAuthor().getName() : event.getMember().getNickname();
                        nickName = nickName + " [MC: " + args[1] + "]";
                        player.linkAccount(nickName, event.getMember());
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

                MinecraftPlayer player = MinecraftPlayer.standard(args[1]);

                if (!player.isValid()) {
                    event.reply("Could not retrieve minecraft account for player " + args[1]);
                    return;
                }

                if (!minecraftHelper.isPlayerWhitelisted(player)) {
                    event.reply("Player " + args[1] + " is not whitelisted on this server");
                    return;
                }

                if (minecraftHelper.unWhitelistPlayer(player)) {
                    whitelistTable.delete();
                    event.reply("Player " + args[1] + " has been removed from the whitelist");

                    if (modConfig.generalConfig.linkedWhitelist && !SystemUtils.hasPermission(controller, event.getMember())) {
                        UserTable userTable = new UserTable();
                        List<UserTable> tables = userTable.fetchAll("discordID = '" + event.getAuthor().getIdLong() + "'");

                        if (!tables.isEmpty()) {
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
