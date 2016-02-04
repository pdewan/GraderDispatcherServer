package edu.unc.cs.niograderserver.graderHandler.sql;

import java.sql.SQLException;

/**
 * @author Andrew Vitkus
 *
 */
public interface IDatabaseReader extends AutoCloseable {

    public void connect(String username, String password, String server) throws SQLException;

    public int readCurrentCourseID(String name, String section) throws SQLException;

    public String readAssignmentCatalogName(String number, String type, String course_id) throws NumberFormatException, SQLException;

    public String readAssignmentCatalogName(int number, String type, int course_id) throws SQLException;

    public int readAssignmentCatalogID(String number, String name, String type, String course_id) throws NumberFormatException, SQLException;

    public int readAssignmentCatalogID(int number, String name, String type, int course_id) throws SQLException;

    public int readLatestAssignmentSubmissionID(String user_id, String assignment_catalog_id) throws NumberFormatException, SQLException;

    public int readLatestAssignmentSubmissionID(int user_id, int assignment_catalog_id) throws SQLException;

    public int readLatestResultID(String assignment_submission_id) throws NumberFormatException, SQLException;

    public int readLatestResultID(int assignment_submission_id) throws SQLException;

    public int readLatestGradingPartID(String result_id) throws NumberFormatException, SQLException;

    public int readLatestGradingPartID(int result_id) throws SQLException;

    public int readLatestGradingPartID(String name, String result_id) throws NumberFormatException, SQLException;

    public int readLatestGradingPartID(String name, int result_id) throws SQLException;

    public int readLatestGradingTestID(String grading_part_id) throws NumberFormatException, SQLException;

    public int readLatestGradingTestID(int grading_part_id) throws SQLException;

    public int readLatestTestNoteID(String grading_test_id) throws NumberFormatException, SQLException;

    public int readLatestTestNoteID(int grading_test_id) throws SQLException;

    public int readCountForSubmission(String assignment_submission_id) throws SQLException, NumberFormatException;

    public int readCountForSubmission(int assignment_submission_id) throws SQLException;

    public int readSubmissionLimitForAssignment(String assignment_catalog_id) throws SQLException, NumberFormatException;

    public int readSubmissionLimitForAssignment(int assignment_catalog_id) throws SQLException;
    
    public String[] readCurrentTerm() throws NumberFormatException, SQLException;
    
    public String[] readTermForCurrentCourse(String name, String section) throws NumberFormatException, SQLException;
    
    public String[] readNameForUID(String uid) throws NumberFormatException, SQLException;
    
    public String[] readNameForUID(int uid) throws SQLException;
    
    public String readPIDForUID(String uid) throws NumberFormatException, SQLException;
    
    public String readPIDForUID(int uid) throws NumberFormatException, SQLException;

    public void disconnect() throws SQLException;

    public void close() throws SQLException;
}
