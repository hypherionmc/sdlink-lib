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
package me.hypherionmc.sdlinklib.discord;

import me.hypherionmc.sdlinklib.config.ModConfig;
import me.hypherionmc.sdlinklib.discord.slashcommands.ServerStatusSlashCommand;
import me.hypherionmc.sdlinklib.services.helpers.IMinecraftHelper;
import me.hypherionmc.sdlinklib.utils.SystemUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

import static me.hypherionmc.sdlinklib.discord.BotController.threadPool;

/**
 * @author HypherionSA
 * Discord Event Listener. Handles things like sending messages back to minecraft,
 * updating the bot status and channel topics
 */

public class DiscordEventHandler extends ListenerAdapter {

    private final IMinecraftHelper minecraftHelper;

    public DiscordEventHandler(BotController controller) {
        this.minecraftHelper = controller.getMinecraftHelper();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        try {
            if (event.getChannel().getIdLong() != ModConfig.INSTANCE.channelConfig.channelID)
                return;

            if (event.isWebhookMessage())
                return;

            if (event.getAuthor() == event.getJDA().getSelfUser())
                return;

            if (event.getAuthor().isBot() && ModConfig.INSTANCE.chatConfig.ignoreBots)
                return;

            if (ModConfig.INSTANCE.generalConfig.debugging) {
                BotController.LOGGER.info("Sending Message from {}: {}", event.getAuthor().getName(), event.getMessage().getContentStripped());
            }
            minecraftHelper.discordMessageEvent(event.getMember().getEffectiveName(), event.getMessage().getContentRaw());
        } catch (Exception e) {
            if (ModConfig.INSTANCE.generalConfig.debugging) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        if (event.getJDA().getStatus() == JDA.Status.CONNECTED) {
            BotController.LOGGER.info("Successfully connected to discord");

            threadPool.scheduleAtFixedRate(() -> {
                try {
                    if (event.getJDA().getStatus() == JDA.Status.CONNECTED) {
                        Activity act = Activity.of(ModConfig.INSTANCE.botConfig.botStatusType, ModConfig.INSTANCE.botConfig.botStatus
                                .replace("%players%", String.valueOf(minecraftHelper.getOnlinePlayerCount()))
                                .replace("%maxplayers%", String.valueOf(minecraftHelper.getMaxPlayerCount())));

                        if (ModConfig.INSTANCE.botConfig.botStatusType == Activity.ActivityType.STREAMING) {
                            act = Activity.of(ModConfig.INSTANCE.botConfig.botStatusType, ModConfig.INSTANCE.botConfig.botStatus
                                            .replace("%players%", String.valueOf(minecraftHelper.getOnlinePlayerCount()))
                                            .replace("%maxplayers%", String.valueOf(minecraftHelper.getMaxPlayerCount())),
                                    ModConfig.INSTANCE.botConfig.botStatusStreamingURL);
                        }

                        event.getJDA().getPresence().setActivity(act);
                    }
                } catch (Exception e) {
                    if (ModConfig.INSTANCE.generalConfig.debugging) {
                        BotController.LOGGER.info(e.getMessage());
                    }
                }
            }, ModConfig.INSTANCE.botConfig.activityUpdateInterval, ModConfig.INSTANCE.botConfig.activityUpdateInterval, TimeUnit.SECONDS);

            threadPool.scheduleAtFixedRate(() -> {
                if (ModConfig.INSTANCE.botConfig.doTopicUpdates) {
                    try {
                        if (event.getJDA().getStatus() == JDA.Status.CONNECTED && (ModConfig.INSTANCE.botConfig.channelTopic != null && !ModConfig.INSTANCE.botConfig.channelTopic.isEmpty())) {
                            TextChannel channel = event.getJDA().getTextChannelById(ModConfig.INSTANCE.channelConfig.channelID);
                            if (channel != null) {
                                String topic = ModConfig.INSTANCE.botConfig.channelTopic
                                        .replace("%players%", String.valueOf(minecraftHelper.getOnlinePlayerCount()))
                                        .replace("%maxplayers%", String.valueOf(minecraftHelper.getMaxPlayerCount()))
                                        .replace("%uptime%", SystemUtils.secondsToTimestamp(minecraftHelper.getServerUptime()));
                                channel.getManager().setTopic(topic).queue();
                            }
                        }
                    } catch (Exception e) {
                        if (ModConfig.INSTANCE.generalConfig.debugging) {
                            BotController.LOGGER.info(e.getMessage());
                        }
                    }
                }
            }, 6, 6, TimeUnit.MINUTES);
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().equals("refreshbtn")) {
            event.getMessage().editMessageEmbeds(ServerStatusSlashCommand.runStatusCommand(minecraftHelper)).queue();
            event.reply("Success!").setEphemeral(true).queue();
        }
    }

    public void shutdown() {
        threadPool.shutdownNow();
    }
}
