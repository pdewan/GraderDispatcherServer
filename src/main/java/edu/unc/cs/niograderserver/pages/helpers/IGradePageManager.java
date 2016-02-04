package edu.unc.cs.niograderserver.pages.helpers;

import java.time.Instant;
import java.util.Optional;
import edu.unc.cs.htmlBuilder.IHTMLFile;

/**
 *
 * @author Andrew Vitkus
 */
public interface IGradePageManager {

    public boolean purge();

    public String add(IHTMLFile page, String ip);

    public Optional<Instant> getTimestamp(String key);

    public Optional<IHTMLFile> get(String key);

    public static long timeToMillis(final int millis, final int sec, final int min, final int hour, final int days) {
        long time = millis;
        time += sec * 1000;
        time += min * 60 * 1000;
        time += hour * 60 * 60 * 1000;
        time += days * 24 * 60 * 60 * 1000;
        return time;
    }
}
