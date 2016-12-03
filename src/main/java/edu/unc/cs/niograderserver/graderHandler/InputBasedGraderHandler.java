package edu.unc.cs.niograderserver.graderHandler;

import edu.unc.cs.niograderserver.graderHandler.sql.DatabaseReader;
import edu.unc.cs.niograderserver.graderHandler.sql.DatabaseWriter;
import edu.unc.cs.niograderserver.graderHandler.sql.IDatabaseWriter;
import edu.unc.cs.niograderserver.graderHandler.util.FileTreeManager;
import edu.unc.cs.niograderserver.graderHandler.util.GradingFailureException;
import edu.unc.cs.niograderserver.graderHandler.util.IGradePublisher;
import edu.unc.cs.niograderserver.graderHandler.util.JSONGradePublisher;
import edu.unc.cs.niograderserver.gradingProgram.GraderPool;
import edu.unc.cs.niograderserver.gradingProgram.GraderSetup;
import edu.unc.cs.niograderserver.gradingProgram.IGraderSetup;
import edu.unc.cs.niograderserver.utils.ConfigReader;
import edu.unc.cs.niograderserver.utils.IConfigReader;
import edu.unc.cs.niograderserver.pages.helpers.GradePageManager;
import edu.unc.cs.niograderserver.gradingProgram.GraderFutureHolder;
import edu.unc.cs.niograderserver.graderHandler.util.PendingSubmissionManger;
import edu.unc.cs.htmlBuilder.IHTMLFile;
import edu.unc.cs.httpserver.pages.IPageGenerator;
import edu.unc.cs.httpserver.util.ResponseStatusNotice;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static java.nio.file.StandardOpenOption.APPEND;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.apache.commons.fileupload.FileItem;

public class InputBasedGraderHandler {

    private static final Logger LOG = Logger.getLogger(InputBasedGraderHandler.class.getName());

    private Path assignmentRoot;
    private String course;
    private String firstName;
    private Path jsonPath;
    private Path checkstylePath;
    private String lastName;
    private String onyen;
    private String pageUUID;
    private String pid;
    private String section;
    private Path submission;
    private Path submissionPath;
    private String title;
    private String uid;
    private String year;
    private String season;
    private Path userPath;
    
    private int runNumber;
    
    protected static final boolean PURGE_SUBMISSION = true;

    public InputBasedGraderHandler() {
        assignmentRoot = Paths.get("graderProgram", "data");
        runNumber = -1;
        System.out.println ("Turning off logging in:" + this);
        LOG.setLevel(Level.OFF);
    }

    public void setAssignment(String name) {
        System.out.println("Assignment: " + name);
        this.title = name;
    }

    public void setCourse(String course) {
        System.out.println("Course: " + course);
        this.course = course;
    }

    public void setFirstName(String firstName) {
        System.out.println("firstName: " + firstName);
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        System.out.println("lastName: " + lastName);
        this.lastName = lastName;
    }

    public void setOnyen(String onyen) {
        System.out.println("onyen: " + onyen);
        this.onyen = onyen;
    }

    public void setPID(String pid) {
        System.out.println("pid: " + pid);
        this.pid = pid;
    }

    public void setPageUUID(String uuid) {
        this.pageUUID = uuid;
    }

    public void setSection(String section) {
        System.out.println("section: " + section);
        this.section = section;
    }

    public void setSubmission(Path submission) {
        System.out.println("submission: " + submission);
        this.submission = submission;
    }

    public void setUID(String uid) {
        System.out.println("uid: " + uid);
        this.uid = uid;
    }
    
    public void setYear(String year) {
        System.out.println("year: " + year);
        this.year = year;
    }
    
    public void setSeason(String season) {
        System.out.println("season: " + season);
        this.season = season;
    }
    
    protected boolean purgeSubmission() {
    	return PURGE_SUBMISSION;
    }
    
    private long startTime;
    private long endPendTime;
    
    public static final int MAX_TRIES = 10;

    public IPageGenerator process() throws GradingFailureException {
        startTime = System.currentTimeMillis();
//        boolean doClear = true;
        // why was clearinng true
        boolean doClear = false;
        System.out.println ("Processing submission");
        
        try {  
        	int aNumTries = 0;
            while (aNumTries < MAX_TRIES && PendingSubmissionManger.isPending(uid, course, section, title)) {
                try {
                	System.out.println("Pending submission");
                    Thread.sleep(100);
                    aNumTries ++;
                } catch (InterruptedException ex) {
                    Logger.getLogger(InputBasedGraderHandler.class.getName()).log(Level.SEVERE, null, ex);
                    doClear = false;
                }
            }
            if (aNumTries == MAX_TRIES) {
            	System.out.println ("Given up on waiting for pending");
            }
            System.out.println ("Submission manager no longer pending or given up on pendng");
            endPendTime = System.currentTimeMillis();
            PendingSubmissionManger.addSubmission(uid, course, section, title);
            FileTreeManager.checkPurgeRoot();
            boolean success = readSubmission();
            System.out.println ("Sucess result:" + success);
            if (success) {
                // this is less readable but fails fast
                if (!(onyen == null || uid == null || pid == null || firstName == null || lastName == null)) {
                    addUser();
                }
                String submissionType = Files.probeContentType(submission);
                Path gradesFile = assignmentRoot.resolve("grades.csv");
                System.out.println(gradesFile.toString());
                if (!gradesFile.toFile().exists()) {
                	System.out.println ("Creating grades file");
                    int parenStart = title.indexOf('(');
                    int parenEnd = title.indexOf(')');
                    String assignmentName = title.substring(parenStart + 1, parenEnd);
                    StringBuilder fileContents = new StringBuilder(100);
                    fileContents.append(assignmentName).append(",Points,,,\n")
                            .append(",,,,\n")
                            .append("Display ID,ID,Last Name,First Name,grade\n")
                            .append(onyen).append(',').append(onyen).append(',').append(lastName).append(',').append(firstName).append(",0.0\n");
                    Files.write(gradesFile, fileContents.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                } else {
                	System.out.println ("Checking onyen in grades file");

                    boolean addOnyen = true;
                    StringBuilder toWrite = new StringBuilder((int)Files.size(gradesFile));
                    for(String line : Files.readAllLines(gradesFile)) {
                        line = line.replaceAll("\uFEFF", "");
                        if (line.startsWith(onyen)) {
                            addOnyen = false;
                        }
                        toWrite.append(line).append("\n");
                    }
                    if (addOnyen) {
                    	System.out.println ("Adding onyen to Creating grades file");

                        toWrite.append(onyen).append(',').append(onyen).append(',').append(lastName).append(',').append(firstName).append(",0.0\n");
                    }
                    Files.write(gradesFile, toWrite.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING);
                    
//                    if (lines.noneMatch((line) -> line.startsWith(onyen))) {
//                        try (OutputStreamWriter osw = new OutputStreamWriter(Files.newOutputStream(gradesFile, APPEND))) {
//                            StringBuilder newLine = new StringBuilder(50);
//                            newLine.append(onyen).append(',').append(onyen).append(',').append(lastName).append(',').append(firstName).append(",0.0\n");
//                            osw.write(newLine.toString().toCharArray());
//                        }
//                    }
                }
                if (!submissionType.equals("application/json")) {
                    try {
                        System.out.println("Grade?");
                        if (underSubmitLimit()) {
                        grade();
                        } else {
                        	System.out.println ("Did not grade, too many submissions");
                        }
                        System.out.println("Grade.");
                        jsonPath = userPath.resolve(Paths.get("Feedback Attachment(s)", "results.json"));
                        checkstylePath = userPath.resolve(Paths.get("Feedback Attachment(s)", "checkstyle.txt"));
                    } catch (InterruptedException e1) {
                        LOG.log(Level.FINER, null, e1);
                    }
                } else {
                    jsonPath = submission;
                }
                try {
                    IGradePublisher gradeWriter = new JSONGradePublisher();
                    gradeWriter.setAssignment(title).setCourse(course)
                            .setSection(section).setFirstName(firstName)
                            .setLastName(lastName).setPID(pid)
                            .setUID(uid).setOnyen(onyen)
                            .setGradesFile(gradesFile)
                            .setResultsFile(jsonPath);
                    try {
                        // write to grades.csv of old zip if allowed
                        if (underSubmitLimit() && writeToGradeFile()) {
                        	
                            gradeWriter.saveToGradesFile();
                        } else {
                        	System.out.println ("Did not write to grade file in INputBasedGenerator");
                        }
                    } catch (FileNotFoundException | InterruptedException | ExecutionException ex) {
                        LOG.log(Level.FINER, null, ex);
                    }
                    gradeWriter.postToDatabase();
                    System.out.println ("Posted to database");
                } catch (SQLException e) {
                    LOG.log(Level.FINER, null, e);
                }
                final IHTMLFile gradingResult = createResponse(title);
                System.out.println ("Finished response");
                IPageGenerator resultGenerator = new IPageGenerator() {
                    private final String html = gradingResult.getHTML();
                    
                    @Override
                    public String getPage(Optional<FileItem[]> request) throws ResponseStatusNotice {
                        return html;
                    }

                    @Override
                    public String[] getValidMethods() {
                        return new String[]{"GET"};
                    }
                };
                System.out.println ("Returning result");
                return resultGenerator;
            } else {
                GradePageManager.refresh(pageUUID);
                GradePageManager.update(pageUUID, GradePageManager.GRADING_FAILURE);
                throw new GradingFailureException();
            }
        } catch (MalformedURLException e1) {
            LOG.log(Level.FINER, null, e1);
            GradingFailureException e = new GradingFailureException();
            e.setStackTrace(e1.getStackTrace());
            throw e;
        } catch (IOException e1) {
            LOG.log(Level.FINER, null, e1);
            GradingFailureException e = new GradingFailureException();
            e.setStackTrace(e1.getStackTrace());
            throw e;
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, null, ex);
            GradingFailureException e = new GradingFailureException();
            e.setStackTrace(ex.getStackTrace());
            throw e;
        } finally { 
            if (doClear) {
                PendingSubmissionManger.removeSubmission(uid, course, section, title);
            } else {
            	System.out.println("Not removing submission for:" + uid);
            }
        }
    }

    public void setName(String first, String last) {
        firstName = first;
        lastName = last;
    }

    private void addUser() throws IOException, SQLException {
        IConfigReader config = new ConfigReader("./config/config.properties");
        String username = config.getString("database.username").orElseThrow(IllegalArgumentException::new);
        String password = config.getString("database.password").orElseThrow(IllegalArgumentException::new);
        String url = config.getString("database.url").orElseThrow(IllegalArgumentException::new);
        if (config.getBoolean("database.ssl", false).get()) {
            url += "?verifyServerCertificate=true&useSSL=true&requireSSL=true";
        }

        try (IDatabaseWriter dw = new DatabaseWriter(username, password, "jdbc:" + url)) {
            dw.writeUser(onyen, uid, pid, firstName, lastName);
        }
    }

    private IHTMLFile createResponse(String title) throws FileNotFoundException, IOException {
        try {
        	System.out.println ("Creating response for title:" + title);
            IResponseWriter responseWriter = new JSONBasedResponseWriter(jsonPath.toFile(), checkstylePath.toFile(), true);
        	System.out.println ("Got reponse from JSON Based Response Writer");

            responseWriter.setAssignmentName(title);
            System.out.println("Finished creating response");

            return responseWriter.getResponse();
        } catch (FileNotFoundException e) {
            LOG.log(Level.WARNING, "Unable to access JSON file at: {0}", jsonPath);
            throw e;
        }
    }

    private void grade() throws IOException, InterruptedException {
        int parenStart = title.indexOf('(');
        int parenEnd = title.indexOf(')');
        String assignmentName = title.substring(parenStart + 1, parenEnd);
        assignmentName = assignmentName.replace(" ", "");
        IGraderSetup setup = new GraderSetup(onyen, assignmentRoot, course, section, assignmentName, firstName, lastName, year, season);
        setup.setupFiles();
        try {
            //System.out.println("Args: " + Arrays.toString(setup.getCommandArgs()));
            GraderFutureHolder graderHolder = GraderPool.runGrader(setup.getCommandArgs());
            Future<String> grader = graderHolder.getFuture();
            runNumber = graderHolder.getNumber();
            GradePageManager.update(pageUUID, runNumber);
            GradePageManager.refresh(pageUUID);
            grader.get();
            long curTime = System.currentTimeMillis();
            LOG.log(Level.INFO, "Pending: {0}, Runtime: {1}, Total: {2}", new Object[]{endPendTime - startTime, curTime - endPendTime, curTime - startTime});
            // log grader program output
            //LOG.log(Level.INFO, "Grader_Program: {0}", grader.get());
        } catch (ExecutionException e) {
            LOG.log(Level.FINER, null, e);
        }
    }

    private boolean readSubmission() throws SQLException {
        try {
        	System.out.println ("Reading submission");
            IConfigReader config = new ConfigReader("./config/config.properties");
            String username = config.getString("database.username").orElseThrow(IllegalArgumentException::new);
            String password = config.getString("database.password").orElseThrow(IllegalArgumentException::new);
            String url = config.getString("database.url").orElseThrow(IllegalArgumentException::new);
            if (config.getBoolean("database.ssl", false).get()) {
                url += "?verifyServerCertificate=true&useSSL=true&requireSSL=true";
            }
            if (year == null || season == null) {
                try (DatabaseReader dr = new DatabaseReader(username, password, "jdbc:" + url)) {
                    String[] term = dr.readTermForCurrentCourse(course, section);
                    if (year == null) {
                        year = term[0];
                    }
                    if (season == null) {
                        season = term[1];
                    }
                }
            }
            int parenStart = title.indexOf('(');
            int parenEnd = title.indexOf(')');
            String assignmentName = title.substring(parenStart + 1, parenEnd);
            assignmentName = assignmentName.replace(" ", "");

            assignmentRoot = assignmentRoot.resolve(Paths.get(year, season, course.replaceAll(" ", ""), section, assignmentName));
            userPath = assignmentRoot.resolve(lastName + ", " + firstName + " (" + onyen + ")");
            String[] fileSplit = submission.getFileName().toString().split("\\.", 2);
            submissionPath = userPath.resolve(Paths.get("Submission attachment(s)")).resolve(title + (fileSplit.length > 1 ? "." + fileSplit[1] : ""));
            System.out.println ("Assignment root:" + assignmentRoot);
            System.out.println ("user path:" + userPath);

            System.out.println ("Submission path:" + submissionPath);
            try {
                if (submissionPath.toFile().exists() && underSubmitLimit()) { // write backup of old zip if allowed
                    Path backupPath = userPath.resolve(Paths.get("Submission attachment(s)", submissionPath.getFileName() + ".bak"));
                    FileTreeManager.backup(submissionPath, backupPath);
                }
            } catch (FileNotFoundException e) {
                LOG.log(Level.FINER, null, e);
                e.printStackTrace();
            } catch (IOException | SQLException e) {
                e.printStackTrace();
                LOG.log(Level.FINER, null, e);
            }

//            FileTreeManager.purgeSubmission(assignmentRoot);
//            System.out.println ("Asking file tree manager to purge:" + userPath);
            System.out.println ("purging " + userPath + " as cannot overwrite zip file");
            if (purgeSubmission()) {
            FileTreeManager.purgeSubmission(userPath);
            }


            Files.createDirectories(submissionPath);

            FileTreeManager.backup(submission, submissionPath);
            return true;
        } catch (IOException e) {
            LOG.log(Level.FINER, null, e);
            e.printStackTrace();
            return false;
        }
    }
    protected boolean writeToGradeFile() {
    	return false;
    }

    private boolean underSubmitLimit() throws IOException, SQLException {
        int parenStart = title.indexOf('(');
        int parenEnd = title.indexOf(')');
        String assignmentName = title.substring(parenStart + 1, parenEnd);

        IConfigReader config = new ConfigReader("./config/config.properties");
        String username = config.getString("database.username").orElseThrow(IllegalArgumentException::new);
        String password = config.getString("database.password").orElseThrow(IllegalArgumentException::new);
        String url = config.getString("database.url").orElseThrow(IllegalArgumentException::new);
        if (config.getBoolean("database.ssl", false).get()) {
            url += "?verifyServerCertificate=true&useSSL=true&requireSSL=true";
        }
        try (DatabaseReader dr = new DatabaseReader(username, password, "jdbc:" + url)) {
            int num = Integer.parseInt(assignmentName.substring(assignmentName.lastIndexOf(' ') + 1)); // assignment number
            String type = assignmentName.substring(0, assignmentName.lastIndexOf(' ')); // assignment type
            int courseID = dr.readCurrentCourseID(course, section); // sql id of course
            String name = dr.readAssignmentCatalogName(num, type, courseID); // sql name of assignment
            int assignmentCatalogID = dr.readAssignmentCatalogID(num, name, type, courseID); // sql id from assignment catalog
            int assignmentSubmissionID = dr.readLatestAssignmentSubmissionID(Integer.parseInt(uid), assignmentCatalogID); // sql id of assignment submissions

            int subCount = dr.readCountForSubmission(assignmentSubmissionID); // number of submissions
            int subLimit = dr.readSubmissionLimitForAssignment(assignmentCatalogID); // max limit of saved submissions
            return subLimit == 0 || subCount < subLimit;
        }
    }
}
