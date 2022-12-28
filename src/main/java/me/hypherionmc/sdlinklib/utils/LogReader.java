package me.hypherionmc.sdlinklib.utils;

import me.hypherionmc.sdlinklib.discord.BotController;
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
import org.apache.logging.log4j.core.time.Instant;

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

    protected LogReader(String name, Filter filter) {
        super(name, filter, null, true, Property.EMPTY_ARRAY);
    }

    @PluginFactory
    public static LogReader createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Filter") Filter filter) {
        return new LogReader(name, filter);
    }

    public static void init(BotController botController) {
        botEngine = botController;
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
        return "**[" + formatTime(event.getTimeMillis()) + "]** " +
                "**[" + event.getThreadName() + "/" + event.getLevel().name() + "]** " +
                "**(" + event.getLoggerName().substring(event.getLoggerName().lastIndexOf(".") + 1) + ")** *" +
                event.getMessage().getFormattedMessage() + "*";
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

                        botEngine.sendConsoleMessage("", logs);
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
