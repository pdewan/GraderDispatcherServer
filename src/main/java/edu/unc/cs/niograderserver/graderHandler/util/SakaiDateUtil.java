package edu.unc.cs.niograderserver.graderHandler.util;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class SakaiDateUtil {

    /**
     * Gets the date and time from a Sakai timestamp converted to the local time
     * zone
     *
     * @param stamp the Sakai timestamp, not null
     *
     * @return the timestamp converted to the local time zone, not null
     */
    public static ZonedDateTime parseTimeStamp(String stamp) {
        return parseTimeStamp(stamp, ZoneId.systemDefault());
    }

    /**
     * Gets the date and time from a Sakai timestamp converted to a specified
     * time zone
     *
     * @param stamp the Sakai timestamp, not null
     * @param zone  the target time zone, not null
     *
     * @return the timestamp converted to the time zone, not null
     */
    public static ZonedDateTime parseTimeStamp(String stamp, ZoneId zone) {
        ZonedDateTime timeStamp = ZonedDateTime.parse(stamp, DateTimeFormatter.ofPattern("yyyyMMddhhmmssSSS"));
        timeStamp = timeStamp.withZoneSameInstant(ZoneId.of("GMT")).withZoneSameLocal(zone);

        return timeStamp;
    }

    /**
     * Gets a Sakai timestamp for the current time
     *
     * @return the timestamp, not null
     */
    public static String getTimeStamp() {
        return getTimeStamp(ZonedDateTime.now());
    }

    /**
     * Gets a Sakai timestamp for a specified time
     *
     * @param dateTime the date to convert into a Sakai timestamp
     *
     * @return the timestamp
     */
    public static String getTimeStamp(ZonedDateTime dateTime) {
        ZonedDateTime time = dateTime.withZoneSameLocal(ZoneId.of("GMT"));
        return time.format(DateTimeFormatter.ofPattern("yyyyMMddhhmmssSSS"));
    }

    private SakaiDateUtil() {
    }
}
