package me.hypherionmc.sdlinklib.utils;

import club.minnced.discord.webhook.WebhookClientBuilder;

/**
 * @author HypherionSA
 * @date 09/10/2022
 */
public class SDWebhookClient extends WebhookClientBuilder {

    private final String url;

    public SDWebhookClient(String url) {
        super(url);
        this.url = url;
        this.setThreadFactory((job) -> {
            Thread thread = new Thread(job);
            thread.setName("Webhook Thread");
            thread.setDaemon(true);
            return thread;
        });
        this.setWait(true);
    }

}
