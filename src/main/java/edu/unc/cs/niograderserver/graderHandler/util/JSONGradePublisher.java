package edu.unc.cs.niograderserver.graderHandler.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import edu.unc.cs.niograderserver.graderHandler.sql.DatabaseReader;
import edu.unc.cs.niograderserver.graderHandler.sql.DatabaseWriter;
import edu.unc.cs.niograderserver.graderHandler.sql.IDatabaseReader;
import edu.unc.cs.niograderserver.graderHandler.sql.IDatabaseWriter;
import edu.unc.cs.niograderserver.utils.ConfigReader;
import edu.unc.cs.niograderserver.utils.IConfigReader;

public class JSONGradePublisher implements IGradePublisher {

    private static final Logger LOG = Logger.getLogger(JSONGradePublisher.class.getName());

    private String course;
    private String firstName;
    private Path gradesFile;
    private String lastName;
    private String onyen;
    private String pid;
    private Path resultsFile;
    private String section;
    private String title;
    private String uid;

    @Override
    public void postToDatabase() throws SQLException, FileNotFoundException, IOException {
        IConfigReader config = new ConfigReader("./config/config.properties");
        String username = config.getString("database.username").orElseThrow(IllegalArgumentException::new);
        String password = config.getString("database.password").orElseThrow(IllegalArgumentException::new);
        String url = config.getString("database.url").orElseThrow(IllegalArgumentException::new);
        if (config.getBoolean("database.ssl", false).get()) {
            url += "?verifyServerCertificate=true&useSSL=true&requireSSL=true";
        }

        try (IDatabaseWriter dw = new DatabaseWriter(username, password, "jdbc:" + url);
                IDatabaseReader dr = new DatabaseReader(username, password, "jdbc:" + url)) {

            //dw.writeUser(onyen, uid, pid, first, last);
            String num = title.substring(title.lastIndexOf(' ') + 1, title.length() - 1);
            String type = title.substring(title.lastIndexOf('(') + 1, title.lastIndexOf(' '));
            int courseID = dr.readCurrentCourseID(course, section);
            String assignmentName = dr.readAssignmentCatalogName(num, type, Integer.toString(courseID));
            int assignmentCatalogID = dr.readAssignmentCatalogID(num, assignmentName, type, Integer.toString(courseID));
            dw.writeAssignment(assignmentCatalogID, Integer.parseInt(uid));

            int assignmentSubmissionID = dr.readLatestAssignmentSubmissionID(Integer.parseInt(uid), assignmentCatalogID);
            dw.writeResult(assignmentSubmissionID);

            File jsonFile = resultsFile.toFile();
            if (!jsonFile.exists()) {
                LOG.log(Level.WARNING, "Can''t read json outpt from file: {0}", jsonFile.getAbsolutePath());
            } else {
                IJSONReader reader = new JSONReader(jsonFile);
                int resultID = dr.readLatestResultID(assignmentSubmissionID);
                dw.writeGradingParts(reader.getGrading(), reader.getExtraCredit(), resultID);

                List<List<String>> testResults = reader.getGradingTests();

                for (List<String> test : testResults) {
                    String gradingPartName = test.get(0);
                    int gradingPartID = dr.readLatestGradingPartID(gradingPartName, resultID);
                    dw.writeGradingTest(test.get(1), test.get(2), test.get(3), Integer.toString(gradingPartID));
                    int gradingTestID = dr.readLatestGradingTestID(gradingPartID);
                    String[] notes = test.get(4).split("[;\n]");
                    if (notes.length > 0 && !notes[0].isEmpty()) {
                        dw.writeTestNotes(notes, gradingTestID);
                    }
                }

                dw.writeComments(reader.getComments(), resultID);
            }
        }
    }

    @Override
    public void saveToGradesFile() throws FileNotFoundException, IOException, InterruptedException, ExecutionException {
        File json = resultsFile.toFile();
        int points = 0;
        int possible = 0;
        if (json.exists()) {
            IJSONReader reader = new JSONReader(json);
            String[][] grading = reader.getGrading();
            for (String[] grade : grading) {
                points += Integer.parseInt(grade[2]);
                possible += Integer.parseInt(grade[3]);
            }
        }
        IGradingData gradingData = new GradingData(onyen, firstName, lastName, points, possible);
        int parenStart = title.indexOf('(');
        int parenEnd = title.indexOf(')');
        String assignmentName = title.substring(parenStart + 1, parenEnd);

        IGradeWriter gradeWriter = new CSVGradeWriter(assignmentName, gradesFile);
        gradeWriter.write(gradingData);
    }

    @Override
    public IGradePublisher setAssignment(String name) {
        this.title = name;
        return this;
    }

    @Override
    public IGradePublisher setCourse(String course) {
        this.course = course;
        return this;
    }

    @Override
    public IGradePublisher setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    @Override
    public IGradePublisher setGradesFile(Path gradesFile) {
        this.gradesFile = gradesFile;
        return this;
    }

    @Override
    public IGradePublisher setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    @Override
    public IGradePublisher setOnyen(String onyen) {
        this.onyen = onyen;
        return this;
    }

    @Override
    public IGradePublisher setPID(String pid) {
        this.pid = pid;
        return this;
    }

    @Override
    public IGradePublisher setResultsFile(Path resultsFile) {
        this.resultsFile = resultsFile;
        return this;
    }

    @Override
    public IGradePublisher setSection(String section) {
        this.section = section;
        return this;
    }

    @Override
    public IGradePublisher setUID(String uid) {
        this.uid = uid;
        return this;
    }
}
