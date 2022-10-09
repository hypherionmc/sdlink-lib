package me.hypherionmc.sdlinklib.discord.slashcommands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import me.hypherionmc.jqlite.data.SQLiteTable;
import me.hypherionmc.sdlinklib.database.UserTable;
import me.hypherionmc.sdlinklib.discord.BotController;
import me.hypherionmc.sdlinklib.utils.PlayerUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static me.hypherionmc.sdlinklib.config.ConfigController.modConfig;

/**
 * @author HypherionSA
 * @date 09/10/2022
 */
public class LinkSlashCommand extends SlashCommand {

    private UserTable userTable = new UserTable();
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
        userTable = new UserTable();
        String mcName = event.getOption("mcname") != null ? event.getOption("mcname").getAsString() : "";

        if (mcName.isEmpty()) {
            event.reply("You need to supply your Minecraft username").setEphemeral(true).queue();
        } else {
            Pair<String, String> player = PlayerUtils.fetchUUID(mcName);
            if (player.getLeft().isEmpty() || player.getRight().isEmpty()) {
                event.reply("Failed to fetch info for player " + mcName).setEphemeral(true).queue();
            } else {
                userTable.username = player.getLeft();
                userTable.UUID = player.getRight();
                userTable.discordID = event.getMember().getIdLong();

                List<UserTable> tables = userTable.fetchAll("discordID = '" + event.getMember().getIdLong() + "'");
                if (tables.isEmpty()) {
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
                event.reply("Your discord and MC account have been linked").setEphemeral(true).queue();
            }
        }
    }

    public static class RemoveLinkSlashCommand extends SlashCommand {
        private UserTable userTable = new UserTable();
        private final BotController controller;

        final Pattern pattern = Pattern.compile("\\[MC: [a-zA-Z]+]\\s+", Pattern.CASE_INSENSITIVE);

        public RemoveLinkSlashCommand(BotController controller) {
            this.controller = controller;

            this.guildOnly = true;
            this.name = "unlink";
            this.help = "Unlink your Minecraft and Discord account";
            this.botPermissions = new Permission[] { Permission.NICKNAME_MANAGE };

            this.options = Collections.singletonList(new OptionData(OptionType.STRING, "mcname", "Your Minecraft Username").setRequired(true));
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            userTable = new UserTable();
            String mcName = event.getOption("mcname") != null ? event.getOption("mcname").getAsString() : "";

            if (mcName.isEmpty()) {
                event.reply("You need to supply your Minecraft username").setEphemeral(true).queue();
            } else {
                Pair<String, String> player = PlayerUtils.fetchUUID(mcName);
                if (player.getLeft().isEmpty() || player.getRight().isEmpty()) {
                    event.reply("Failed to fetch info for player " + mcName).setEphemeral(true).queue();
                } else {
                    userTable = new UserTable();
                    List<UserTable> tables = userTable.fetchAll("discordID = '" + event.getMember().getIdLong() + "'");

                    if (tables.isEmpty()) {
                        event.reply("Your discord account does not appear to be linked to a minecraft account").setEphemeral(true).queue();
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
                        event.reply("Your discord and MC account have been unlinked").setEphemeral(true).queue();
                    }
                }
            }
        }
    }

 }
