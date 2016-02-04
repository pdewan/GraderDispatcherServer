package edu.unc.cs.niograderserver.graderHandler.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author Andrew Vitkus
 */
public interface IGradePublisher {

    public IGradePublisher setAssignment(String name);

    public IGradePublisher setCourse(String course);

    public IGradePublisher setSection(String section);

    public IGradePublisher setFirstName(String firstName);

    public IGradePublisher setLastName(String lastName);

    public IGradePublisher setOnyen(String onyen);

    public IGradePublisher setPID(String pid);

    public IGradePublisher setUID(String uid);

    public IGradePublisher setGradesFile(Path gradesFile);

    public IGradePublisher setResultsFile(Path results);

    public void postToDatabase() throws SQLException, FileNotFoundException, IOException;

    public void saveToGradesFile() throws FileNotFoundException, IOException, InterruptedException, ExecutionException;
}
