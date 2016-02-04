package edu.unc.cs.niograderserver.pages.helpers;

import edu.unc.cs.httpserver.pages.IPageGenerator;
import edu.unc.cs.httpserver.util.ResponseStatusNotice;
import edu.unc.cs.niograderserver.pages.GradingInProgressPageGenerator;
import edu.unc.cs.niograderserver.pages.IGradingInProgressPageGenerator;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.validator.routines.InetAddressValidator;

/**
 * This class keeps a record of pages and IP addresses associated with UUIDs.
 *
 * @author Andrew Vitkus
 */
public class GradePageManager {

    private static final Logger LOG = Logger.getLogger(GradePageManager.class.getName());

    private static final Duration lifetime;
    private static final HashMap<String, PageHolder> pageMap;
    
    public static final IGradingInProgressPageGenerator IN_PROGRESS_PAGE_GENERATOR;
    public static final IPageGenerator GRADING_FAILURE;
    public static final IPageGenerator NOT_FOUND;

    static {
        pageMap = new HashMap<>(10);
        lifetime = Duration.ofMinutes(30);
        ScheduledExecutorService sec = Executors.newSingleThreadScheduledExecutor();
        sec.scheduleAtFixedRate(new ManagerMonitor(), 60, 60, TimeUnit.SECONDS);
        
        IN_PROGRESS_PAGE_GENERATOR = new GradingInProgressPageGenerator();
        GRADING_FAILURE = new IPageGenerator() {

            @Override
            public String getPage(Optional<FileItem[]> request) throws ResponseStatusNotice {
                throw new ResponseStatusNotice("Grading failed, please contact a TA.", HttpStatus.SC_INTERNAL_SERVER_ERROR);
            }

            @Override
            public String[] getValidMethods() {
                return new String[]{"GET"};
            }
        };
        NOT_FOUND = new IPageGenerator() {

            @Override
            public String getPage(Optional<FileItem[]> request) throws ResponseStatusNotice {
                throw new ResponseStatusNotice(HttpStatus.SC_NOT_FOUND);
            }

            @Override
            public String[] getValidMethods() {
                return new String[]{"GET"};
            }
        };
    }

    /**
     * Adds a new page to the manager with an associated IP address
     *
     * @param generator the page generator to add
     * @param ip   the IP address associated with the page
     *
     * @return the added page's UUID
     */
    public static String add(IPageGenerator generator, String ip, int number) {
        Objects.requireNonNull(generator, "The added grade page generator cannot be null.");
        String uuid = UUID.randomUUID().toString();
        synchronized (pageMap) {
            pageMap.put(uuid, new PageHolder(generator, ip, number));
        }
        LOG.log(Level.INFO, "Grading page with UUID ''{0}'' has been added.", uuid);
        return uuid;
    }

    /**
     * Gets the page associated with a UUID. If the page does not exist an
     * empty optional is returned.
     *
     * @param key a UUID
     *
     * @return the page generator associated with the given UUID
     */
    public static Optional<IPageGenerator> get(String key) {
        Objects.requireNonNull(key, "Grade page UUID cannot be null.");
        PageHolder holder;
        synchronized (pageMap) {
            holder = pageMap.get(key);
            if (holder == null) {
                return Optional.empty();
            }
            return Optional.of(holder.generator);
        }
    }

    /**
     * Gets the IP associated with a UUID. If the page does not exist an empty
     * optional is returned.
     *
     * @param key a UUID
     *
     * @return the IP associated with the given UUID
     */
    public static Optional<String> getIP(String key) {
        Objects.requireNonNull(key, "Grade page UUID cannot be null.");
        synchronized (pageMap) {
            PageHolder pageHolder = pageMap.get(key);
            if (pageHolder != null) {
                return pageHolder.ip;
            } else {
                return Optional.empty();
            }
        }
    }
    
    public static Optional<Integer> getNumber(String key) {
        PageHolder holder;
        synchronized (pageMap) {
            holder = pageMap.get(key);
            if (holder == null) {
                return Optional.empty();
            }
            return Optional.of(holder.number);
        }
    }

    /**
     * Gets the timestamp associated with a UUID. If the page does not exist an
     * empty optional is returned.
     *
     * @param key a UUID
     *
     * @return the timestamp associated with the given UUID
     */
    public static Optional<Instant> getTimestamp(String key) {
        PageHolder holder;
        synchronized (pageMap) {
            holder = pageMap.get(key);
            if (holder == null) {
                return Optional.empty();
            }
            return Optional.of(holder.instant);
        }

    }

    /**
     * Removes all pages from the manager.
     *
     * @return if the manager was changed
     */
    public static boolean purge() {
        synchronized (pageMap) {
            LOG.log(Level.INFO, "Purging grading page register.");
            if (pageMap.isEmpty()) {
                LOG.log(Level.INFO, "Register already empty.");
                return false;
            } else {
                LOG.log(Level.INFO, "{0} pages removed.", pageMap.size());
                pageMap.clear();
                return true;
            }
        }
    }

    /**
     * Sets the timestamp associated with the given UUID to the current time.
     *
     * @param key a UUID
     *
     * @return if a timestamp was updated
     */
    public static boolean refresh(String key) {
        Objects.requireNonNull(key, "Grade page UUID cannot be null.");
        synchronized (pageMap) {
            PageHolder pageHolder = pageMap.get(key);
            if (pageHolder != null) {
                pageHolder.instant = Instant.now();
                return true;
            } else {
                return false;
            }
        }
    }
    
    /**
     * Associates a new page with a UUID.
     *
     * @param key  a UUID
     * @param number the new grading run number
     *
     * @return if a page was updated
     */
    public static boolean update(String key, int number) {
        Objects.requireNonNull(key, "Grade page UUID cannot be null.");
        synchronized (pageMap) {
            PageHolder pageHolder = pageMap.get(key);
            if (pageHolder != null) {
                pageHolder.number = number;
                return true;
            } else {
                return false;
            }
        }
    }
    
    /**
     * Associates a new page with a UUID.
     *
     * @param key  a UUID
     * @param generator the new page generator
     *
     * @return if a page was updated
     */
    public static boolean update(String key, IPageGenerator generator) {
        Objects.requireNonNull(key, "Grade page UUID cannot be null.");
        Objects.requireNonNull(generator, "The updated grade page generator cannot be null.");
        synchronized (pageMap) {
            PageHolder pageHolder = pageMap.get(key);
            if (pageHolder != null) {
                pageHolder.generator = generator;
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Associates a new page with a UUID.
     *
     * @param key  a UUID
     * @param generator the new page generator
     * @param number the run number of this page
     *
     * @return if a page was updated
     */
    public static boolean update(String key, IPageGenerator generator, int number) {
        Objects.requireNonNull(key, "Grade page UUID cannot be null.");
        Objects.requireNonNull(generator, "The updated grade page generator cannot be null.");
        synchronized (pageMap) {
            PageHolder pageHolder = pageMap.get(key);
            if (pageHolder != null) {
                pageHolder.generator = generator;
                pageHolder.number = number;
                return true;
            } else {
                return false;
            }
        }
    }

    private GradePageManager() {
    }

    /**
     * This class removes any pages that have existed beyond their expiration.
     * A page is expired if the timestamp is more than a certain time before
     * the current time. All pages are compared to the same time.
     */
    private static class ManagerMonitor implements Runnable {

        @Override
        public void run() {
            final Instant now = Instant.now();
            synchronized (pageMap) {
                Iterator<Entry<String, PageHolder>> pageIter = pageMap.entrySet().iterator();
                while (pageIter.hasNext()) {
                    Entry<String, PageHolder> entry = pageIter.next();
                    final Instant start = entry.getValue().instant;
                    if (start.plus(lifetime).isBefore(now)) {
                        pageIter.remove();
                        LOG.log(Level.INFO, "Grading page with UUID ''{0}'' has expired.", entry.getKey());
                    }
                }
            }
        }
    }

    private static class PageHolder {

        int number;
        IPageGenerator generator;
        Instant instant;
        final Optional<String> ip;

        PageHolder(IPageGenerator generator, String ip, int number) {
            this(generator, ip, Instant.now(), number);
        }

        PageHolder(IPageGenerator generator, String ip, Instant instant, int number) {
            this.generator = generator;
            this.instant = instant;
            this.number = number;
            if (InetAddressValidator.getInstance().isValid(ip)) {
                this.ip = Optional.empty();
            } else {
                this.ip = Optional.of(ip);
            }
        }
    }
}
