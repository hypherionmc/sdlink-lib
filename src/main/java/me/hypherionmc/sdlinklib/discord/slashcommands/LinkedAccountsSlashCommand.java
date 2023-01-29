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

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import me.hypherionmc.sdlinklib.database.UserTable;
import me.hypherionmc.sdlinklib.discord.BotController;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.List;

/**
 * @author HypherionSA
 * @date 09/10/2022
 */
public class LinkedAccountsSlashCommand extends BaseSlashCommand {

    private UserTable table = new UserTable();

    public LinkedAccountsSlashCommand(BotController controller) {
        super(controller, true);
        this.name = "linkedacc";
        this.help = "View a list of linked Discord and MC accounts";
        this.guildOnly = true;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        table = new UserTable();
        List<UserTable> tables = table.fetchAll();

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Linked Discord and Minecraft Accounts");

        StringBuilder content = new StringBuilder();

        tables.forEach(user -> {
            if (event.getGuild().getMemberById(user.discordID) != null) {
                content.append(event.getGuild().getMemberById(user.discordID).getAsMention()).append(" - MC: ").append(user.username).append("\r\n");
            }
        });

        builder.setDescription(content.toString());
        event.replyEmbeds(builder.build()).setEphemeral(true).queue();
    }

}
