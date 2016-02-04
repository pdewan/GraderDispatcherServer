package edu.unc.cs.niograderserver.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

/**
 *
 * @author Andrew Vitkus
 */
public class TimestampUtil {
    
    /**
     * 
     * @return a Sakai formatted timestamp based on the current date and time
     */
    public static String getCurrentSakaiTimestamp() {
        return getSakaiTimestamp(Instant.now());
    }
    
    /**
     * 
     * @param time the time for the timestamp
     * @return a Sakai formatted timestamp based on the specified date and time
     */
    public static String getSakaiTimestamp(Instant time) {
        return getTimestamp(time, "yyyyMMddhhmmssSSS", ZoneOffset.UTC, ZoneOffset.UTC);
    }
    
    /**
     * 
     * @param time the time for the timestamp
     * @param format a string describing the timestamp's format
     * @param sourceTimeZone the time zone source time
     * @param targetTimeZone the time zone target time
     * @return a formatted time zone for the specified time, format, and time zone
     */
    public static String getTimestamp(Instant time, String format, ZoneId sourceTimeZone, ZoneId targetTimeZone) {
        LocalDateTime localTime = LocalDateTime.ofInstant(time, sourceTimeZone);
        ZonedDateTime zonedTime = ZonedDateTime.of(localTime, targetTimeZone);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
        return dtf.format(zonedTime);
    }
}
