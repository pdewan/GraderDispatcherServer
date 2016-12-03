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

/**
 * @author Andrew Vitkus
 *
 */
public class DatabaseWriter implements IDatabaseWriter {
    
    Logger LOG = Logger.getLogger(DatabaseWriter.class.getName());

    private Connection connection;
    private IDatabaseReader reader;

    public DatabaseWriter() {
    	System.out.println ("Turning off logging in:" + this);
        LOG.setLevel(Level.OFF);
    }

    public DatabaseWriter(String username, String password, String server) throws SQLException {
        connect(username, password, server);
    }

    @Override
    public final void connect(String username, String password, String server) throws SQLException {
        Properties connectionProps = new Properties();
        connectionProps.put("user", username);
        connectionProps.put("password", password);
        try {
            DriverManager.registerDriver((Driver) Class.forName("com.mysql.jdbc.Driver").newInstance());
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        connection = DriverManager.getConnection(server, connectionProps);

        reader = new DatabaseReader();
        reader.connect(username, password, server);
    }

    @Override
    public void writeUser(String onyen, String uid, String pid, String first_name, String last_name) throws NumberFormatException, SQLException {
        writeUser(onyen, Integer.parseInt(uid), Integer.parseInt(pid), first_name, last_name);
    }

    @Override
    public void writeUser(String onyen, int uid, int pid, String first_name, String last_name) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement("INSERT IGNORE INTO user (onyen, uid, pid, first_name, last_name) VALUES (?, ?, ?, ?, ?)")) {
            pstmt.setString(1, onyen);
            pstmt.setInt(2, uid);
            pstmt.setInt(3, pid);
            pstmt.setString(4, first_name);
            pstmt.setString(5, last_name);
            pstmt.executeUpdate();
        }
    }

    @Override
    public void writeAssignment(String assignmentCatalogID, String userID) throws NumberFormatException, SQLException {
        writeAssignment(Integer.parseInt(assignmentCatalogID), Integer.parseInt(userID));
    }

    @Override
    public void writeAssignment(int assignmentCatalogID, int userID) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM assignment_submission WHERE assignment_catalog_id = ? AND user_uid = ?")) {
            pstmt.setInt(1, assignmentCatalogID);
            pstmt.setInt(2, userID);
            ResultSet results = pstmt.executeQuery();
            if (results.isBeforeFirst()) {
                return;
            }
        }

        try (PreparedStatement pstmt = connection.prepareStatement("INSERT INTO assignment_submission (assignment_catalog_id, user_uid) VALUES (?, ?)")) {
            pstmt.setInt(1, assignmentCatalogID);
            pstmt.setInt(2, userID);
            pstmt.executeUpdate();
        }
    }

    @Override
    public void writeResult(String assignmentSubmissionID) throws NumberFormatException, SQLException {
        writeResult(Integer.parseInt(assignmentSubmissionID));
    }

    @Override
    public void writeResult(int assignmentSubmissionID) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement("INSERT INTO result (assignment_submission_id) VALUES (?)")) {
            pstmt.setInt(1, assignmentSubmissionID);
            pstmt.executeUpdate();
        }
    }

    @Override
    public void writeComments(String[] comments, String resultID) throws NumberFormatException, SQLException {
        writeComments(comments, Integer.parseInt(resultID));
    }

    @Override
    public void writeComments(String[] comments, int resultID) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement("INSERT INTO comment (comment, result_id) VALUES (?, ?)")) {
            for (String comment : comments) {
                pstmt.setString(1, comment);
                pstmt.setInt(2, resultID);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    @Override
    public void writeGradingParts(String[][] grading, boolean[] extraCredit, String resultID) throws NumberFormatException, SQLException {
        writeGradingParts(grading, extraCredit, Integer.parseInt(resultID));
    }

    @Override
    public void writeGradingParts(String[][] grading, boolean[] extraCredit, int resultID) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement("INSERT INTO grading_part (name, auto_graded_percent, points, possible, extra_credit, result_id) VALUES (?, ?, ?, ?, ?, ?)")) {
            for (int i = 0; i < grading.length; i++) {
                String[] part = grading[i];
                pstmt.setString(1, part[0]);
                pstmt.setDouble(2, Double.parseDouble(part[1]));
                pstmt.setDouble(3, Double.parseDouble(part[2]));
                pstmt.setDouble(4, Double.parseDouble(part[3]));
                pstmt.setBoolean(5, extraCredit[i]);
                pstmt.setInt(6, resultID);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    @Override
    public void writeGradingTest(String name, String percent, String autoGraded, String gradingPartID) throws NumberFormatException, SQLException {
        writeGradingTest(name, Double.parseDouble(percent), Boolean.parseBoolean(autoGraded), Integer.parseInt(gradingPartID));
    }

    @Override
    public void writeGradingTest(String name, double percent, boolean autoGraded, int gradingPartID) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement("INSERT INTO grading_test (name, percent, auto_graded, grading_part_id) VALUES (?, ?, ?, ?)")) {
            pstmt.setString(1, name);
            pstmt.setDouble(2, percent);
            pstmt.setBoolean(3, autoGraded);
            pstmt.setInt(4, gradingPartID);
            pstmt.executeUpdate();
        }
    }

    @Override
    public void writeTestNotes(String notes[], String gradingTestID) throws NumberFormatException, SQLException {
        writeTestNotes(notes, Integer.parseInt(gradingTestID));
    }

    @Override
    public void writeTestNotes(String notes[], int gradingTestID) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement("INSERT INTO test_note (note, grading_test_id) VALUES (?, ?)")) {
            for (String note : notes) {
                pstmt.setString(1, note);
                pstmt.setInt(2, gradingTestID);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    @Override
    public void disconnect() throws SQLException {
        reader.disconnect();
        if (connection != null) {
            connection.close();
        }
    }

    @Override
    public void close() throws SQLException {
        disconnect();
    }
}
