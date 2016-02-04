package edu.unc.cs.niograderserver.pages;

import edu.unc.cs.htmlBuilder.HTMLFile;
import edu.unc.cs.htmlBuilder.IHTMLFile;
import edu.unc.cs.htmlBuilder.attributes.LinkTarget;
import edu.unc.cs.htmlBuilder.body.Body;
import edu.unc.cs.htmlBuilder.body.Division;
import edu.unc.cs.htmlBuilder.body.Header;
import edu.unc.cs.htmlBuilder.body.HorizontalRule;
import edu.unc.cs.htmlBuilder.body.Hyperlink;
import edu.unc.cs.htmlBuilder.body.IBody;
import edu.unc.cs.htmlBuilder.body.IDivision;
import edu.unc.cs.htmlBuilder.body.IHyperlink;
import edu.unc.cs.htmlBuilder.body.ISpan;
import edu.unc.cs.htmlBuilder.form.input.HiddenField;
import edu.unc.cs.htmlBuilder.form.input.IHiddenField;
import edu.unc.cs.htmlBuilder.body.LineBreak;
import edu.unc.cs.htmlBuilder.body.Span;
import edu.unc.cs.htmlBuilder.body.Text;
import edu.unc.cs.htmlBuilder.doctype.HTML5Doctype;
import edu.unc.cs.htmlBuilder.form.Form;
import edu.unc.cs.htmlBuilder.form.IForm;
import edu.unc.cs.htmlBuilder.form.ILabel;
import edu.unc.cs.htmlBuilder.form.IOption;
import edu.unc.cs.htmlBuilder.form.ISelect;
import edu.unc.cs.htmlBuilder.form.Label;
import edu.unc.cs.htmlBuilder.form.Option;
import edu.unc.cs.htmlBuilder.form.Select;
import edu.unc.cs.htmlBuilder.form.input.ISubmitButton;
import edu.unc.cs.htmlBuilder.form.input.SubmitButton;
import edu.unc.cs.htmlBuilder.head.Head;
import edu.unc.cs.htmlBuilder.head.IHead;
import edu.unc.cs.htmlBuilder.head.ILink;
import edu.unc.cs.htmlBuilder.head.IMetaAttr;
import edu.unc.cs.htmlBuilder.head.ITitle;
import edu.unc.cs.htmlBuilder.head.Link;
import edu.unc.cs.htmlBuilder.head.MetaAttr;
import edu.unc.cs.htmlBuilder.head.Title;
import edu.unc.cs.htmlBuilder.table.ITable;
import edu.unc.cs.htmlBuilder.table.ITableData;
import edu.unc.cs.htmlBuilder.table.ITableHeader;
import edu.unc.cs.htmlBuilder.table.ITableRow;
import edu.unc.cs.htmlBuilder.table.Table;
import edu.unc.cs.htmlBuilder.table.TableData;
import edu.unc.cs.htmlBuilder.table.TableHeader;
import edu.unc.cs.htmlBuilder.table.TableRow;
import edu.unc.cs.httpserver.util.ResponseStatusNotice;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import edu.unc.cs.niograderserver.pages.parts.StudentDataNavBar;
import edu.unc.cs.niograderserver.pages.sql.DatabaseReader;
import edu.unc.cs.niograderserver.pages.sql.IDatabaseReader;
import edu.unc.cs.niograderserver.utils.ConfigReader;
import edu.unc.cs.niograderserver.utils.IConfigReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.httpclient.HttpStatus;

public class StudentDataLookupPageGenerator extends HTMLFile implements IStudentDataLookupPageGenerator {

    private static final Logger LOG = Logger.getLogger(StudentDataLookupPageGenerator.class.getName());

    
    private static final String AUTH_KEY;
    private static final boolean SHOW_ADMIN_ONYEN;
    private static final boolean SHOW_USER_ONYEN;
    private static final boolean DO_CHECK_AUTH;
    private static final int PAGE_SIZE = 50;

    static {
        StringBuilder auth = new StringBuilder(50);
        boolean userOnyen = false;
        boolean adminOnyen = false;
        boolean checkAuthTmp = false;
        IConfigReader config = null;
        try {
            config = new ConfigReader("config/auth.properties");
            checkAuthTmp = config.getBoolean("database.view.checkAuth").orElse(false);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        if (checkAuthTmp) {
            Random rand = new Random();
            while (auth.length() < 50) {
                auth.append((char) rand.nextInt());
            }
            try {
                if (config != null) {
                    config.getString("database.view.authKey").ifPresent((key) -> {
                        auth.setLength(0);
                        auth.append(key);
                    });
                }
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
        try {
            IConfigReader properties = new ConfigReader("config/config.properties");
            userOnyen = properties.getBoolean("database.view.showUserOnyen", false).get();
            adminOnyen = properties.getBoolean("database.view.showAdminOnyen", false).get();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        AUTH_KEY = auth.toString();
        SHOW_USER_ONYEN = userOnyen;
        SHOW_ADMIN_ONYEN = adminOnyen;
        DO_CHECK_AUTH = checkAuthTmp;
    }
    private final String[] methods = new String[]{"GET", "POST"};
    
    @Override
    public String getPage(Optional<FileItem[]> request) throws ResponseStatusNotice {
        
        String assignment = "";
        String auth = "";
        String course = "";
        boolean isAdmin = false;
        String onyen = "";
        String season = "";
        String section = "";
        String type = "";
        String user = "";
        String year = "";
        String view = "";
        int page = 0;
        boolean newSearch = false;
        
        FileItem[] args = request.orElseThrow(() -> new ResponseStatusNotice(HttpStatus.SC_BAD_REQUEST));
        for (FileItem arg : args) {
            System.out.println(arg.getFieldName() + " --> " + arg.getString());
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
                case "season":
                    season = arg.getString();
                    break;
                case "section":
                    section = arg.getString();
                    break;
                case "type":
                    type = arg.getString();
                    break;
                case "year":
                    year = arg.getString();
                    break;
                case "user":
                    user = arg.getString();
                    isAdmin = checkAdmin(user);
                    break;
                case "auth":
                    auth = arg.getString();
                    break;
                case "view":
                    view = arg.getString();
                    break;
                case "assignment_data_submit":
                    newSearch = arg.getString().equals("Submit");
                    break;
                case "page":
                    try {
                        page += Integer.parseInt(arg.getString());
                    } catch (NumberFormatException e) {
                        LOG.log(Level.INFO, null, e);
                        page = 0;
                    }
                    break;
                case "newPage":
                    switch(arg.getString()) {
                        case "<< First":
                            page = 0;
                            break;
                        case "< Previous":
                            page --;
                            break;
                        case "Next >":
                            page ++;
                            break;
                        case "Last >>":
                            page = -1;
                            break;
                    }
                    if (arg.getString().equals("Next >")) {
                        page ++;
                    }
                    break;
            }
        }
        if (!checkAuth(auth)) {
            throw new ResponseStatusNotice(HttpStatus.SC_FORBIDDEN);
        }
        if (user == null || user.isEmpty()) {
            throw new ResponseStatusNotice(HttpStatus.SC_BAD_REQUEST);
        }
        
        int total = -1;
        try {
            IConfigReader config = new ConfigReader(Paths.get("config", "config.properties").toString());
            
            IDatabaseReader dr = new DatabaseReader();
            dr.connect(config.getString("database.username").orElseThrow(IllegalArgumentException::new),
                    config.getString("database.password").orElseThrow(IllegalArgumentException::new),
                    "jdbc:" + config.getString("database.url").orElseThrow(IllegalArgumentException::new));

            ResultSet results = null;
            switch (view) {
                case "Submissions":
                case "Grading":
                    results = dr.getResultCountForAll(doShowOnyen(isAdmin) ? onyen : "", assignment, type, course, section, year, season);
                    break;
                case "Comments":
                    results = dr.getCommentCountForAll(doShowOnyen(isAdmin) ? onyen : "", assignment, type, course, section, year, season);
                    break;
                case "Notes":
                    results = dr.getNoteCountForAll(doShowOnyen(isAdmin) ? onyen : "", assignment, type, course, section, year, season);
                    break;
            }
            if (results != null) {
                results.first();
                total = results.getInt("Total");
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        
        System.out.println("Item count: " + total);
        
        int pageCount = total / PAGE_SIZE; // number of complete pages
        pageCount -= (total % PAGE_SIZE) > 0 ? 0 : 1; // since pages are 0 based, remove extra unless there is an incomplete page
        
        if (total > 0) {
            if (page == -1) {
                page = pageCount;
            }
            page = Math.min(page, pageCount);
        } else if (total == 0) {
            pageCount = 0;
        }else {
            pageCount = -1;
        }
        
        if (newSearch) {
            page = 0;
        }
        
        IHTMLFile html = buildHtmlFile(assignment, course, isAdmin, onyen, season, section, type, user, year, view, page, pageCount);
        return html.getHTML();
    }

    private IHTMLFile buildHtmlFile(String assignment, String course, boolean isAdmin, String onyen, String season,
            String section, String type, String user, String year, String view, int page, int pageCount) {
        IHTMLFile html = new HTMLFile();
        
        try {
            html.setDoctype(new HTML5Doctype());
            html.setHead(buildHead());
            html.setBody(buildBody(assignment, course, isAdmin, onyen, season, section, type, user, year, view, page, pageCount));
        } catch (FileNotFoundException e) {
            LOG.log(Level.FINER, null, e);
        } catch (IOException e) {
            LOG.log(Level.FINER, null, e);
        }
        return html;
    }
    
    private ITable buildAssignmentTable(String assignment, String course, boolean isAdmin, String onyen, String season,
            String section, String type, String year, String view, int page) throws FileNotFoundException, IOException {
        IDatabaseReader dr = new DatabaseReader();
        ITable table = new Table();
        table.setClassName("center");
        ResultSet results = null;
        try {
            IConfigReader config = new ConfigReader(Paths.get("config", "config.properties").toString());
            dr.connect(config.getString("database.username").orElseThrow(IllegalArgumentException::new),
                    config.getString("database.password").orElseThrow(IllegalArgumentException::new),
                    "jdbc:" + config.getString("database.url").orElseThrow(IllegalArgumentException::new));

            ITableRow headerRow = new TableRow();

            if (view.isEmpty() || view.equals("Submissions")) {
                if (doShowOnyen(isAdmin) && onyen.isEmpty()) {
                    headerRow.addDataPart(new TableHeader(new Text("Onyen")));
                }
                if (course.isEmpty()) {
                    headerRow.addDataPart(new TableHeader(new Text("Course")));
                }
                if (section.isEmpty()) {
                    headerRow.addDataPart(new TableHeader(new Text("Section")));
                }
                if (assignment.isEmpty()) {
                    headerRow.addDataPart(new TableHeader(new Text("Name")));
                }
                if (type.isEmpty()) {
                    headerRow.addDataPart(new TableHeader(new Text("Type")));
                }
                ITableHeader dateHeader = new TableHeader(new Text("Date/Time"));
                ITableHeader scoreHeader = new TableHeader(new Text("Score"));
                ITableHeader autoGradeHeader = new TableHeader(new Text("Autograded"));

                headerRow.addDataPart(dateHeader);
                headerRow.addDataPart(scoreHeader);
                headerRow.addDataPart(autoGradeHeader);
            } else if (view.equals("Comments")) {
                if (doShowOnyen(isAdmin) && onyen.isEmpty()) {
                    headerRow.addDataPart(new TableHeader(new Text("Onyen")));
                }
                if (course.isEmpty()) {
                    headerRow.addDataPart(new TableHeader(new Text("Course")));
                }
                if (section.isEmpty()) {
                    headerRow.addDataPart(new TableHeader(new Text("Section")));
                }
                if (assignment.isEmpty()) {
                    headerRow.addDataPart(new TableHeader(new Text("Name")));
                }
                if (type.isEmpty()) {
                    headerRow.addDataPart(new TableHeader(new Text("Type")));
                }
                ITableHeader dateHeader = new TableHeader(new Text("Date/Time"));
                ITableHeader numHeader = new TableHeader(new Text("#"));
                ITableHeader commentHeader = new TableHeader(new Text("Comment"));

                headerRow.addDataPart(dateHeader);
                headerRow.addDataPart(numHeader);
                headerRow.addDataPart(commentHeader);
            } else if (view.equals("Grading")) {
                if (doShowOnyen(isAdmin) && onyen.isEmpty()) {
                    headerRow.addDataPart(new TableHeader(new Text("Onyen")));
                }
                if (course.isEmpty()) {
                    headerRow.addDataPart(new TableHeader(new Text("Course")));
                }
                if (section.isEmpty()) {
                    headerRow.addDataPart(new TableHeader(new Text("Section")));
                }
                if (assignment.isEmpty()) {
                    headerRow.addDataPart(new TableHeader(new Text("Name")));
                }
                if (type.isEmpty()) {
                    headerRow.addDataPart(new TableHeader(new Text("Type")));
                }
                ITableHeader dateHeader = new TableHeader(new Text("Date/Time"));
                ITableHeader nameHeader = new TableHeader(new Text("Name"));
                ITableHeader pointsHeader = new TableHeader(new Text("Points"));
                ITableHeader possibleHeader = new TableHeader(new Text("Possible"));
                ITableHeader ecHeader = new TableHeader(new Text("Extra Credit"));
                ITableHeader autoGradeHeader = new TableHeader(new Text("Autograded"));

                headerRow.addDataPart(dateHeader);
                headerRow.addDataPart(nameHeader);
                headerRow.addDataPart(pointsHeader);
                headerRow.addDataPart(possibleHeader);
                headerRow.addDataPart(ecHeader);
                headerRow.addDataPart(autoGradeHeader);
            } else if (view.equals("Notes")) {
                if (doShowOnyen(isAdmin) && onyen.isEmpty()) {
                    headerRow.addDataPart(new TableHeader(new Text("Onyen")));
                }
                if (course.isEmpty()) {
                    headerRow.addDataPart(new TableHeader(new Text("Course")));
                }
                if (section.isEmpty()) {
                    headerRow.addDataPart(new TableHeader(new Text("Section")));
                }
                if (assignment.isEmpty()) {
                    headerRow.addDataPart(new TableHeader(new Text("Name")));
                }
                if (type.isEmpty()) {
                    headerRow.addDataPart(new TableHeader(new Text("Type")));
                }
                ITableHeader dateHeader = new TableHeader(new Text("Date/Time"));
                ITableHeader partHeader = new TableHeader(new Text("Part"));
                ITableHeader testHeader = new TableHeader(new Text("Test"));
                ITableHeader noteHeader = new TableHeader(new Text("Note"));

                headerRow.addDataPart(dateHeader);
                headerRow.addDataPart(partHeader);
                headerRow.addDataPart(testHeader);
                headerRow.addDataPart(noteHeader);
            }
            table.addRow(headerRow);

            results = dr.getResultsForAllPaged(doShowOnyen(isAdmin) ? onyen : "", assignment, type, course, section, year, season, page, PAGE_SIZE);

            int i = 0;
            results.beforeFirst();
            while (results.next()) {
                if (view.isEmpty() || view.equals("Submissions")) {
                    ITableRow row = new TableRow();
                    if (i % 2 == 1) {
                        row.setClassName("highlight-row");
                    }
                    if (doShowOnyen(isAdmin) && onyen.isEmpty()) {
                        try (ResultSet users = dr.getUserForResult(results.getInt("id"))) {
                            users.first();
                            row.addDataPart(new TableData(new Text(users.getString("onyen"))));
                        }
                    }
                    if (course.isEmpty()) {
                        try (ResultSet courses = dr.getCourseForResult(results.getInt("id"))) {
                            courses.first();
                            row.addDataPart(new TableData(new Text(courses.getString("name"))));
                        }
                    }
                    if (section.isEmpty()) {
                        try (ResultSet courses = dr.getCourseForResult(results.getInt("id"))) {
                            courses.first();
                            row.addDataPart(new TableData(new Text(Integer.toString(courses.getInt("section")))));
                        }
                    }
                    if (assignment.isEmpty()) {
                        try (ResultSet assignments = dr.getAssignmentForResult(results.getInt("id"))) {
                            assignments.first();
                            row.addDataPart(new TableData(new Text(assignments.getString("name"))));
                        }
                    }
                    if (type.isEmpty()) {
                        try (ResultSet assignments = dr.getAssignmentForResult(results.getInt("id"))) {
                            assignments.first();
                            try (ResultSet assignmentType = dr.getTypeForAssignment(assignments.getInt("id"))) {
                                assignmentType.first();
                                row.addDataPart(new TableData(new Text(assignmentType.getString("name"))));
                            }
                        }
                    }
                    row.addDataPart(new TableData(new Text(DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(results.getTimestamp("date")))));

                    try (ResultSet grading = dr.getGradingForResult(results.getInt("id"))) {
                        double points = 0;
                        double possible = 0;
                        double autoGraded = 0;
                        while (grading.next()) {
                            points += grading.getDouble("points");
                            if (!grading.getBoolean("extra_credit")) {
                                possible += grading.getDouble("possible");
                            }
                            autoGraded += grading.getDouble("auto_graded_percent");
                        }
                        grading.last();
                        double score = points / possible;
                        score = Math.round(score * 1000.) / 10.;
                        autoGraded /= grading.getRow();
                        autoGraded = Math.round(autoGraded * 10.) / 10.;
                        row.addDataPart(new TableData(new Text(score + "%")));
                        row.addDataPart(new TableData(new Text(autoGraded + "%")));
                    }

                    table.addRow(row);
                } else if (view.equals("Comments")) {
                    TableData dateTime = new TableData(new Text(DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(results.getTimestamp("date"))));
                    try (ResultSet comments = dr.getCommentsForResult(results.getInt("id"))) {
                        ITableRow row = new TableRow();
                        ITableData date = new TableData();
                        date.addContent(new Text(DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(results.getTimestamp("date"))));
                        comments.last();
                        int dateSpan = comments.getRow();
                        comments.beforeFirst();
                        date.setRowSpan(dateSpan);
                        date.setClassName("highlight-row");
                        row.addDataPart(date);
                        int commentNum = 1;
                        while (comments.next()) {
                            if (i % 2 == 1) {
                                row.setClassName("highlight-row");
                            }
                            if (doShowOnyen(isAdmin) && onyen.isEmpty()) {
                                try (ResultSet users = dr.getUserForResult(results.getInt("id"))) {
                                    users.first();
                                    row.addDataPart(new TableData(new Text(users.getString("onyen"))));
                                }
                            }
                            if (course.isEmpty()) {
                                try (ResultSet courses = dr.getCourseForResult(results.getInt("id"))) {
                                    courses.first();
                                    row.addDataPart(new TableData(new Text(courses.getString("name"))));
                                }
                            }
                            if (section.isEmpty()) {
                                try (ResultSet courses = dr.getCourseForResult(results.getInt("id"))) {
                                    courses.first();
                                    row.addDataPart(new TableData(new Text(Integer.toString(courses.getInt("section")))));
                                }
                            }
                            if (assignment.isEmpty()) {
                                try (ResultSet assignments = dr.getAssignmentForResult(results.getInt("id"))) {
                                    assignments.first();
                                    row.addDataPart(new TableData(new Text(assignments.getString("name"))));
                                }
                            }
                            if (type.isEmpty()) {
                                try (ResultSet assignments = dr.getAssignmentForResult(results.getInt("id"))) {
                                    assignments.first();
                                    try (ResultSet assignmentType = dr.getTypeForAssignment(assignments.getInt("id"))) {
                                        assignmentType.first();
                                        row.addDataPart(new TableData(new Text(assignmentType.getString("name"))));
                                    }
                                }
                            }
                            row.addDataPart(dateTime);
                            row.addDataPart(new TableData(new Text("" + commentNum++)));
                            row.addDataPart(new TableData(new Text(comments.getString("comment"))));

                            table.addRow(row);
                            row = new TableRow();
                        }
                    }
                } else if (view.equals("Grading")) {
                    try (ResultSet grading = dr.getGradingForResult(results.getInt("id"))) {
                        ITableRow row = new TableRow();
                        ITableData date = new TableData();
                        date.addContent(new Text(DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(results.getTimestamp("date"))));
                        grading.last();
                        int dateSpan = grading.getRow();
                        grading.next();
                        while (grading.previous()) {
                            try (ResultSet tests = dr.getTestsForGrading(grading.getInt("id"))) {
                                tests.last();
                                dateSpan += tests.getRow();
                            }
                        }
                        if (dateSpan > 1) {
                            date.setRowSpan(dateSpan);
                        }
                        date.setClassName("highlight-row");
                        grading.beforeFirst();
                        if (doShowOnyen(isAdmin) && onyen.isEmpty()) {
                            try (ResultSet users = dr.getUserForResult(results.getInt("id"))) {
                                users.first();
                                ITableData userData = new TableData(new Text(users.getString("onyen")));
                                if (dateSpan > 1) {
                                    userData.setRowSpan(dateSpan);
                                }
                                userData.setClassName("highlight-row");
                                row.addDataPart(userData);
                            }
                        }
                        if (course.isEmpty()) {
                            try (ResultSet courses = dr.getCourseForResult(results.getInt("id"))) {
                                courses.first();
                                ITableData courseData = new TableData(new Text(courses.getString("name")));
                                if (dateSpan > 1) {
                                    courseData.setRowSpan(dateSpan);
                                }
                                courseData.setClassName("highlight-row");
                                row.addDataPart(courseData);
                            }
                        }
                        if (section.isEmpty()) {
                            try (ResultSet courses = dr.getCourseForResult(results.getInt("id"))) {
                                courses.first();
                                ITableData sectionData = new TableData(new Text(Integer.toString(courses.getInt("section"))));
                                if (dateSpan > 1) {
                                    sectionData.setRowSpan(dateSpan);
                                }
                                sectionData.setClassName("highlight-row");
                                row.addDataPart(sectionData);
                            }
                        }
                        if (assignment.isEmpty()) {
                            try (ResultSet assignments = dr.getAssignmentForResult(results.getInt("id"))) {
                                assignments.first();
                                ITableData assignmentData = new TableData(new Text(assignments.getString("name")));
                                if (dateSpan > 1) {
                                    assignmentData.setRowSpan(dateSpan);
                                }
                                assignmentData.setClassName("highlight-row");
                                row.addDataPart(assignmentData);
                            }
                        }
                        if (type.isEmpty()) {
                            try (ResultSet assignments = dr.getAssignmentForResult(results.getInt("id"))) {
                                assignments.first();
                                try (ResultSet assignmentType = dr.getTypeForAssignment(assignments.getInt("id"))) {
                                    assignmentType.first();
                                    ITableData typeData = new TableData(new Text(assignmentType.getString("name")));
                                    if (dateSpan > 1) {
                                        typeData.setRowSpan(dateSpan);
                                    }
                                    typeData.setClassName("highlight-row");
                                    row.addDataPart(typeData);
                                }
                            }
                        }
                        row.addDataPart(date);
                        while (grading.next()) {
                            row.setClassName("highlight-row");

                            row.addDataPart(new TableData(new Text(grading.getString("name"))));
                            row.addDataPart(new TableData(new Text("" + grading.getInt("points"))));
                            row.addDataPart(new TableData(new Text("" + grading.getInt("possible"))));
                            row.addDataPart(new TableData(new Text("" + (grading.getBoolean("extra_credit") ? "Yes" : "No"))));
                            row.addDataPart(new TableData(new Text("" + (Math.round(grading.getDouble("auto_graded_percent") * 100.) / 100.) + "%")));

                            table.addRow(row);
                            row = new TableRow();

                            try (ResultSet tests = dr.getTestsForGrading(grading.getInt("id"))) {
                                while (tests.next()) {
                                    row.addDataPart(new TableData(new Text(tests.getString("name"))));
                                    ITableData percentData = new TableData(new Text("" + (Math.round(tests.getDouble("percent") * 1000.) / 10.) + "%"));
                                    percentData.setColSpan(2);
                                    row.addDataPart(percentData);
                                    row.addDataPart(new TableData(new Text("")));
                                    row.addDataPart(new TableData(new Text("" + (tests.getBoolean("auto_graded") ? "Yes" : "No"))));

                                    table.addRow(row);
                                    row = new TableRow();
                                }
                            }
                        }
                    }
                } else if (view.equals("Notes")) {
                    try (ResultSet grading = dr.getGradingForResult(results.getInt("id"))) {
                        ITableRow row = new TableRow();
                        ITableData date = new TableData();
                        date.addContent(new Text(DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(results.getTimestamp("date"))));
                        int dateSpan = 0;
                        while (grading.next()) {
                            try (ResultSet tests = dr.getTestsForGrading(grading.getInt("id"))) {
                                while (tests.next()) {
                                    try (ResultSet notes = dr.getNotesForTest(tests.getInt("id"))) {
                                        notes.last();
                                        dateSpan += notes.getRow();
                                    }
                                }
                            }
                        }
                        if (dateSpan == 0) {
                            continue;
                        }
                        if (doShowOnyen(isAdmin) && onyen.isEmpty()) {
                            try (ResultSet users = dr.getUserForResult(results.getInt("id"))) {
                                users.first();
                                ITableData userData = new TableData(new Text(users.getString("onyen")));
                                if (dateSpan > 1) {
                                    userData.setRowSpan(dateSpan);
                                }
                                row.addDataPart(userData);
                            }
                        }
                        if (course.isEmpty()) {
                            try (ResultSet courses = dr.getCourseForResult(results.getInt("id"))) {
                                courses.first();
                                ITableData courseData = new TableData(new Text(courses.getString("name")));
                                if (dateSpan > 1) {
                                    courseData.setRowSpan(dateSpan);
                                }
                                row.addDataPart(courseData);
                            }
                        }
                        if (section.isEmpty()) {
                            try (ResultSet courses = dr.getCourseForResult(results.getInt("id"))) {
                                courses.first();
                                ITableData sectionData = new TableData(new Text(Integer.toString(courses.getInt("section"))));
                                if (dateSpan > 1) {
                                    sectionData.setRowSpan(dateSpan);
                                }
                                row.addDataPart(sectionData);
                            }
                        }
                        if (assignment.isEmpty()) {
                            try (ResultSet assignments = dr.getAssignmentForResult(results.getInt("id"))) {
                                assignments.first();
                                ITableData assignmentData = new TableData(new Text(assignments.getString("name")));
                                if (dateSpan > 1) {
                                    assignmentData.setRowSpan(dateSpan);
                                }
                                row.addDataPart(assignmentData);
                            }
                        }
                        if (type.isEmpty()) {
                            try (ResultSet assignments = dr.getAssignmentForResult(results.getInt("id"))) {
                                assignments.first();
                                try (ResultSet assignmentType = dr.getTypeForAssignment(assignments.getInt("id"))) {
                                    assignmentType.first();
                                    ITableData typeData = new TableData(new Text(assignmentType.getString("name")));
                                    if (dateSpan > 1) {
                                        typeData.setRowSpan(dateSpan);
                                    }
                                    row.addDataPart(typeData);
                                }
                            }
                        }
                        date.setRowSpan(dateSpan);
                        grading.beforeFirst();
                        row.addDataPart(date);
                        while (grading.next()) {
                            List<ITableRow> noteRows = new ArrayList<>();
                            try (ResultSet tests = dr.getTestsForGrading(grading.getInt("id"))) {
                                ITableData gradingName = new TableData(new Text(grading.getString("name")));
                                if (tests.isBeforeFirst()) {
                                    int gradingNameSpan = 0;
                                    while (tests.next()) {
                                        ITableData test = new TableData(new Text(tests.getString("name")));
                                        try (ResultSet notes = dr.getNotesForTest(tests.getInt("id"))) {
//                                            if (notes.isBeforeFirst()) {
//                                                notes.last();
//                                                gradingNameSpan += notes.getRow();
//                                            }
                                            if (notes.isBeforeFirst()) {
                                                notes.last();
                                                int testSpan = notes.getRow();
                                                gradingNameSpan += testSpan;
                                                notes.beforeFirst();
                                                if (testSpan > 1) {
                                                    test.setRowSpan(testSpan);
                                                }
                                                if (testSpan != 0) {
                                                    row.addDataPart(test);
                                                }
                                            }
                                            while (notes.next()) {
                                                if (i % 2 == 1) {
                                                    row.setClassName("highlight-row");
                                                }
                                                row.addDataPart(new TableData(new Text(notes.getString("note"))));
//                                                table.addRow(row);
                                                noteRows.add(row);
                                                row = new TableRow();
                                            }
                                        }
                                    }
//                                    tests.beforeFirst();
                                    if (gradingNameSpan > 1) {
                                        gradingName.setRowSpan(gradingNameSpan);
                                    }
//                                    if (gradingNameSpan > 0) {
//                                        row.addDataPart(gradingName);
//                                    }
                                    if (gradingNameSpan > 0) {
                                        if (noteRows.size() > 0) {
                                            noteRows.get(0).addDataPart(gradingName);
                                            for (ITableRow noteRow : noteRows) {
                                                table.addRow(noteRow);
                                            }
                                        }
                                    }
                                }
//                                while (tests.next()) {
//                                    ITableData test = new TableData(new Text(tests.getString("name")));
//
//                                    try (ResultSet notes = dr.getNotesForTest(tests.getInt("id"))) {
//                                        if (notes.isBeforeFirst()) {
//                                            notes.last();
//                                            int testSpan = notes.getRow();
//                                            notes.beforeFirst();
//                                            if (testSpan > 1) {
//                                                test.setRowSpan(testSpan);
//                                            }
//                                            if (testSpan != 0) {
//                                                row.addDataPart(test);
//                                            }
//                                        }
//                                        while (notes.next()) {
//                                            if (i % 2 == 1) {
//                                                row.setClassName("highlight-row");
//                                            }
//                                            row.addDataPart(new TableData(new Text(notes.getString("note"))));
//                                            table.addRow(row);
//                                            row = new TableRow();
//                                        }
//                                    }
//                                }
                            }
                        }
                    }
                }
                i++;
            }
        } catch (SQLException e) {
            LOG.log(Level.FINER, null, e);
        } finally {
            if (results != null) {
                try {
                    results.close();
                } catch (SQLException e) {
                    LOG.log(Level.FINER, null, e);
                }
            }
        }
        return table;
    }
    
    private IDivision buildPageNav(int currentPage, int pageCount) {
        IDivision pageNav = new Division();
        pageNav.setID("page-nav-bar");
        pageNav.setClass("center");
        
        IHiddenField page = buildHiddenField("page", Integer.toString(currentPage));
        page.setForm("assignment_data");
        pageNav.addContent(page);
        
        if (currentPage > 1) {
            SubmitButton first = new SubmitButton();
            first.setClassName("back-nav");
            first.setForm("assignment_data");
            first.setName("newPage");
            first.setValue("&lt;&lt; First");
            
            pageNav.addContent(first);
        }
        
        if (currentPage > 0) {
            SubmitButton prev = new SubmitButton();
            prev.setClassName("back-nav");
            prev.setForm("assignment_data");
            prev.setName("newPage");
            prev.setValue("&lt; Previous");
            
            pageNav.addContent(prev);
        }
        
        if (pageCount == -1) {
            SubmitButton next = new SubmitButton();
            next.setClassName("forward-nav");
            next.setForm("assignment_data");
            next.setName("newPage");
            next.setValue("Next &gt;");

            pageNav.addContent(next);
        } else {
            if (pageCount > currentPage) {
                SubmitButton next = new SubmitButton();
                next.setClassName("forward-nav");
                next.setForm("assignment_data");
                next.setName("newPage");
                next.setValue("Next &gt;");
                
                pageNav.addContent(next);
            }
            if (pageCount - 1 > currentPage) {
                SubmitButton next = new SubmitButton();
                next.setClassName("forward-nav");
                next.setForm("assignment_data");
                next.setName("newPage");
                next.setValue("Last &gt;&gt;");
                
                pageNav.addContent(next);
            }
        }
        
        pageNav.addContent(new LineBreak());
        
        return pageNav;
    }

    private IBody buildBody(String assignment, String course, boolean isAdmin, String onyen, String season, String section,
            String type, String user, String year, String view, int page, int pageCount) throws FileNotFoundException, IOException {
        IBody body = new Body();

        IDivision header = new Division();
        header.setClass("header-bar");

        ISpan userInfo = new Span();
        userInfo.setID("user-info");
        try {
            IDatabaseReader dr = new DatabaseReader();
            IConfigReader config = new ConfigReader(Paths.get("config", "config.properties").toString());
            dr.connect(config.getString("database.username").orElseThrow(IllegalArgumentException::new),
                    config.getString("database.password").orElseThrow(IllegalArgumentException::new),
                    "jdbc:" + config.getString("database.url").orElseThrow(IllegalArgumentException::new));
            ResultSet admins = dr.getAdminForUser(user);
            if (admins.first()) {
                userInfo.addContent(new Text(user + "&ndash;admin"));
            } else {
                userInfo.addContent(new Text(user));
            }
        } catch (SQLException e) {
            LOG.log(Level.FINER, null, e);
        }
        header.addContent(userInfo);

        IDivision logOff = new Division();
        logOff.setID("log-off");
        IHyperlink logOffLink = new Hyperlink();
        logOffLink.addContent(new Text("Log Off"));
        logOffLink.setURL("logoff.php");
        logOffLink.setTarget(LinkTarget.SELF);
        logOff.addContent(logOffLink);

        header.addContent(logOff);

        header.addContent(new LineBreak());

        body.addElement(header);

        IDivision bodyWrapper = new Division();
        bodyWrapper.setClass("body-wrapper");

        IDivision title = new Division();
        title.setClass("title");
        title.addContent(new Header("Student Grading Database Lookup", 1));
        bodyWrapper.addContent(title);

        IDivision content = new Division();
        content.setClass("content");
        content.addContent(new StudentDataNavBar());
        content.addContent(buildDataSelectForm(assignment, course, isAdmin, onyen, season, section, type, year, view));
        content.addContent(new HorizontalRule());
        content.addContent(buildAssignmentTable(assignment, course, isAdmin, onyen, season, section, type, year, view, page));
        content.addContent(buildPageNav(page, pageCount));
        bodyWrapper.addContent(content);
        body.addElement(bodyWrapper);

        return body;
    }

    private ILabel buildDropDown(ResultSet results, String id, String key, String name, String defaultVal) throws SQLException {
        return buildDropDown(results, id, key, name, defaultVal, false);
    }

    private ILabel buildDropDown(ResultSet results, String id, String key, String name, String defaultVal, boolean required) throws SQLException {
        ISelect select = new Select();
        select.setRequired(required);
        select.setName(id);

        IOption blank = new Option();
        blank.setLabel("Any");
        blank.setValue("");
        select.addOption(blank);
        while (results.next()) {
            String result = results.getString(key);
            IOption option = new Option();
            option.setText(result);
            option.setValue(result);
            if (result.equals(defaultVal)) {
                option.setSelected(true);
            }
            select.addOption(option);
        }

        ILabel assignmentLabel = new Label();
        assignmentLabel.setLabel(new Text(name));
        assignmentLabel.setElement(select);
        results.close();

        return assignmentLabel;
    }

    private ILabel buildDropDown(String[] options, String id, String name, String defaultVal) throws SQLException {
        return buildDropDown(options, id, name, defaultVal, false);
    }

    private ILabel buildDropDown(String[] options, String id, String name, String defaultVal, boolean required) throws SQLException {
        ISelect select = new Select();
        select.setRequired(required);
        select.setName(id);

        for (String s : options) {
            IOption option = new Option();
            option.setText(s);
            option.setValue(s);
            if (s.equals(defaultVal)) {
                option.setSelected(true);
            }
            select.addOption(option);
        }

        ILabel assignmentLabel = new Label();
        assignmentLabel.setLabel(new Text(name));
        assignmentLabel.setElement(select);

        return assignmentLabel;
    }
    
    private IHiddenField buildHiddenField(String name, String value) {
        IHiddenField hidden = new HiddenField();
        hidden.setName(name);
        hidden.setValue(value);
        return hidden;
    }

    private IForm buildDataSelectForm(String assignment, String course, boolean isAdmin, String onyen, String season,
            String section, String type, String year, String view) throws FileNotFoundException, IOException {
        IDatabaseReader dr = new DatabaseReader();
        IForm form = new Form();
        ResultSet results = null;
        try {
            IConfigReader config = new ConfigReader(Paths.get("config", "config.properties").toString());

            dr.connect(config.getString("database.username").orElseThrow(IllegalArgumentException::new),
                    config.getString("database.password").orElseThrow(IllegalArgumentException::new),
                    "jdbc:" + config.getString("database.url").orElseThrow(IllegalArgumentException::new));
            form.setMethod("post");
            form.setName("assignment_data");
            form.setID("assignment_data");
            form.setAction("lookup.php");

            if (isAdmin && SHOW_ADMIN_ONYEN) {
                form.addElement(buildDropDown(dr.getUsers(), "onyen", "onyen", "Onyen", onyen));
            } else if (SHOW_USER_ONYEN) {
                form.addElement(buildDropDown(new String[]{onyen}, "onyen", "Onyen", "onyen"));
            }

            form.addElement(buildDropDown(dr.getTypes(), "type", "name", "Type", type));
            form.addElement(buildDropDown(dr.getAssignments(type, course, section, year, season), "assignment", "name", "Name", assignment));
            form.addElement(buildDropDown(dr.getCourses(year, season), "course", "name", "Course", course));
            if (!course.isEmpty()) {
                form.addElement(buildDropDown(dr.getSections(course, year, season), "section", "section", "Section", section));
            }
            form.addElement(buildDropDown(dr.getTerms(), "year", "year", "Year", year));
            form.addElement(buildDropDown(dr.getTerms(), "season", "season", "Season", season));
            form.addElement(buildDropDown(new String[]{"Submissions", "Grading", "Notes", "Comments"}, "view", "View", view));

            ISubmitButton submit = new SubmitButton();
            //submit.setForm("assignment_data");
            submit.setName("assignment_data_submit");
            submit.setValue("Submit");
            form.addElement(submit);

        } catch (SQLException e) {
            LOG.log(Level.FINER, null, e);
        } finally {
            if (results != null) {
                try {
                    results.close();
                } catch (SQLException e) {
                    LOG.log(Level.FINER, null, e);
                }
            }
        }

        return form;
    }

    
    
    private IHead buildHead() {
        ITitle title = new Title("Student Grading Database Lookup");

        IMetaAttr charset = new MetaAttr();
        charset.addAttribute("charset", "UTF-8");

        ILink faviconLink = new Link();
        faviconLink.setRelation("shortcut icon");
        faviconLink.addLinkAttribtue("href", "favicon.ico");
        faviconLink.addLinkAttribtue("type", "image/vnd.microsoft.icon");
        
        ILink cssLink = new Link();
        cssLink.addLinkAttribtue("rel", "stylesheet");
        cssLink.addLinkAttribtue("type", "text/css");
        cssLink.addLinkAttribtue("href", "grader.css");

        return new Head(title, charset, faviconLink, cssLink);
    }
    
    private boolean doShowOnyen(boolean isAdmin) {
        return SHOW_USER_ONYEN || (SHOW_ADMIN_ONYEN && isAdmin);
    }

    @Override
    public String[] getValidMethods() {
        return methods;
    }
    
    private boolean checkAuth(String auth) {
        return !DO_CHECK_AUTH || AUTH_KEY.equals(auth);
    }
    
    private boolean checkAdmin(String user) {
        boolean isAdmin = false;
        IDatabaseReader dr = new DatabaseReader();
        try {
            IConfigReader config = new ConfigReader(Paths.get("config", "config.properties").toString());
            dr.connect(config.getString("database.username").orElseThrow(IllegalArgumentException::new),
                    config.getString("database.password").orElseThrow(IllegalArgumentException::new),
                    "jdbc:" + config.getString("database.url").orElseThrow(IllegalArgumentException::new));

            try (ResultSet admins = dr.getAdminForUser(user)) {
                if (admins.isBeforeFirst()) {
                    isAdmin = true;
                }
            }
        } catch (SQLException | IOException e) {
            LOG.log(Level.FINER, null, e);
        } finally {
            try {
                dr.disconnect();
            } catch (SQLException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
        LOG.log(Level.INFO, "Authenticated as user {0}, is admin? {1}", new Object[]{user, isAdmin});
        return isAdmin;
    }
}
