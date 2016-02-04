package edu.unc.cs.niograderserver.pages;

import edu.unc.cs.niograderserver.utils.ConfigReader;
import edu.unc.cs.niograderserver.utils.IConfigReader;
import edu.unc.cs.niograderserver.pages.helpers.AverageTableBuilder;
import edu.unc.cs.niograderserver.pages.helpers.ITableBuilder;
import edu.unc.cs.niograderserver.pages.parts.StudentDataNavBar;
import edu.unc.cs.niograderserver.pages.sql.DatabaseReader;
import edu.unc.cs.niograderserver.pages.sql.IDatabaseReader;
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
import edu.unc.cs.htmlBuilder.body.IParagraph;
import edu.unc.cs.htmlBuilder.body.ISpan;
import edu.unc.cs.htmlBuilder.body.LineBreak;
import edu.unc.cs.htmlBuilder.body.Paragraph;
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
import edu.unc.cs.htmlBuilder.table.Table;
import edu.unc.cs.httpserver.util.ResponseStatusNotice;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.httpclient.HttpStatus;

public class StudentDataStatisticsPageGenerator extends HTMLFile implements IStudentDataStatisticsPageGenerator {

    private static final Logger LOG = Logger.getLogger(StudentDataStatisticsPageGenerator.class.getName());

    private final String[] METHODS = new String[]{"GET", "POST"};
    
    private static final String AUTH_KEY;
    private static final boolean SHOW_ADMIN_ONYEN;
    private static final boolean DO_CHECK_AUTH;

    static {
        boolean adminOnyen = false;StringBuilder auth = new StringBuilder(50);
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
            IConfigReader properties = new ConfigReader("config/auth.properties");
            properties = new ConfigReader("config/config.properties");
            adminOnyen = properties.getBoolean("database.view.showAdminOnyen", false).get();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        AUTH_KEY = auth.toString();
        SHOW_ADMIN_ONYEN = adminOnyen;
        DO_CHECK_AUTH = checkAuthTmp;
    }
    
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
        
        FileItem[] args = request.orElseThrow(() -> new ResponseStatusNotice(HttpStatus.SC_BAD_REQUEST));
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
            }
        }
        if (!checkAuth(auth)) {
            throw new ResponseStatusNotice(HttpStatus.SC_FORBIDDEN);
        }
        if (user == null || user.isEmpty()) {
            throw new ResponseStatusNotice(HttpStatus.SC_BAD_REQUEST);
        }
        
        IHTMLFile html = buildHtmlFile(assignment, course, isAdmin, onyen, season, section, type, user, year);
        return html.getHTML();
    }

    private IHTMLFile buildHtmlFile(String assignment, String course, boolean isAdmin, String onyen,
            String season, String section, String type, String user, String year) {
        IHTMLFile html = new HTMLFile();
        
        try {
            html.setDoctype(new HTML5Doctype());
            html.setHead(buildHead());
            html.setBody(buildBody(assignment, course, isAdmin, onyen, season, section, type, user, year));
        } catch (FileNotFoundException e) {
            LOG.log(Level.FINER, null, e);
        } catch (IOException e) {
            LOG.log(Level.FINER, null, e);
        }
        return html;
    }

    private ITable buildAverageTable(String assignment, String course, boolean isAdmin, String onyen,
            String season, String section, String type, String year) throws FileNotFoundException, IOException {
        IDatabaseReader dr = new DatabaseReader();
        ITable table = new Table();
        table.setClassName("center");
        ResultSet results = null;
        try {
            IConfigReader config = new ConfigReader(Paths.get("config", "config.properties").toString());
            dr.connect(config.getString("database.username").orElseThrow(IllegalArgumentException::new),
                    config.getString("database.password").orElseThrow(IllegalArgumentException::new),
                    "jdbc:" + config.getString("database.url").orElseThrow(IllegalArgumentException::new));

            if (!assignment.isEmpty() && !course.isEmpty()) {
                results = dr.getResultsForAll(doShowOnyen(isAdmin) ? onyen : "", assignment, type, course, section, year, season);
            }

            ITableBuilder tb = new AverageTableBuilder(results, dr);
            table = tb.getTable();
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

    private IBody buildBody(String assignment, String course, boolean isAdmin, String onyen, String season,
            String section, String type, String user, String year) throws FileNotFoundException, IOException {
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
        title.addContent(new Header("Student Grading Database Statistics", 1));
        bodyWrapper.addContent(title);

        IDivision content = new Division();
        content.setClass("content");
        content.addContent(new StudentDataNavBar());
        content.addContent(buildForm(assignment, course, isAdmin, onyen, season, section, type, year));
        content.addContent(new HorizontalRule());
        if (!assignment.isEmpty() && !course.isEmpty()) {
            content.addContent(buildAverageTable(assignment, course, isAdmin, onyen, season, section, type, year));
        } else {
            IParagraph p = new Paragraph();
            p.addContent(new Text("Please select an assignment name and course to view statistics."));
            content.addContent(p);
        }
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

    private IForm buildForm(String assignment, String course, boolean isAdmin, String onyen, String season,
            String section, String type, String year) throws FileNotFoundException, IOException {
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
            form.setAction("statistics.php");

            if (doShowOnyen(isAdmin)) {
                form.addElement(buildDropDown(dr.getUsers(), "onyen", "onyen", "Onyen", onyen));
            }

            form.addElement(buildDropDown(dr.getTypes(), "type", "name", "Type", type));
            form.addElement(buildDropDown(dr.getAssignments(type, course, section, year, season), "assignment", "name", "Name", assignment, true));
            form.addElement(buildDropDown(dr.getCourses(year, season), "course", "name", "Course", course, true));
            if (!course.isEmpty()) {
                form.addElement(buildDropDown(dr.getSections(course, year, season), "section", "section", "Section", section));
            }
            form.addElement(buildDropDown(dr.getTerms(), "year", "year", "Year", year));
            form.addElement(buildDropDown(dr.getTerms(), "season", "season", "Season", season));

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
        return SHOW_ADMIN_ONYEN && isAdmin;
    }

    @Override
    public String[] getValidMethods() {
        return METHODS;
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
