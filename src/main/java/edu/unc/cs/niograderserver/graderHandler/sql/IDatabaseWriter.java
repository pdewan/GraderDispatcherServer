package edu.unc.cs.niograderserver.graderHandler.sql;

import java.sql.SQLException;

/**
 * @author Andrew Vitkus
 *
 */
public interface IDatabaseWriter extends AutoCloseable {

    public void connect(String username, String password, String server) throws SQLException;

    public void writeUser(String onyen, String uid, String pid, String first_name, String last_name) throws NumberFormatException, SQLException;

    public void writeUser(String onyen, int uid, int pid, String first_name, String last_name) throws SQLException;

    public void writeAssignment(String assignmenCatalogID, String userID) throws NumberFormatException, SQLException;

    public void writeAssignment(int assignmenCatalogID, int userID) throws SQLException;

    public void writeResult(String assignmenSubmissionID) throws NumberFormatException, SQLException;

    public void writeResult(int assignmenSubmissionID) throws SQLException;

    public void writeComments(String[] comments, String resusltID) throws NumberFormatException, SQLException;

    public void writeComments(String[] comments, int resusltID) throws SQLException;

    public void writeGradingParts(String[][] grading, boolean[] extraCredit, String resultID) throws NumberFormatException, SQLException;

    public void writeGradingParts(String[][] grading, boolean[] extraCredit, int resultID) throws SQLException;

    public void writeGradingTest(String name, String percent, String autoGraded, String gradingPartID) throws NumberFormatException, SQLException;

    public void writeGradingTest(String name, double percent, boolean autoGraded, int gradingPartID) throws SQLException;

    public void writeTestNotes(String[] notes, String gradingTestID) throws NumberFormatException, SQLException;

    public void writeTestNotes(String[] notes, int gradingTestID) throws SQLException;

    public void disconnect() throws SQLException;

    public void close() throws SQLException;
}
