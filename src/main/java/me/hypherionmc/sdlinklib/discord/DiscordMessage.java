package me.hypherionmc.sdlinklib.discord;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import me.hypherionmc.sdlinklib.discord.messages.MessageAuthor;
import me.hypherionmc.sdlinklib.discord.messages.MessageDestination;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;

import static me.hypherionmc.sdlinklib.config.ConfigController.modConfig;

public final class DiscordMessage {

    private final BotController controller;
    private final MessageDestination destination;
    private final MessageAuthor messageAuthor;
    private final String message;

    private DiscordMessage(Builder builder) {
        controller = builder.controller;
        destination = builder.destination;
        messageAuthor = builder.author;
        message = builder.message;
    }

    public static final class Builder {
        private final BotController controller;
        private final MessageDestination destination;

        private MessageAuthor author;
        private String message;

        public Builder(BotController controller, MessageDestination destination) {
            this.controller = controller;
            this.destination = destination;
        }

        public Builder withAuthor(MessageAuthor author) {
            this.author = author;
            return this;
        }

        public Builder withMessage(String message) {
            message = message.replace("<@", "");
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
                if (destination.isChat()) {
                    if (modConfig.messageConfig.chat.contains("%player%")) {
                        message = modConfig.messageConfig.chat.replace("%player%", author.getUsername()).replace("%message%", message);
                    } else {
                        message = author.getUsername() + ": " + message;
                    }
                }
            }

            return new DiscordMessage(this);
        }

    }

    public void sendMessage() {
        if (destination.isConsole()) {
            sendConsoleMessage();
        } else {
            if (modConfig.webhookConfig.enabled) {
                sendWebhookMessage();
            } else {
                sendStandardMessage();
            }
        }
    }

    private void sendWebhookMessage() {
        WebhookMessageBuilder builder = new WebhookMessageBuilder();

        builder.setUsername(this.messageAuthor.getUsername());

        if (!this.messageAuthor.getAvatar().isEmpty()) {
            builder.setAvatarUrl(this.messageAuthor.getAvatar());
        }

        if ((destination.isChat() && modConfig.webhookConfig.chatEmbeds) || (destination.isServer() && modConfig.webhookConfig.eventEmbeds)) {
            EmbedBuilder eb = getEmbed(false);
            WebhookEmbed web = WebhookEmbedBuilder.fromJDA(eb.build()).build();
            builder.addEmbeds(web);
        } else {
            builder.setContent(message);
        }

        if (destination.isChat()) {
            if (controller.getChatWebhookClient() != null) {
                controller.getChatWebhookClient().send(builder.build());
            }
        } else {
            if (controller.getEventWebhookClient() != null) {
                controller.getEventWebhookClient().send(builder.build());
            } else {
                controller.getChatWebhookClient().send(builder.build());
            }
        }
    }

    private void sendStandardMessage() {
        EmbedBuilder builder = getEmbed(true);

        StandardGuildMessageChannel channel = controller.get_jda().getChannelById(StandardGuildMessageChannel.class,
                destination.isChat() ? modConfig.channelConfig.channelID : (modConfig.channelConfig.eventsID != 0 ? modConfig.channelConfig.eventsID : modConfig.channelConfig.channelID)
        );

        if (channel != null) {
            if ((destination.isChat() && modConfig.channelConfig.chatEmbeds) || (destination.isServer() && modConfig.channelConfig.eventEmbeds)) {
                channel.sendMessageEmbeds(builder.build()).complete();
            } else {
                channel.sendMessage(message).complete();
            }
        }
    }

    private EmbedBuilder getEmbed(boolean withAuthor) {
        String user = this.messageAuthor.getUsername();

        if (destination.isChat() && modConfig.messageConfig.chat.contains("%player%")) {
            user = MessageAuthor.SERVER.getUsername();
        }

        EmbedBuilder builder = new EmbedBuilder();

        if (withAuthor) {
            builder.setAuthor(
                user,
                null,
                this.messageAuthor.getAvatar().isEmpty() ? null :  this.messageAuthor.getAvatar()
            );
        }

        builder.setDescription(message);
        return builder;
    }

    private void sendConsoleMessage() {
        if (controller.isBotReady() && modConfig.messageConfig.sendConsoleMessages) {
            StandardGuildMessageChannel channel = controller.get_jda().getChannelById(StandardGuildMessageChannel.class, modConfig.channelConfig.consoleChannelID);
            if (channel != null) {
                channel.sendMessage(message).queue();
            }
        }
    }

}
