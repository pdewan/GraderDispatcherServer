package edu.unc.cs.niograderserver.pages;

import edu.unc.cs.niograderserver.graderHandler.InputBasedGraderHandler;
import edu.unc.cs.niograderserver.graderHandler.util.GradingFailureException;
import edu.unc.cs.niograderserver.pages.helpers.GradePageManager;
import edu.unc.cs.httpserver.util.ResponseStatusNotice;
import edu.unc.cs.httpserver.util.URLEncodedPostFileItem;
import edu.unc.cs.niograderserver.graderHandler.sql.DatabaseReader;
import edu.unc.cs.niograderserver.graderHandler.sql.IDatabaseReader;
import edu.unc.cs.niograderserver.utils.ConfigReader;
import edu.unc.cs.niograderserver.utils.IConfigReader;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.httpclient.HttpStatus;

public class GraderPageGenerator implements IGraderPageGenerator {

    private static final Logger LOG = Logger.getLogger(GraderPageGenerator.class.getName());

    private final String[] VALID_METHODS = new String[]{"POST"};
    
    @Override
    public String getPage(Optional<FileItem[]> request) throws ResponseStatusNotice {
        String assignment = null;
        String course = null;
        String onyen = null;
        Path fileLoc = null;
        String ip = null;
        String uid = null;
        String firstName = null;
        String lastName = null;
        String pid = null;
        String year = null;
        String season = null;
        
        try {
            IConfigReader config = new ConfigReader("./config/config.properties");
            String username = config.getString("database.username").orElseThrow(IllegalArgumentException::new);
            String password = config.getString("database.password").orElseThrow(IllegalArgumentException::new);
            String url = config.getString("database.url").orElseThrow(IllegalArgumentException::new);
            if (config.getBoolean("database.ssl", false).get()) {
                url += "?verifyServerCertificate=true&useSSL=true&requireSSL=true";
            }
            try (IDatabaseReader dr = new DatabaseReader(username, password, "jdbc:" + url) {}) {
                String[] termParts = dr.readCurrentTerm();
                year = termParts[0];
                season = termParts[1];
            }
        } catch (IOException | SQLException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        
        FileItem[] args = request.orElseThrow(() -> {System.out.println("broken"); return new ResponseStatusNotice(HttpStatus.SC_BAD_REQUEST);});
        for (FileItem arg : args) {
            switch (arg.getFieldName()) {
                case "assignment":
                    assignment = arg.getString();
                    break;
                case "course":
                    course = arg.getString();
                    break;
                case "onyen":
                    onyen = arg.getString();
                    break;
                case "file":
                    String name = arg.getName();
                    int lastDot = name.lastIndexOf('.');
                    String ext = lastDot < 0 ? "" : name.substring(lastDot);
                    try {
                        Path file = Files.createTempFile("grader-submission-", ext);
                        try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(file, StandardOpenOption.TRUNCATE_EXISTING));
                                BufferedInputStream bis = new BufferedInputStream(arg.getInputStream())) {
                            byte[] bytes = new byte[1024];
                            int available = bis.available();
                            while(available > 0) {
                                if (available > 1024) {
                                    bis.read(bytes);
                                    bos.write(bytes, 0, 1024);
                                } else {
                                    bytes = new byte[available];
                                    bis.read(bytes);
                                    bos.write(bytes, 0, available);
                                }
                                available = bis.available();
                            }
                        }
                        fileLoc = file;
                    } catch (IOException ex) {
                        LOG.log(Level.SEVERE, null, ex);
                        throw new ResponseStatusNotice(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                    }
                    break;
                case "ip":
                    ip = arg.getString();
                    break;
                case "uid":
                    uid = arg.getString();
                    try {
                        IConfigReader config = new ConfigReader("./config/config.properties");
                        String username = config.getString("database.username").orElseThrow(IllegalArgumentException::new);
                        String password = config.getString("database.password").orElseThrow(IllegalArgumentException::new);
                        String url = config.getString("database.url").orElseThrow(IllegalArgumentException::new);
                        if (config.getBoolean("database.ssl", false).get()) {
                            url += "?verifyServerCertificate=true&useSSL=true&requireSSL=true";
                        }
                        try (IDatabaseReader dr = new DatabaseReader(username, password, "jdbc:" + url) {}) {
                            String[] nameParts = dr.readNameForUID(uid);
                            firstName = nameParts[0];
                            lastName = nameParts[1];
                        }
                    } catch (IOException | SQLException ex) {
                        LOG.log(Level.SEVERE, null, ex);
                    }
                    break;
                case "firstName":
                    firstName = arg.getString();
                    break;
                case "lastName":
                    lastName = arg.getString();
                    break;
                case "pid":
                    pid = arg.getString();
                    break;
                case "year":
                    year = arg.getString();
                    break;
                case "season":
                    season = arg.getString();
                    break;
            }        
        }
        int missing = 0;
        if (assignment == null) {
            missing += 1;
        }
        if (course == null) {
            missing += 2;
        }
        if (onyen == null) {
            missing += 4;
        }
        if (fileLoc == null) {
            missing += 8;
        }
        if (ip == null) {
            missing += 16;
        }
        if (uid == null) {
            missing += 32;
        }
        if (firstName == null) {
            missing += 64;
        }
        if (lastName == null) {
            missing += 128;
        }
        if (assignment == null || course == null || onyen == null || fileLoc == null || ip == null
                || uid == null || firstName == null || lastName == null) {
            LOG.log(Level.WARNING, "Missing argument. Code: {0}", missing);
            throw new ResponseStatusNotice(HttpStatus.SC_BAD_REQUEST);
        }
        
        String uuid = submit(assignment, course, onyen, fileLoc, ip, uid, firstName, lastName, pid, year, season);
        
        FileItem uuidItem = new URLEncodedPostFileItem("id", uuid, Charset.defaultCharset().name());
        
        Optional<FileItem[]> retParam = Optional.of(new FileItem[]{uuidItem});
        
        int sleepCount = 0;
        while (GradePageManager.getNumber(uuid).orElse(-2) == -1 && sleepCount < 10) {
            try {
                sleepCount++;
                Thread.sleep(50);
            } catch (InterruptedException ex) {
                LOG.log(Level.SEVERE, null, ex);
                sleepCount = 10;
            }
        }
        
        return GradePageManager.IN_PROGRESS_PAGE_GENERATOR.getPage(retParam);
    }

    private String submit(String assignment, String course, String onyen, Path fileLoc, String ip, String uid, String firstName, String lastName, String pid, String year, String season) {
        String uuid = GradePageManager.add(GradePageManager.NOT_FOUND, ip, -1);
        
        InputBasedGraderHandler grader = new InputBasedGraderHandler();
        grader.setAssignment(assignment);
        grader.setCourse(course.split("-")[0]);
        grader.setSection(course.split("-")[1]);
        grader.setOnyen(onyen);
        grader.setSubmission(fileLoc);
        grader.setUID(uid);
        grader.setName(firstName, lastName);
        grader.setPID(pid);
        grader.setYear(year);
        grader.setSeason(season);
        grader.setPageUUID(uuid);

        GradePageManager.update(uuid, GradePageManager.IN_PROGRESS_PAGE_GENERATOR);
        GradePageManager.refresh(uuid);

        //System.out.println("grade");
        new Thread() {
            @Override
            public void run() {
                try {
                    GradePageManager.update(uuid, grader.process());
                    GradePageManager.refresh(uuid);
                } catch (GradingFailureException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                    throw new ResponseStatusNotice(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                }
            }
        }.start();
        return uuid;
    }

    @Override
    public String[] getValidMethods() {
        return VALID_METHODS;
    }
}
