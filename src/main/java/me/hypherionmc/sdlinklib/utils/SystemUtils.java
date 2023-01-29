package me.hypherionmc.sdlinklib.utils;

import me.hypherionmc.sdlinklib.discord.BotController;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SystemUtils {

    /* From https://stackoverflow.com/a/3758880 */
    public static String byteToHuman(long bytes) {
        long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absB < 1024) {
            return bytes + " B";
        }
        long value = absB;
        CharacterIterator ci = new StringCharacterIterator("KMGTPE");
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }
        value *= Long.signum(bytes);
        return String.format("%.1f %ciB", value / 1024.0, ci.current());
    }

    public static final List<Long> times = Arrays.asList(
            TimeUnit.DAYS.toMillis(365),
            TimeUnit.DAYS.toMillis(30),
            TimeUnit.DAYS.toMillis(1),
            TimeUnit.HOURS.toMillis(1),
            TimeUnit.MINUTES.toMillis(1),
            TimeUnit.SECONDS.toMillis(1));

    public static final List<String> timesString = Arrays.asList("year", "month", "day", "hour", "minute", "second");

    public static String toDuration(long duration) {
        StringBuffer res = new StringBuffer();
        for(int i=0; i < times.size(); i++) {
            Long current = times.get(i);
            long temp = duration / current;
            if(temp>0) {
                res.append(temp).append(" ").append(timesString.get(i) ).append(temp != 1 ? "s" : "");
                break;
            }
        }
        if("".equals(res.toString()))
            return "0 seconds ago";
        else
            return res.toString();
    }

    public static String secondsToTimestamp(long sec) {
        long seconds = sec % 60;
        long minutes = sec / 60;
        if (minutes >= 60) {
            long hours = minutes / 60;
            minutes %= 60;
            if( hours >= 24) {
                long days = hours / 24;
                return String.format("%d day(s), %02d hour(s), %02d minute(s), %02d second(s)", days, hours % 24, minutes, seconds);
            }
            return String.format("%02d hour(s), %02d minute(s), %02d second(s)", hours, minutes, seconds);
        }
        return String.format("00 hour(s), %02d minute(s), %02d second(s)", minutes, seconds);
    }

    public static boolean hasPermission(BotController controller, Member member) {
        if (!controller.getAdminRole().isEmpty()) {
            return member.getRoles().stream().anyMatch(r -> r.getName().equalsIgnoreCase(controller.getAdminRole()));
        }
        return member.hasPermission(Permission.ADMINISTRATOR) || member.hasPermission(Permission.KICK_MEMBERS);
    }
}
