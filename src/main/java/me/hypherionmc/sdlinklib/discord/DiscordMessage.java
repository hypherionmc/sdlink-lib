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

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import me.hypherionmc.sdlinklib.config.configobjects.MessageChannelsConfig;
import me.hypherionmc.sdlinklib.discord.messages.MessageAuthor;
import me.hypherionmc.sdlinklib.discord.messages.MessageType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import org.apache.commons.lang3.tuple.Pair;

import static me.hypherionmc.sdlinklib.config.ConfigController.modConfig;

/**
 * @author HypherionSA
 * A helper class to format messages correctly for sending to discord
 */
public final class DiscordMessage {

    // Variables
    private final BotController controller;
    //private final MessageDestination destination;
    private final MessageType messageType;

    private final MessageAuthor messageAuthor;
    private final String message;

    private DiscordMessage(Builder builder) {
        controller = builder.controller;
        messageType = builder.messageType;
        messageAuthor = builder.author;
        message = builder.message;
    }

    /**
     * Builder class to construct a Discord Message
     */
    public static final class Builder {

        // Required Variables
        private final BotController controller;
        //private final MessageDestination destination;
        private final MessageType messageType;

        // Optional Variables
        private MessageAuthor author;
        private String message;

        public Builder(BotController controller, MessageType messageType) {
            this.controller = controller;
            this.messageType = messageType;
        }

        public Builder withAuthor(MessageAuthor author) {
            this.author = author;

            if (author.getUsername().equalsIgnoreCase("server")) {
                this.author = MessageAuthor.SERVER;
            }

            return this;
        }

        public Builder withMessage(String message) {
            message = message.replace("<@", "");
            message = message.replace("@everyone", "");
            message = message.replace("@here", "");
            this.message = message;
            return this;
        }

        public DiscordMessage build() {
            if (this.author == null) {
                this.author = MessageAuthor.SERVER;
            }

            if (this.message == null) {
                this.message = "";
            } else {
                if (messageType == MessageType.CHAT) {
                    if (modConfig.messageConfig.chat.contains("%player%")) {
                        message = modConfig.messageConfig.chat.replace("%player%", author.getUsername()).replace("%message%", message);
                    } else {
                        if (!modConfig.webhookConfig.enabled || controller.getChatWebhookClient() == null) {
                            message = author.getUsername() + ": " + message;
                        }
                    }
                }
            }

            return new DiscordMessage(this);
        }

    }

    public void sendMessage() {
        if (controller.get_jda() == null)
            return;

        try {
            if (messageType == MessageType.CONSOLE) {
                sendConsoleMessage();
            } else {
                if (modConfig.webhookConfig.enabled) {
                    sendWebhookMessage();
                } else {
                    sendStandardMessage();
                }
            }
        } catch (Exception e) {
            BotController.LOGGER.error("Failed to send Discord Message", e);
        }
    }

    private void sendWebhookMessage() {
        WebhookMessageBuilder builder = new WebhookMessageBuilder();

        builder.setUsername(this.messageAuthor.getUsername());

        if (!this.messageAuthor.getAvatar().isEmpty()) {
            builder.setAvatarUrl(this.messageAuthor.getAvatar());
        }

        Pair<WebhookClient, Boolean> webhook = getDestinationWebhook();

        if (webhook.getLeft() == null)
            return;

        if (webhook.getRight()) {
            EmbedBuilder eb = getEmbed(false);
            WebhookEmbed web = WebhookEmbedBuilder.fromJDA(eb.build()).build();
            builder.addEmbeds(web);
        } else {
            builder.setContent(message);
        }

        webhook.getLeft().send(builder.build());
    }

    private void sendStandardMessage() {
        EmbedBuilder builder = getEmbed(true);

        Pair<StandardGuildMessageChannel, Boolean> channel = getDestinationChannel();

        if (channel.getLeft() != null) {
            if (channel.getRight()) {
                channel.getLeft().sendMessageEmbeds(builder.build()).queue();
            } else {
                channel.getLeft().sendMessage(message).queue();
            }
        }
    }

    private EmbedBuilder getEmbed(boolean withAuthor) {
        String user = this.messageAuthor.getUsername();
        String finalMsg = message;

        if (messageType == MessageType.CHAT && modConfig.messageConfig.chat.contains("%player%")) {
            user = MessageAuthor.SERVER.getUsername();
        }

        if (messageType == MessageType.CHAT && !modConfig.messageConfig.chat.contains("%player%")) {
            finalMsg = message.replace(messageAuthor.getUsername() + ": ", "");
        }

        EmbedBuilder builder = new EmbedBuilder();

        if (withAuthor) {
            builder.setAuthor(
                user,
                null,
                this.messageAuthor.getAvatar().isEmpty() ? null :  this.messageAuthor.getAvatar()
            );
        }

        builder.setDescription(finalMsg);
        return builder;
    }

    private void sendConsoleMessage() {
        try {
            if (controller.isBotReady() && modConfig.messageConfig.sendConsoleMessages) {
                StandardGuildMessageChannel channel = controller.get_jda().getChannelById(StandardGuildMessageChannel.class, modConfig.channelConfig.consoleChannelID);
                if (channel != null) {
                    channel.sendMessage(message).queue();
                }
            }
        } catch (Exception e) {
            if (modConfig != null && modConfig.generalConfig.debugging) {
                e.printStackTrace();
            }
        }
    }

    private Pair<StandardGuildMessageChannel, Boolean> getDestinationChannel() {
        StandardGuildMessageChannel channel = controller.get_jda().getChannelById(StandardGuildMessageChannel.class, modConfig.channelConfig.channelID);
        StandardGuildMessageChannel eventChannel = controller.get_jda().getChannelById(StandardGuildMessageChannel.class, modConfig.channelConfig.eventsID);
        StandardGuildMessageChannel consoleChannel = controller.get_jda().getChannelById(StandardGuildMessageChannel.class, modConfig.channelConfig.consoleChannelID);

        if (messageType == MessageType.CHAT) {
            MessageChannelsConfig.DestinationObject object = modConfig.messageDestinations.chat;

            if (object.channel.isEvent() && eventChannel != null) {
                return Pair.of(eventChannel, object.useEmbed);
            }
            if (object.channel.isConsole() && consoleChannel != null) {
                return Pair.of(consoleChannel, object.useEmbed);
            }

            return Pair.of(channel, object.useEmbed);
        }

        if (messageType == MessageType.START_STOP) {
            MessageChannelsConfig.DestinationObject object = modConfig.messageDestinations.startStop;

            if (object.channel.isEvent() && eventChannel != null) {
                return Pair.of(eventChannel, object.useEmbed);
            }
            if (object.channel.isConsole() && consoleChannel != null) {
                return Pair.of(consoleChannel, object.useEmbed);
            }

            return Pair.of(channel, object.useEmbed);
        }

        if (messageType == MessageType.JOIN_LEAVE) {
            MessageChannelsConfig.DestinationObject object = modConfig.messageDestinations.joinLeave;

            if (object.channel.isEvent() && eventChannel != null) {
                return Pair.of(eventChannel, object.useEmbed);
            }
            if (object.channel.isConsole() && consoleChannel != null) {
                return Pair.of(consoleChannel, object.useEmbed);
            }

            return Pair.of(channel, object.useEmbed);
        }

        if (messageType == MessageType.ADVANCEMENT) {
            MessageChannelsConfig.DestinationObject object = modConfig.messageDestinations.advancements;

            if (object.channel.isEvent() && eventChannel != null) {
                return Pair.of(eventChannel, object.useEmbed);
            }
            if (object.channel.isConsole() && consoleChannel != null) {
                return Pair.of(consoleChannel, object.useEmbed);
            }

            return Pair.of(channel, object.useEmbed);
        }

        if (messageType == MessageType.DEATH) {
            MessageChannelsConfig.DestinationObject object = modConfig.messageDestinations.death;

            if (object.channel.isEvent() && eventChannel != null) {
                return Pair.of(eventChannel, object.useEmbed);
            }
            if (object.channel.isConsole() && consoleChannel != null) {
                return Pair.of(consoleChannel, object.useEmbed);
            }

            return Pair.of(channel, object.useEmbed);
        }

        if (messageType == MessageType.COMMAND) {
            MessageChannelsConfig.DestinationObject object = modConfig.messageDestinations.commands;

            if (object.channel.isEvent() && eventChannel != null) {
                return Pair.of(eventChannel, object.useEmbed);
            }
            if (object.channel.isConsole() && consoleChannel != null) {
                return Pair.of(consoleChannel, object.useEmbed);
            }

            return Pair.of(channel, object.useEmbed);
        }

        return Pair.of(null, false);
    }

    private Pair<WebhookClient, Boolean> getDestinationWebhook() {
        WebhookClient channel = controller.getChatWebhookClient();
        WebhookClient eventChannel = controller.getEventWebhookClient();
        WebhookClient consoleChannel = controller.getConsoleWebhookClient();

        if (messageType == MessageType.CHAT) {
            MessageChannelsConfig.DestinationObject object = modConfig.messageDestinations.chat;

            if (object.channel.isEvent() && eventChannel != null) {
                return Pair.of(eventChannel, object.useEmbed);
            }
            if (object.channel.isConsole() && consoleChannel != null) {
                return Pair.of(consoleChannel, object.useEmbed);
            }

            return Pair.of(channel, object.useEmbed);
        }

        if (messageType == MessageType.START_STOP) {
            MessageChannelsConfig.DestinationObject object = modConfig.messageDestinations.startStop;

            if (object.channel.isEvent() && eventChannel != null) {
                return Pair.of(eventChannel, object.useEmbed);
            }
            if (object.channel.isConsole() && consoleChannel != null) {
                return Pair.of(consoleChannel, object.useEmbed);
            }

            return Pair.of(channel, object.useEmbed);
        }

        if (messageType == MessageType.JOIN_LEAVE) {
            MessageChannelsConfig.DestinationObject object = modConfig.messageDestinations.joinLeave;

            if (object.channel.isEvent() && eventChannel != null) {
                return Pair.of(eventChannel, object.useEmbed);
            }
            if (object.channel.isConsole() && consoleChannel != null) {
                return Pair.of(consoleChannel, object.useEmbed);
            }

            return Pair.of(channel, object.useEmbed);
        }

        if (messageType == MessageType.ADVANCEMENT) {
            MessageChannelsConfig.DestinationObject object = modConfig.messageDestinations.advancements;

            if (object.channel.isEvent() && eventChannel != null) {
                return Pair.of(eventChannel, object.useEmbed);
            }
            if (object.channel.isConsole() && consoleChannel != null) {
                return Pair.of(consoleChannel, object.useEmbed);
            }

            return Pair.of(channel, object.useEmbed);
        }

        if (messageType == MessageType.DEATH) {
            MessageChannelsConfig.DestinationObject object = modConfig.messageDestinations.death;

            if (object.channel.isEvent() && eventChannel != null) {
                return Pair.of(eventChannel, object.useEmbed);
            }
            if (object.channel.isConsole() && consoleChannel != null) {
                return Pair.of(consoleChannel, object.useEmbed);
            }

            return Pair.of(channel, object.useEmbed);
        }

        if (messageType == MessageType.COMMAND) {
            MessageChannelsConfig.DestinationObject object = modConfig.messageDestinations.commands;

            if (object.channel.isEvent() && eventChannel != null) {
                return Pair.of(eventChannel, object.useEmbed);
            }
            if (object.channel.isConsole() && consoleChannel != null) {
                return Pair.of(consoleChannel, object.useEmbed);
            }

            return Pair.of(channel, object.useEmbed);
        }

        return Pair.of(null, false);
    }

}
