package edu.unc.cs.niograderserver.graderHandler.sql;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseReader implements IDatabaseReader {
    private static final Logger LOG = Logger.getLogger(DatabaseReader.class.getName());

    private Connection connection;

    public DatabaseReader() {
    }

    public DatabaseReader(String username, String password, String server) throws SQLException {
    	System.out.println ("Turning off logging in:" + this);
        LOG.setLevel(Level.OFF);
    	connect(username, password, server);
    }

    @Override
    public void close() throws SQLException {
        disconnect();
    }

    @Override
    public final void connect(String username, String password, String server) throws SQLException {
        Properties connectionProps = new Properties();
        connectionProps.put("user", username);
        connectionProps.put("password", password);
        try {
            DriverManager.registerDriver((Driver) Class.forName("com.mysql.jdbc.Driver").newInstance());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        connection = DriverManager.getConnection(server, connectionProps);
    }

    @Override
    public void disconnect() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    @Override
    public int readAssignmentCatalogID(String number, String name, String type, String course_id) throws NumberFormatException, SQLException {
        return readAssignmentCatalogID(Integer.parseInt(number), name, type, Integer.parseInt(course_id));
    }

    @Override
    public int readAssignmentCatalogID(int number, String name, String type, int course_id) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM assignment_catalog WHERE name = ? AND number = ? AND course_id = ? AND assignment_type_id IN (SELECT id FROM assignment_type WHERE name = ?)")) {
            pstmt.setString(1, name);
            pstmt.setInt(2, number);
            pstmt.setInt(3, course_id);
            pstmt.setString(4, type);
            ResultSet results = pstmt.executeQuery();
            if (results.isBeforeFirst()) {
                results.first();
                return results.getInt("id");
            } else {
                return 0;
            }
        }
    }

    @Override
    public String readAssignmentCatalogName(String number, String type, String course_id) throws NumberFormatException, SQLException {
        return readAssignmentCatalogName(Integer.parseInt(number), type, Integer.parseInt(course_id));
    }

    @Override
    public String readAssignmentCatalogName(int number, String type, int course_id) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM assignment_catalog WHERE number = ? AND course_id = ? AND assignment_type_id IN (SELECT id FROM assignment_type WHERE name = ?)")) {
            pstmt.setInt(1, number);
            pstmt.setInt(2, course_id);
            pstmt.setString(3, type);
            ResultSet results = pstmt.executeQuery();
            if (results.isBeforeFirst()) {
                results.first();
                return results.getString("name");
            } else {
                return "";
            }
        }
    }

    @Override
    public int readCountForSubmission(String assignment_catalog_id) throws SQLException, NumberFormatException {
        return readCountForSubmission(Integer.parseInt(assignment_catalog_id));
    }

    @Override
    public int readCountForSubmission(int assignment_submission_id) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT COUNT(*) AS count FROM result WHERE assignment_submission_id = ?")) {
            pstmt.setInt(1, assignment_submission_id);
            ResultSet results = pstmt.executeQuery();
            if (results.isBeforeFirst()) {
                results.last();
                return results.getInt("count");
            } else {
                return 0;
            }
        }
    }

    @Override
    public int readCurrentCourseID(String name, String section) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM course WHERE name = ? && section = ? && term_id IN (SELECT id FROM term WHERE current = TRUE)")) {
            pstmt.setString(1, name);
            pstmt.setString(2, section);
            ResultSet results = pstmt.executeQuery();
            if (results.isBeforeFirst()) {
                results.first();
                return results.getInt("id");
            } else {
                return 0;
            }
        }
    }

    @Override
    public String[] readCurrentTerm() throws NumberFormatException, SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM term WHERE current = TRUE")) {
            ResultSet results = pstmt.executeQuery();
            if (results.isBeforeFirst()) {
                results.first();
                return new String[]{Integer.toString(results.getInt("year")), results.getString("season")};
            } else {
                return new String[]{"", ""};
            }
        }
    }

    @Override
    public String[] readNameForUID(String uid) throws NumberFormatException, SQLException {
        return readNameForUID(Integer.parseInt(uid));
    }

    @Override
    public String[] readNameForUID(int uid) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT first_name, last_name FROM user WHERE uid = ?")) {
            pstmt.setInt(1, uid);
            ResultSet results = pstmt.executeQuery();
            if (results.isBeforeFirst()) {
                results.first();
                return new String[]{results.getString("first_name"), results.getString("last_name")};
            } else {
                return new String[]{"", ""};
            }
        }
    }
    
    @Override
    public String readPIDForUID(String uid) throws NumberFormatException, SQLException {
        return readPIDForUID(Integer.parseInt(uid));
    }
    @Override
    public String readPIDForUID(int uid) throws NumberFormatException, SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT pid FROM user WHERE uid = ?")) {
            pstmt.setInt(1, uid);
            ResultSet results = pstmt.executeQuery();
            if (results.isBeforeFirst()) {
                results.first();
                return Integer.toString(results.getInt("pid"));
            } else {
                return "";
            }
        }
    }
    
    @Override
    public String[] readTermForCurrentCourse(String name, String section) throws NumberFormatException, SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM term WHERE current = TRUE && id IN (SELECT term_id FROM course WHERE name = ? AND section = ?)")) {
            pstmt.setString(1, name);
            pstmt.setString(2, section);
            ResultSet results = pstmt.executeQuery();
            if (results.isBeforeFirst()) {
                results.first();
                return new String[]{Integer.toString(results.getInt("year")), results.getString("season")};
            } else {
                return new String[]{"", ""};
            }
        }
    }

    @Override
    public int readLatestAssignmentSubmissionID(String user_id, String assignment_catalog_id) throws NumberFormatException, SQLException {
        return readLatestAssignmentSubmissionID(Integer.parseInt(user_id), Integer.parseInt(assignment_catalog_id));
    }

    @Override
    public int readLatestAssignmentSubmissionID(int user_uid, int assignment_catalog_id) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM assignment_submission WHERE user_uid = ? AND assignment_catalog_id = ?")) {
            pstmt.setInt(1, user_uid);
            pstmt.setInt(2, assignment_catalog_id);
            ResultSet results = pstmt.executeQuery();
            if (results.isBeforeFirst()) {
                results.first();
                return results.getInt("id");
            } else {
                return 0;
            }
        }
    }

    @Override
    public int readLatestGradingPartID(String result_id) throws NumberFormatException, SQLException {
        return readLatestGradingPartID(Integer.parseInt(result_id));
    }

    @Override
    public int readLatestGradingPartID(int result_id) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM grading_part WHERE result_id = ?")) {
            pstmt.setInt(1, result_id);
            ResultSet results = pstmt.executeQuery();
            if (results.isBeforeFirst()) {
                results.last();
                return results.getInt("id");
            } else {
                return 0;
            }
        }
    }

    @Override
    public int readLatestGradingPartID(String name, String result_id) throws NumberFormatException, SQLException {
        return readLatestGradingPartID(name, Integer.parseInt(result_id));
    }

    @Override
    public int readLatestGradingPartID(String name, int result_id) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM grading_part WHERE name = ? && result_id = ?")) {
            pstmt.setString(1, name);
            pstmt.setInt(2, result_id);
            ResultSet results = pstmt.executeQuery();
            if (results.isBeforeFirst()) {
                results.last();
                return results.getInt("id");
            } else {
                return 0;
            }
        }
    }

    @Override
    public int readLatestGradingTestID(String grading_part_id) throws NumberFormatException, SQLException {
        return readLatestGradingTestID(Integer.parseInt(grading_part_id));
    }

    @Override
    public int readLatestGradingTestID(int grading_part_id) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM grading_test WHERE grading_part_id = ?")) {
            pstmt.setInt(1, grading_part_id);
            ResultSet results = pstmt.executeQuery();
            if (results.isBeforeFirst()) {
                results.last();
                return results.getInt("id");
            } else {
                return 0;
            }
        }
    }

    @Override
    public int readLatestResultID(String assignment_submission_id) throws NumberFormatException, SQLException {
        return readLatestResultID(Integer.parseInt(assignment_submission_id));
    }

    @Override
    public int readLatestResultID(int assignment_submission_id) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM result WHERE assignment_submission_id = ?")) {
            pstmt.setInt(1, assignment_submission_id);
            ResultSet results = pstmt.executeQuery();
            if (results.isBeforeFirst()) {
                results.last();
                return results.getInt("id");
            } else {
                return 0;
            }
        }
    }

    @Override
    public int readLatestTestNoteID(String grading_test_id) throws NumberFormatException, SQLException {
        return readLatestTestNoteID(Integer.parseInt(grading_test_id));
    }

    @Override
    public int readLatestTestNoteID(int grading_test_id) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM test_note WHERE grading_test_id = ?")) {
            pstmt.setInt(1, grading_test_id);
            ResultSet results = pstmt.executeQuery();
            if (results.isBeforeFirst()) {
                results.last();
                return results.getInt("id");
            } else {
                return 0;
            }
        }
    }

    @Override
    public int readSubmissionLimitForAssignment(String assignment_catalog_id) throws SQLException, NumberFormatException {
        return readSubmissionLimitForAssignment(Integer.parseInt(assignment_catalog_id));
    }

    @Override
    public int readSubmissionLimitForAssignment(int assignment_catalog_id) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT submission_limit FROM assignment_catalog WHERE id = ?")) {
            pstmt.setInt(1, assignment_catalog_id);
            ResultSet results = pstmt.executeQuery();
            if (results.isBeforeFirst()) {
                results.last();
                return results.getInt("submission_limit");
            } else {
                return 0;
            }
        }
    }
}
