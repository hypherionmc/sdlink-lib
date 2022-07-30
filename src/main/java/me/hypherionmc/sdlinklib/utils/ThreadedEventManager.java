package me.hypherionmc.sdlinklib.utils;

import me.hypherionmc.sdlinklib.discord.BotController;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.InterfacedEventManager;
import org.jetbrains.annotations.NotNull;

/**
 * @author HypherionSA
 * @date 30/07/2022
 */
public class ThreadedEventManager extends InterfacedEventManager {

    @Override
    public void handle(@NotNull GenericEvent event) {
        BotController.threadPool.submit(() -> super.handle(event));
    }
}
