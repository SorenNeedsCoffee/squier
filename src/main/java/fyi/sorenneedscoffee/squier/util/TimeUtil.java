package fyi.sorenneedscoffee.squier.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtil {
    private static final ZoneId utc = ZoneId.of("UTC");
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static ZonedDateTime toUTC(LocalDateTime date, ZoneId zone) {
        ZonedDateTime from = date.atZone(zone);
        return from.withZoneSameInstant(utc);
    }

    public static ZonedDateTime fromUTC(LocalDateTime date, ZoneId zone) {
        ZonedDateTime from = date.atZone(utc);
        return from.withZoneSameInstant(zone);
    }
}
