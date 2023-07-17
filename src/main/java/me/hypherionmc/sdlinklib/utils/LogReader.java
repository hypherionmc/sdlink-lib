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
package me.hypherionmc.sdlinklib.utils;

import me.hypherionmc.sdlinklib.discord.BotController;
import me.hypherionmc.sdlinklib.discord.DiscordMessage;
import me.hypherionmc.sdlinklib.discord.messages.MessageAuthor;
import me.hypherionmc.sdlinklib.discord.messages.MessageType;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static me.hypherionmc.sdlinklib.config.ConfigController.modConfig;

@Plugin(name = "SDLinkLogging", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
public class LogReader extends AbstractAppender {

    private static BotController botEngine;

    public static String logs = "";
    private long time;
    private Thread messageScheduler;
    private static boolean isDevEnv = false;

    protected LogReader(String name, Filter filter) {
        super(name, filter, null, true, new Property[0]);
    }

    @PluginFactory
    public static LogReader createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Filter") Filter filter) {
        return new LogReader(name, filter);
    }

    public static void init(BotController botController, boolean isDev) {
        botEngine = botController;
        isDevEnv = isDev;
        LogReader da = LogReader.createAppender("SDLinkLogging", null);
        ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).addAppender(da);
        da.start();
    }

    @Override
    public void append(LogEvent event) {
        if (botEngine.isBotReady()) {
            if (event.getLevel().intLevel() < Level.DEBUG.intLevel()) {
                logs += formatMessage(event) + "\n";
                scheduleMessage();
            }
        }
    }

    private String formatMessage(LogEvent event) {
        String devString = "**[" + formatTime(event.getTimeMillis()) + "]** " +
                "**[" + event.getThreadName() + "/" + event.getLevel().name() + "]** " +
                "**(" + event.getLoggerName().substring(event.getLoggerName().lastIndexOf(".") + 1) + ")** *" +
                event.getMessage().getFormattedMessage() + "*";

        String prodString = "**[" + formatTime(event.getTimeMillis()) + "]** " +
                "**[" + event.getThreadName() + "/" + event.getLevel().name() + "]** *" +
                event.getMessage().getFormattedMessage() + "*";

        return isDevEnv ? devString : prodString;
    }

    private String formatTime(long millis) {
        DateFormat obj = new SimpleDateFormat("HH:mm:ss");
        Date res = new Date(millis);
        return obj.format(res);
    }

    private void scheduleMessage() {
        time = System.currentTimeMillis();
        if (messageScheduler == null || !messageScheduler.isAlive()) {
            messageScheduler = new Thread(() -> {
                while (true) {
                    if (!botEngine.isBotReady())
                        return;
                    if (System.currentTimeMillis() - time > 250) {
                        if (logs.length() > 2000) {
                            logs = logs.substring(0, 1999);
                        }

                        logs = logs.replaceAll("\\b(?:(?:2(?:[0-4][0-9]|5[0-5])|[0-1]?[0-9]?[0-9])\\.){3}(?:(?:2([0-4][0-9]|5[0-5])|[0-1]?[0-9]?[0-9]))\\b", "[REDACTED]");

                        DiscordMessage message = new DiscordMessage.Builder(botEngine, MessageType.CONSOLE).withMessage(logs).withAuthor(MessageAuthor.SERVER).build();

                        if (modConfig.messageConfig.sendConsoleMessages) {
                            message.sendMessage();
                        }

                        logs = "";
                        break;
                    }
                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        if (modConfig.generalConfig.debugging) {
                            BotController.LOGGER.error("Failed to send console message: {}", e.getMessage());
                        }
                    }
                }
            });
            messageScheduler.start();
        }
    }
}
