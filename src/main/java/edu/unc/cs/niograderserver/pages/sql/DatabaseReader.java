package edu.unc.cs.niograderserver.pages.sql;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author Andrew Vitkus
 *
 */
public class DatabaseReader implements IDatabaseReader {

    private Connection connection;

    @Override
    public void connect(String username, String password, String server) throws SQLException {
        if (connection == null || connection.isClosed()) {
            Properties connectionProps = new Properties();
            connectionProps.put("user", username);
            connectionProps.put("password", password);
            try {
                DriverManager.registerDriver((Driver) Class.forName("com.mysql.jdbc.Driver").newInstance());
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            connection = DriverManager.getConnection(server, connectionProps);
        }
    }

    @Override
    public ResultSet getResultsForUser(String onyen) throws SQLException {
        PreparedStatement pstmt = null;
        pstmt = connection.prepareStatement("SELECT * FROM result WHERE assignment_submission_id IN (SELECT id FROM assignment_submission WHERE user_uid IN (SELECT uid FROM user WHERE onyen = ?))");
        pstmt.setString(1, onyen);
        return pstmt.executeQuery();
    }

    @Override
    public void disconnect() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    @Override
    public ResultSet getResultsForAssignment(String course, String assignment) throws SQLException {
        PreparedStatement pstmt = null;
        String statement = "SELECT * FROM result";
        if (!course.isEmpty() || !assignment.isEmpty()) {
            statement += " WHERE assignment_submission_id IN (SELECT id FROM assignment_submission WHERE assignment_catalog_id IN (SELECT id FROM assignment_catalog WHERE";
            if (!course.isEmpty()) {
                statement += " course_id IN (SELECT id FROM course WHERE name = ?)";
                if (!assignment.isEmpty()) {
                    statement += " AND";
                }
            }
            if (!assignment.isEmpty()) {
                statement += " name = ?";
            }
            statement += "))";
        }
        //System.out.println(statement);
        pstmt = connection.prepareStatement(statement);//"SELECT * FROM result WHERE assignment_submission_id IN (SELECT id FROM assignment_submission WHERE assignment_catalog_id IN (SELECT id FROM assignment_catalog WHERE course_id IN (SELECT id FROM course WHERE name = ?) AND name = ?))");
        if (!course.isEmpty()) {
            pstmt.setString(1, course);
            if (!assignment.isEmpty()) {
                pstmt.setString(2, assignment);
            }
        } else {
            if (!assignment.isEmpty()) {
                pstmt.setString(1, assignment);
            }
        }
        return pstmt.executeQuery();
    }

    @Override
    public ResultSet getResultsForAll(String onyen, String assignment, String type, String course, String section, String year, String season) throws SQLException {
        //System.out.println(onyen + ", " + assignment + ", " + course + ", " + year + ", " + season);
        StringBuilder statement = new StringBuilder();
        int count = 0;
        statement.append("SELECT * FROM (\n");
        if (onyen != null && !onyen.isEmpty()) {
            count++;
            statement.append("SELECT * FROM result WHERE assignment_submission_id IN (SELECT id FROM assignment_submission WHERE user_uid IN (SELECT uid FROM user WHERE onyen = ?))");
        }
        if (assignment != null && !assignment.isEmpty()) {
            if (count > 0) {
                statement.append("\nUNION ALL\n");
            }
            count++;
            statement.append("SELECT * FROM result WHERE assignment_submission_id IN (SELECT id FROM assignment_submission WHERE assignment_catalog_id IN (SELECT id FROM assignment_catalog WHERE name = ?))");
        }
        if (type != null && !type.isEmpty()) {
            if (count > 0) {
                statement.append("\nUNION ALL\n");
            }
            count++;
            statement.append("SELECT * FROM result WHERE assignment_submission_id IN (SELECT id FROM assignment_submission WHERE assignment_catalog_id IN (SELECT id FROM assignment_catalog WHERE assignment_type_id IN (SELECT id FROM assignment_type WHERE name = ?)))");
        }
        if (course != null && !course.isEmpty()) {
            if (count > 0) {
                statement.append("\nUNION ALL\n");
            }
            count++;
            statement.append("SELECT * FROM result WHERE assignment_submission_id IN (SELECT id FROM assignment_submission WHERE assignment_catalog_id IN (SELECT id FROM assignment_catalog WHERE course_id IN (SELECT id FROM course WHERE name = ?)))");
        }
        if (section != null && !section.isEmpty()) {
            count++;
            if (count > 0) {
                statement.append("\nUNION ALL\n");
            }
            statement.append("SELECT * FROM result WHERE assignment_submission_id IN (SELECT id FROM assignment_submission WHERE assignment_catalog_id IN (SELECT id FROM assignment_catalog WHERE course_id IN (SELECT id FROM course WHERE section = ?)))");
        }
        if (year != null && !year.isEmpty()) {
            if (count > 0) {
                statement.append("\nUNION ALL\n");
            }
            count++;
            statement.append("SELECT * FROM result WHERE assignment_submission_id IN (SELECT id FROM assignment_submission WHERE assignment_catalog_id IN (SELECT id FROM assignment_catalog WHERE course_id IN (SELECT id FROM course WHERE term_id IN (SELECT id FROM term WHERE year = ?))))");
        }
        if (season != null && !season.isEmpty()) {
            if (count > 0) {
                statement.append("\nUNION ALL\n");
            }
            count++;
            statement.append("SELECT * FROM result WHERE assignment_submission_id IN (SELECT id FROM assignment_submission WHERE assignment_catalog_id IN (SELECT id FROM assignment_catalog WHERE course_id IN (SELECT id FROM course WHERE term_id IN (SELECT id FROM term WHERE season = ?))))");
        }
        if (count > 0) {
            statement.append(")\n AS result GROUP BY id HAVING count(*) = ").append(count);
        } else {
            statement.setLength(0);
            statement.append("SELECT * FROM result");
        }
        statement.append("\nORDER BY date DESC;");
        //System.out.println(statement.toString());
        PreparedStatement pstmt = connection.prepareStatement(statement.toString());
        int i = 1;
        if (onyen != null && !onyen.isEmpty()) {
            pstmt.setString(i++, onyen);
        }
        if (assignment != null && !assignment.isEmpty()) {
            pstmt.setString(i++, assignment);
        }
        if (type != null && !type.isEmpty()) {
            pstmt.setString(i++, type);
        }
        if (course != null && !course.isEmpty()) {
            pstmt.setString(i++, course);
        }
        if (section != null && !section.isEmpty()) {
            pstmt.setInt(i++, Integer.parseInt(section));
        }
        if (year != null && !year.isEmpty()) {
            pstmt.setInt(i++, Integer.parseInt(year));
        }
        if (season != null && !season.isEmpty()) {
            pstmt.setString(i, season);
        }
        return pstmt.executeQuery();
    }
    
    @Override
    public ResultSet getResultsForAllPaged(String onyen, String assignment, String type, String course, String section, String year, String season, int page, int pageSize) throws SQLException {
        //System.out.println(onyen + ", " + assignment + ", " + course + ", " + year + ", " + season);
        StringBuilder statement = new StringBuilder();
        int count = 0;
        statement.append("SELECT * FROM (\n");
        if (onyen != null && !onyen.isEmpty()) {
            count++;
            statement.append("SELECT * FROM result WHERE assignment_submission_id IN (SELECT id FROM assignment_submission WHERE user_uid IN (SELECT uid FROM user WHERE onyen = ?))");
        }
        if (assignment != null && !assignment.isEmpty()) {
            if (count > 0) {
                statement.append("\nUNION ALL\n");
            }
            count++;
            statement.append("SELECT * FROM result WHERE assignment_submission_id IN (SELECT id FROM assignment_submission WHERE assignment_catalog_id IN (SELECT id FROM assignment_catalog WHERE name = ?))");
        }
        if (type != null && !type.isEmpty()) {
            if (count > 0) {
                statement.append("\nUNION ALL\n");
            }
            count++;
            statement.append("SELECT * FROM result WHERE assignment_submission_id IN (SELECT id FROM assignment_submission WHERE assignment_catalog_id IN (SELECT id FROM assignment_catalog WHERE assignment_type_id IN (SELECT id FROM assignment_type WHERE name = ?)))");
        }
        if (course != null && !course.isEmpty()) {
            if (count > 0) {
                statement.append("\nUNION ALL\n");
            }
            count++;
            statement.append("SELECT * FROM result WHERE assignment_submission_id IN (SELECT id FROM assignment_submission WHERE assignment_catalog_id IN (SELECT id FROM assignment_catalog WHERE course_id IN (SELECT id FROM course WHERE name = ?)))");
        }
        if (section != null && !section.isEmpty()) {
            count++;
            if (count > 0) {
                statement.append("\nUNION ALL\n");
            }
            statement.append("SELECT * FROM result WHERE assignment_submission_id IN (SELECT id FROM assignment_submission WHERE assignment_catalog_id IN (SELECT id FROM assignment_catalog WHERE course_id IN (SELECT id FROM course WHERE section = ?)))");
        }
        if (year != null && !year.isEmpty()) {
            if (count > 0) {
                statement.append("\nUNION ALL\n");
            }
            count++;
            statement.append("SELECT * FROM result WHERE assignment_submission_id IN (SELECT id FROM assignment_submission WHERE assignment_catalog_id IN (SELECT id FROM assignment_catalog WHERE course_id IN (SELECT id FROM course WHERE term_id IN (SELECT id FROM term WHERE year = ?))))");
        }
        if (season != null && !season.isEmpty()) {
            if (count > 0) {
                statement.append("\nUNION ALL\n");
            }
            count++;
            statement.append("SELECT * FROM result WHERE assignment_submission_id IN (SELECT id FROM assignment_submission WHERE assignment_catalog_id IN (SELECT id FROM assignment_catalog WHERE course_id IN (SELECT id FROM course WHERE term_id IN (SELECT id FROM term WHERE season = ?))))");
        }
        if (count > 0) {
            statement.append(")\n AS result GROUP BY id HAVING count(*) = ").append(count);
        } else {
            statement.setLength(0);
            statement.append("SELECT * FROM result");
        }
        statement.append("\nORDER BY date DESC");
        statement.append("\nLIMIT ").append(pageSize);
        if(page > 0) {
            statement.append("\nOFFSET ").append(page * pageSize);
        }
        statement.append(";");
        //System.out.println(statement.toString());
        PreparedStatement pstmt = connection.prepareStatement(statement.toString());
        int i = 1;
        if (onyen != null && !onyen.isEmpty()) {
            pstmt.setString(i++, onyen);
        }
        if (assignment != null && !assignment.isEmpty()) {
            pstmt.setString(i++, assignment);
        }
        if (type != null && !type.isEmpty()) {
            pstmt.setString(i++, type);
        }
        if (course != null && !course.isEmpty()) {
            pstmt.setString(i++, course);
        }
        if (section != null && !section.isEmpty()) {
            pstmt.setInt(i++, Integer.parseInt(section));
        }
        if (year != null && !year.isEmpty()) {
            pstmt.setInt(i++, Integer.parseInt(year));
        }
        if (season != null && !season.isEmpty()) {
            pstmt.setString(i, season);
        }
        //System.out.println(pstmt.toString());
        return pstmt.executeQuery();
    }
    
    @Override
    public ResultSet getResultCountForAll(String onyen, String assignment, String type, String course, String section, String year, String season) throws SQLException {
        //System.out.println(onyen + ", " + assignment + ", " + course + ", " + year + ", " + season);
        StringBuilder statement = new StringBuilder();
        int count = 0;
        statement.append("SELECT COUNT(*) \"Total\" FROM (\n");
        if (onyen != null && !onyen.isEmpty()) {
            count++;
            statement.append("SELECT * FROM result WHERE assignment_submission_id IN (SELECT id FROM assignment_submission WHERE user_uid IN (SELECT uid FROM user WHERE onyen = ?))");
        }
        if (assignment != null && !assignment.isEmpty()) {
            if (count > 0) {
                statement.append("\nUNION ALL\n");
            }
            count++;
            statement.append("SELECT * FROM result WHERE assignment_submission_id IN (SELECT id FROM assignment_submission WHERE assignment_catalog_id IN (SELECT id FROM assignment_catalog WHERE name = ?))");
        }
        if (type != null && !type.isEmpty()) {
            if (count > 0) {
                statement.append("\nUNION ALL\n");
            }
            count++;
            statement.append("SELECT * FROM result WHERE assignment_submission_id IN (SELECT id FROM assignment_submission WHERE assignment_catalog_id IN (SELECT id FROM assignment_catalog WHERE assignment_type_id IN (SELECT id FROM assignment_type WHERE name = ?)))");
        }
        if (course != null && !course.isEmpty()) {
            if (count > 0) {
                statement.append("\nUNION ALL\n");
            }
            count++;
            statement.append("SELECT * FROM result WHERE assignment_submission_id IN (SELECT id FROM assignment_submission WHERE assignment_catalog_id IN (SELECT id FROM assignment_catalog WHERE course_id IN (SELECT id FROM course WHERE name = ?)))");
        }
        if (section != null && !section.isEmpty()) {
            count++;
            if (count > 0) {
                statement.append("\nUNION ALL\n");
            }
            statement.append("SELECT * FROM result WHERE assignment_submission_id IN (SELECT id FROM assignment_submission WHERE assignment_catalog_id IN (SELECT id FROM assignment_catalog WHERE course_id IN (SELECT id FROM course WHERE section = ?)))");
        }
        if (year != null && !year.isEmpty()) {
            if (count > 0) {
                statement.append("\nUNION ALL\n");
            }
            count++;
            statement.append("SELECT * FROM result WHERE assignment_submission_id IN (SELECT id FROM assignment_submission WHERE assignment_catalog_id IN (SELECT id FROM assignment_catalog WHERE course_id IN (SELECT id FROM course WHERE term_id IN (SELECT id FROM term WHERE year = ?))))");
        }
        if (season != null && !season.isEmpty()) {
            if (count > 0) {
                statement.append("\nUNION ALL\n");
            }
            count++;
            statement.append("SELECT * FROM result WHERE assignment_submission_id IN (SELECT id FROM assignment_submission WHERE assignment_catalog_id IN (SELECT id FROM assignment_catalog WHERE course_id IN (SELECT id FROM course WHERE term_id IN (SELECT id FROM term WHERE season = ?))))");
        }
        if (count > 0) {
            statement.append("\n GROUP BY id HAVING count(*) = ").append(count).append(") AS results;");
        } else {
            statement.setLength(0);
            statement.append("SELECT COUNT(*) \"Total\" FROM result;");
        }
        //System.out.println(statement.toString());
        PreparedStatement pstmt = connection.prepareStatement(statement.toString());
        int i = 1;
        if (onyen != null && !onyen.isEmpty()) {
            pstmt.setString(i++, onyen);
        }
        if (assignment != null && !assignment.isEmpty()) {
            pstmt.setString(i++, assignment);
        }
        if (type != null && !type.isEmpty()) {
            pstmt.setString(i++, type);
        }
        if (course != null && !course.isEmpty()) {
            pstmt.setString(i++, course);
        }
        if (section != null && !section.isEmpty()) {
            pstmt.setInt(i++, Integer.parseInt(section));
        }
        if (year != null && !year.isEmpty()) {
            pstmt.setInt(i++, Integer.parseInt(year));
        }
        if (season != null && !season.isEmpty()) {
            pstmt.setString(i, season);
        }
        //System.out.println(pstmt.toString());
        return pstmt.executeQuery();
    }
    
    @Override
    public ResultSet getNoteCountForAll(String onyen, String assignment, String type, String course, String section, String year, String season) throws SQLException {
        //System.out.println(onyen + ", " + assignment + ", " + course + ", " + year + ", " + season);
        StringBuilder statement = new StringBuilder();
        int count = 0;
        statement.append("SELECT COUNT(*) \"Total\" FROM (\n");
        statement.append("SELECT * FROM test_note WHERE grading_test_id IN (");
        statement.append("SELECT id FROM grading_test WHERE grading_part_id IN (");
        if (onyen != null && !onyen.isEmpty()) {
            count++;
            statement.append("SELECT id FROM result WHERE assignment_submission_id IN (SELECT id FROM assignment_submission WHERE user_uid IN (SELECT uid FROM user WHERE onyen = ?))");
        }
        if (assignment != null && !assignment.isEmpty()) {
            if (count > 0) {
                statement.append("\nUNION ALL\n");
            }
            count++;
            statement.append("SELECT id FROM result WHERE assignment_submission_id IN (SELECT id FROM assignment_submission WHERE assignment_catalog_id IN (SELECT id FROM assignment_catalog WHERE name = ?))");
        }
        if (type != null && !type.isEmpty()) {
            if (count > 0) {
                statement.append("\nUNION ALL\n");
            }
            count++;
            statement.append("SELECT id FROM result WHERE assignment_submission_id IN (SELECT id FROM assignment_submission WHERE assignment_catalog_id IN (SELECT id FROM assignment_catalog WHERE assignment_type_id IN (SELECT id FROM assignment_type WHERE name = ?)))");
        }
        if (course != null && !course.isEmpty()) {
            if (count > 0) {
                statement.append("\nUNION ALL\n");
            }
            count++;
            statement.append("SELECT id FROM result WHERE assignment_submission_id IN (SELECT id FROM assignment_submission WHERE assignment_catalog_id IN (SELECT id FROM assignment_catalog WHERE course_id IN (SELECT id FROM course WHERE name = ?)))");
        }
        if (section != null && !section.isEmpty()) {
            count++;
            if (count > 0) {
                statement.append("\nUNION ALL\n");
            }
            statement.append("SELECT id FROM result WHERE assignment_submission_id IN (SELECT id FROM assignment_submission WHERE assignment_catalog_id IN (SELECT id FROM assignment_catalog WHERE course_id IN (SELECT id FROM course WHERE section = ?)))");
        }
        if (year != null && !year.isEmpty()) {
            if (count > 0) {
                statement.append("\nUNION ALL\n");
            }
            count++;
            statement.append("SELECT id FROM result WHERE assignment_submission_id IN (SELECT id FROM assignment_submission WHERE assignment_catalog_id IN (SELECT id FROM assignment_catalog WHERE course_id IN (SELECT id FROM course WHERE term_id IN (SELECT id FROM term WHERE year = ?))))");
        }
        if (season != null && !season.isEmpty()) {
            if (count > 0) {
                statement.append("\nUNION ALL\n");
            }
            count++;
            statement.append("SELECT id FROM result WHERE assignment_submission_id IN (SELECT id FROM assignment_submission WHERE assignment_catalog_id IN (SELECT id FROM assignment_catalog WHERE course_id IN (SELECT id FROM course WHERE term_id IN (SELECT id FROM term WHERE season = ?))))");
        }
        if (count > 0) {
            statement.append("))\n GROUP BY id HAVING count(*) = ").append(count).append(") AS notes;");
        } else {
            statement.setLength(0);
            statement.append("SELECT COUNT(*) \"Total\" FROM (SELECT * FROM test_note WHERE grading_test_id IN (SELECT id FROM result)) AS notes;");
        }
        //System.out.println(statement.toString());
        PreparedStatement pstmt = connection.prepareStatement(statement.toString());
        int i = 1;
        if (onyen != null && !onyen.isEmpty()) {
            pstmt.setString(i++, onyen);
        }
        if (assignment != null && !assignment.isEmpty()) {
            pstmt.setString(i++, assignment);
        }
        if (type != null && !type.isEmpty()) {
            pstmt.setString(i++, type);
        }
        if (course != null && !course.isEmpty()) {
            pstmt.setString(i++, course);
        }
        if (section != null && !section.isEmpty()) {
            pstmt.setInt(i++, Integer.parseInt(section));
        }
        if (year != null && !year.isEmpty()) {
            pstmt.setInt(i++, Integer.parseInt(year));
        }
        if (season != null && !season.isEmpty()) {
            pstmt.setString(i, season);
        }
        //System.out.println(pstmt.toString());
        return pstmt.executeQuery();
    }
    
    @Override
    public ResultSet getCommentCountForAll(String onyen, String assignment, String type, String course, String section, String year, String season) throws SQLException {
        //System.out.println(onyen + ", " + assignment + ", " + course + ", " + year + ", " + season);
        StringBuilder statement = new StringBuilder();
        int count = 0;
        statement.append("SELECT COUNT(*) \"Total\" FROM (\n");
        statement.append("SELECT * FROM comment WHERE result_id IN (");
        if (onyen != null && !onyen.isEmpty()) {
            count++;
            statement.append("SELECT id FROM result WHERE assignment_submission_id IN (SELECT id FROM assignment_submission WHERE user_uid IN (SELECT uid FROM user WHERE onyen = ?))");
        }
        if (assignment != null && !assignment.isEmpty()) {
            if (count > 0) {
                statement.append("\nUNION ALL\n");
            }
            count++;
            statement.append("SELECT id FROM result WHERE assignment_submission_id IN (SELECT id FROM assignment_submission WHERE assignment_catalog_id IN (SELECT id FROM assignment_catalog WHERE name = ?))");
        }
        if (type != null && !type.isEmpty()) {
            if (count > 0) {
                statement.append("\nUNION ALL\n");
            }
            count++;
            statement.append("SELECT id FROM result WHERE assignment_submission_id IN (SELECT id FROM assignment_submission WHERE assignment_catalog_id IN (SELECT id FROM assignment_catalog WHERE assignment_type_id IN (SELECT id FROM assignment_type WHERE name = ?)))");
        }
        if (course != null && !course.isEmpty()) {
            if (count > 0) {
                statement.append("\nUNION ALL\n");
            }
            count++;
            statement.append("SELECT id FROM result WHERE assignment_submission_id IN (SELECT id FROM assignment_submission WHERE assignment_catalog_id IN (SELECT id FROM assignment_catalog WHERE course_id IN (SELECT id FROM course WHERE name = ?)))");
        }
        if (section != null && !section.isEmpty()) {
            count++;
            if (count > 0) {
                statement.append("\nUNION ALL\n");
            }
            statement.append("SELECT id FROM result WHERE assignment_submission_id IN (SELECT id FROM assignment_submission WHERE assignment_catalog_id IN (SELECT id FROM assignment_catalog WHERE course_id IN (SELECT id FROM course WHERE section = ?)))");
        }
        if (year != null && !year.isEmpty()) {
            if (count > 0) {
                statement.append("\nUNION ALL\n");
            }
            count++;
            statement.append("SELECT id FROM result WHERE assignment_submission_id IN (SELECT id FROM assignment_submission WHERE assignment_catalog_id IN (SELECT id FROM assignment_catalog WHERE course_id IN (SELECT id FROM course WHERE term_id IN (SELECT id FROM term WHERE year = ?))))");
        }
        if (season != null && !season.isEmpty()) {
            if (count > 0) {
                statement.append("\nUNION ALL\n");
            }
            count++;
            statement.append("SELECT id FROM result WHERE assignment_submission_id IN (SELECT id FROM assignment_submission WHERE assignment_catalog_id IN (SELECT id FROM assignment_catalog WHERE course_id IN (SELECT id FROM course WHERE term_id IN (SELECT id FROM term WHERE season = ?))))");
        }
        if (count > 0) {
            statement.append(")\n GROUP BY id HAVING count(*) = ").append(count).append(") AS comments;");
        } else  {
            statement.setLength(0);
            statement.append("SELECT COUNT(*) \"Total\" FROM (SELECT * FROM comment WHERE result_id IN (SELECT id FROM result)) AS comments;");
        }
        //System.out.println(statement.toString());
        PreparedStatement pstmt = connection.prepareStatement(statement.toString());
        int i = 1;
        if (onyen != null && !onyen.isEmpty()) {
            pstmt.setString(i++, onyen);
        }
        if (assignment != null && !assignment.isEmpty()) {
            pstmt.setString(i++, assignment);
        }
        if (type != null && !type.isEmpty()) {
            pstmt.setString(i++, type);
        }
        if (course != null && !course.isEmpty()) {
            pstmt.setString(i++, course);
        }
        if (section != null && !section.isEmpty()) {
            pstmt.setInt(i++, Integer.parseInt(section));
        }
        if (year != null && !year.isEmpty()) {
            pstmt.setInt(i++, Integer.parseInt(year));
        }
        if (season != null && !season.isEmpty()) {
            pstmt.setString(i, season);
        }
        //System.out.println(pstmt.toString());
        return pstmt.executeQuery();
    }

    @Override
    public ResultSet getGradingForResult(int id) throws SQLException {
        PreparedStatement pstmt = null;
        pstmt = connection.prepareStatement("SELECT * FROM grading_part WHERE result_id = ?");
        pstmt.setInt(1, id);
        return pstmt.executeQuery();
    }

    @Override
    public ResultSet getCommentsForResult(int id) throws SQLException {
        PreparedStatement pstmt = null;
        pstmt = connection.prepareStatement("SELECT * FROM comment WHERE result_id = ?");
        pstmt.setInt(1, id);
        return pstmt.executeQuery();
    }

    @Override
    public ResultSet getUserForResult(int id) throws SQLException {
        PreparedStatement pstmt = null;
        pstmt = connection.prepareStatement("SELECT * FROM user WHERE uid IN (SELECT user_uid FROM assignment_submission WHERE id IN (SELECT assignment_submission_id FROM result WHERE id = ?))");
        pstmt.setInt(1, id);
        return pstmt.executeQuery();
    }

    @Override
    public ResultSet getAssignmentForResult(int id) throws SQLException {
        PreparedStatement pstmt = null;
        pstmt = connection.prepareStatement("SELECT * FROM assignment_catalog WHERE id IN (SELECT assignment_catalog_id FROM assignment_submission WHERE id IN (SELECT assignment_submission_id FROM result WHERE id = ?))");
        pstmt.setInt(1, id);
        return pstmt.executeQuery();
    }

    public ResultSet getTypeForAssignment(int id) throws SQLException {
        PreparedStatement pstmt = null;
        pstmt = connection.prepareStatement("SELECT * FROM assignment_type WHERE id IN (SELECT assignment_type_id FROM assignment_catalog WHERE id = ?)");
        pstmt.setInt(1, id);
        return pstmt.executeQuery();
    }

    @Override
    public ResultSet getCourseForResult(int id) throws SQLException {
        PreparedStatement pstmt = null;
        pstmt = connection.prepareStatement("SELECT * FROM course WHERE id IN (SELECT course_id FROM assignment_catalog WHERE id IN (SELECT assignment_catalog_id FROM assignment_submission WHERE id IN (SELECT assignment_submission_id FROM result WHERE id = ?)))");
        pstmt.setInt(1, id);
        return pstmt.executeQuery();
    }

    @Override
    public ResultSet getTestsForGrading(int id) throws SQLException {
        PreparedStatement pstmt = null;
        pstmt = connection.prepareStatement("SELECT * FROM grading_test WHERE grading_part_id = ?");
        pstmt.setInt(1, id);
        return pstmt.executeQuery();
    }

    @Override
    public ResultSet getNotesForTest(int id) throws SQLException {
        PreparedStatement pstmt = null;
        pstmt = connection.prepareStatement("SELECT * FROM test_note WHERE grading_test_id = ?");
        pstmt.setInt(1, id);
        return pstmt.executeQuery();
    }

    @Override
    public ResultSet getUsers() throws SQLException {
        PreparedStatement pstmt = null;
        pstmt = connection.prepareStatement("SELECT * FROM user");
        return pstmt.executeQuery();
    }

    @Override
    public ResultSet getAssignments() throws SQLException {
        PreparedStatement pstmt = null;
        pstmt = connection.prepareStatement("SELECT * FROM assignment_catalog");
        return pstmt.executeQuery();
    }

    @Override
    public ResultSet getAssignments(String type, String course, String section, String year, String season) throws SQLException {
        //System.out.println(type + ", " + course + ", " + section + ", " + year + ", " + season);
        StringBuilder statement = new StringBuilder();
        int count = 0;
        statement.append("SELECT * FROM (\n");
        if (type != null && !type.isEmpty()) {
            count++;
            statement.append("SELECT * FROM assignment_catalog WHERE assignment_type_id IN (SELECT id FROM assignment_type WHERE name = ?)");
        }
        if (course != null && !course.isEmpty()) {
            count++;
            if (statement.length() > 16) {
                statement.append("\nUNION ALL\n");
            }
            statement.append("SELECT * FROM assignment_catalog WHERE course_id IN (SELECT id FROM course WHERE name = ?)");
        }
        if (section != null && !section.isEmpty()) {
            count++;
            if (statement.length() > 16) {
                statement.append("\nUNION ALL\n");
            }
            statement.append("SELECT * FROM assignment_catalog WHERE course_id IN (SELECT id FROM course WHERE section = ?)");
        }
        if (year != null && !year.isEmpty()) {
            count++;
            if (statement.length() > 16) {
                statement.append("\nUNION ALL\n");
            }
            statement.append("SELECT * FROM assignment_catalog WHERE course_id IN (SELECT id FROM course WHERE term_id IN (SELECT id FROM term WHERE year = ?))");
        }
        if (season != null && !season.isEmpty()) {
            count++;
            if (statement.length() > 16) {
                statement.append("\nUNION ALL\n");
            }
            statement.append("SELECT * FROM assignment_catalog WHERE course_id IN (SELECT id FROM course WHERE term_id IN (SELECT id FROM term WHERE season = ?))");
        }
        statement.append("\n) AS assignment_catalog GROUP BY id HAVING count(*) = ").append(count).append(";");
        if (count == 0) {
            statement.setLength(0);
            statement.append("SELECT * FROM assignment_catalog");
        }
        //System.out.println(statement.toString());
        PreparedStatement pstmt = connection.prepareStatement(statement.toString());
        int i = 1;
        if (type != null && !type.isEmpty()) {
            pstmt.setString(i++, type);
        }
        if (course != null && !course.isEmpty()) {
            pstmt.setString(i++, course);
        }
        if (section != null && !section.isEmpty()) {
            pstmt.setInt(i++, Integer.parseInt(section));
        }
        if (year != null && !year.isEmpty()) {
            pstmt.setInt(i++, Integer.parseInt(year));
        }
        if (season != null && !season.isEmpty()) {
            pstmt.setString(i, season);
        }
        //System.out.println(pstmt.toString());
        return pstmt.executeQuery();
    }

    @Override
    public ResultSet getCurrentAssignments(String type, String course, String section) throws SQLException {
        //System.out.println(type + ", " + course + ", " + section + ", " + year + ", " + season);
        StringBuilder statement = new StringBuilder(100);
        int count = 1;
        statement.append("SELECT * FROM (\n");
        statement.append("SELECT * FROM assignment_catalog WHERE course_id IN (SELECT id FROM course WHERE term_id IN (SELECT id FROM term WHERE current = TRUE))");
        if (type != null && !type.isEmpty()) {
            count++;
            statement.append("\nUNION ALL\n");
            statement.append("SELECT * FROM assignment_catalog WHERE assignment_type_id IN (SELECT id FROM assignment_type WHERE name = ?)");
        }
        if (course != null && !course.isEmpty()) {
            count++;
            statement.append("\nUNION ALL\n");
            statement.append("SELECT * FROM assignment_catalog WHERE course_id IN (SELECT id FROM course WHERE name = ?)");
        }
        if (section != null && !section.isEmpty()) {
            count++;
            statement.append("\nUNION ALL\n");
            statement.append("SELECT * FROM assignment_catalog WHERE course_id IN (SELECT id FROM course WHERE section = ?)");
        }
        statement.append("\n) AS assignment_catalog GROUP BY id HAVING count(*) = ").append(count).append(";");
        if (count == 0) {
            statement.setLength(0);
            statement.append("SELECT * FROM assignment_catalog");
        }
        //System.out.println(statement.toString());
        PreparedStatement pstmt = connection.prepareStatement(statement.toString());
        int i = 1;
        if (type != null && !type.isEmpty()) {
            pstmt.setString(i++, type);
        }
        if (course != null && !course.isEmpty()) {
            pstmt.setString(i++, course);
        }
        if (section != null && !section.isEmpty()) {
            pstmt.setInt(i++, Integer.parseInt(section));
        }
        //System.out.println(pstmt.toString());
        return pstmt.executeQuery();
    }

    @Override
    public ResultSet getCourses() throws SQLException {
        PreparedStatement pstmt = null;
        pstmt = connection.prepareStatement("SELECT * FROM course");
        return pstmt.executeQuery();
    }

    @Override
    public ResultSet getCourses(String year, String season) throws SQLException {
        //System.out.println(onyen + ", " + assignment + ", " + course + ", " + year + ", " + season);
        StringBuilder statement = new StringBuilder(75);
        int count = 0;
        statement.append("SELECT DISTINCT name FROM (\n");
        if (year != null && !year.isEmpty()) {
            count++;
            statement.append("SELECT * FROM course WHERE term_id IN (SELECT id FROM term WHERE year = ?)");
        }
        if (season != null && !season.isEmpty()) {
            count++;
            if (statement.length() > 16) {
                statement.append("\nUNION ALL\n");
            }
            statement.append("SELECT * FROM course WHERE term_id IN (SELECT id FROM term WHERE season = ?)");
        }
        statement.append("\n) AS course GROUP BY id HAVING count(*) = ").append(count).append(";");
        if (count == 0) {
            statement.setLength(0);
            statement.append("SELECT DISTINCT name FROM course");
        }
        //System.out.println(statement.toString());
        PreparedStatement pstmt = connection.prepareStatement(statement.toString());
        int i = 1;
        if (year != null && !year.isEmpty()) {
            pstmt.setInt(i++, Integer.parseInt(year));
        }
        if (season != null && !season.isEmpty()) {
            pstmt.setString(i, season);
        }
        return pstmt.executeQuery();
    }

    @Override
    public ResultSet getCurrentCourses() throws SQLException {
        PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM course WHERE term_id IN (SELECT id FROM term WHERE current = TRUE)");
        return pstmt.executeQuery();
    }

    @Override
    public ResultSet getSections(String course, String year, String season) throws SQLException {
        //System.out.println(onyen + ", " + assignment + ", " + course + ", " + year + ", " + season);
        StringBuilder statement = new StringBuilder(50);
        int count = 0;
        statement.append("SELECT DISTINCT section FROM (\n");
        if (course != null && !course.isEmpty()) {
            count++;
            statement.append("SELECT * FROM course WHERE name = ?");
        }
        if (year != null && !year.isEmpty()) {
            count++;
            if (statement.length() > 16) {
                statement.append("\nUNION ALL\n");
            }
            statement.append("SELECT * FROM course WHERE term_id IN (SELECT id FROM term WHERE year = ?)");
        }
        if (season != null && !season.isEmpty()) {
            count++;
            if (statement.length() > 16) {
                statement.append("\nUNION ALL\n");
            }
            statement.append("SELECT * FROM course WHERE term_id IN (SELECT id FROM term WHERE season = ?)");
        }
        statement.append("\n) AS course GROUP BY id HAVING count(*) = ").append(count).append(";");
        if (count == 0) {
            statement.setLength(0);
            statement.append("SELECT DISTINCT section FROM course");
        }
        //System.out.println(statement.toString());
        PreparedStatement pstmt = connection.prepareStatement(statement.toString());
        int i = 1;
        if (course != null && !course.isEmpty()) {
            pstmt.setString(i++, course);
        }
        if (year != null && !year.isEmpty()) {
            pstmt.setInt(i++, Integer.parseInt(year));
        }
        if (season != null && !season.isEmpty()) {
            pstmt.setString(i, season);
        }
        return pstmt.executeQuery();
    }

    @Override
    public ResultSet getCurrentSections(String course) throws SQLException {
        StringBuilder statement = new StringBuilder(75);
        statement.append("SELECT DISTINCT section FROM (\n")
                .append("SELECT * FROM course WHERE name = ?")
                .append("\nUNION ALL\n")
                .append("SELECT * FROM course WHERE term_id IN (SELECT id FROM term WHERE current = TRUE)")
                .append("\n) AS course GROUP BY id HAVING count(*) = 2;");
        //System.out.println(statement.toString());
        PreparedStatement pstmt = connection.prepareStatement(statement.toString());
        if (course != null && !course.isEmpty()) {
            pstmt.setString(1, course);
        }
        return pstmt.executeQuery();
    }

    @Override
    public ResultSet getTerms() throws SQLException {
        PreparedStatement pstmt = null;
        pstmt = connection.prepareStatement("SELECT * FROM term");
        return pstmt.executeQuery();
    }

    @Override
    public ResultSet getTypes() throws SQLException {
        PreparedStatement pstmt = null;
        pstmt = connection.prepareStatement("SELECT * FROM assignment_type");
        return pstmt.executeQuery();
    }

    @Override
    public ResultSet getAdminForUser(String onyen) throws SQLException {
        PreparedStatement pstmt = null;
        pstmt = connection.prepareStatement("SELECT * FROM admin WHERE user_uid IN (SELECT uid FROM user WHERE onyen = ?)");
        pstmt.setString(1, onyen);
        return pstmt.executeQuery();
    }

    @Override
    public void close() throws SQLException {
        disconnect();
    }
}
