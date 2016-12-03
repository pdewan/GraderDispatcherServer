package edu.unc.cs.niograderserver.pages;

import edu.unc.cs.niograderserver.gradingProgram.GraderPool;
import edu.unc.cs.htmlBuilder.HTMLFile;
import edu.unc.cs.htmlBuilder.IHTMLFile;
import edu.unc.cs.htmlBuilder.attributes.LinkTarget;
import edu.unc.cs.htmlBuilder.body.Body;
import edu.unc.cs.htmlBuilder.body.Division;
import edu.unc.cs.htmlBuilder.body.Header;
import edu.unc.cs.htmlBuilder.body.Hyperlink;
import edu.unc.cs.htmlBuilder.body.IBody;
import edu.unc.cs.htmlBuilder.body.IDivision;
import edu.unc.cs.htmlBuilder.body.IHyperlink;
import edu.unc.cs.htmlBuilder.body.IParagraph;
import edu.unc.cs.htmlBuilder.body.Paragraph;
import edu.unc.cs.htmlBuilder.body.Text;
import edu.unc.cs.htmlBuilder.doctype.HTML5Doctype;
import edu.unc.cs.htmlBuilder.head.Head;
import edu.unc.cs.htmlBuilder.head.IHead;
import edu.unc.cs.htmlBuilder.head.ILink;
import edu.unc.cs.htmlBuilder.head.IMetaAttr;
import edu.unc.cs.htmlBuilder.head.ITitle;
import edu.unc.cs.htmlBuilder.head.Link;
import edu.unc.cs.htmlBuilder.head.MetaAttr;
import edu.unc.cs.htmlBuilder.head.Title;
import edu.unc.cs.httpserver.util.ResponseStatusNotice;
import edu.unc.cs.niograderserver.pages.helpers.GradePageManager;
import java.util.logging.Logger;
import edu.unc.cs.niograderserver.pages.parts.StudentDataNavBar;
import edu.unc.cs.niograderserver.utils.ConfigReader;
import edu.unc.cs.niograderserver.utils.IConfigReader;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import org.apache.commons.fileupload.FileItem;
import org.apache.http.HttpStatus;

public class GradingInProgressPageGenerator implements IGradingInProgressPageGenerator {

    private static final Logger LOG = Logger.getLogger(GradingInProgressPageGenerator.class.getName());
    private static final String SERVER_ROOT_ADDRESS;
    private static final String PAGE_ADDRESS = "grading.php";
    protected static  final boolean CHECK_PENDING = false;

    private final String[] methods = new String[]{"GET", "POST"};

    static {
        String tmpRoot = null;
        try {
            IConfigReader config = new ConfigReader("config/config.properties");
            tmpRoot = config.getString("server.external.root").get();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }

        SERVER_ROOT_ADDRESS = tmpRoot;
    }

    @Override
    public String getPage(Optional<FileItem[]> request) throws ResponseStatusNotice {
        String uuid = null;
        int number = -1;

        FileItem[] args = request.orElseThrow(() -> new ResponseStatusNotice(HttpStatus.SC_BAD_REQUEST));
        for (FileItem arg : args) {
            switch (arg.getFieldName()) {
                case "id":
                    uuid = arg.getString();
                    number = GradePageManager.getNumber(uuid).orElseThrow(() -> new ResponseStatusNotice(HttpStatus.SC_NOT_FOUND));
                    break;
            }
        }

        if (uuid == null) {
            throw new ResponseStatusNotice(HttpStatus.SC_BAD_REQUEST);
        }

        IHTMLFile html = buildHtmlFile(uuid, number);
        return html.getHTML();
    }
    
    protected boolean checkPending() {
    	return false;
    }

    private IHTMLFile buildHtmlFile(String uuid, int number) {
        IHTMLFile html = new HTMLFile();
        html.setDoctype(new HTML5Doctype());
        html.setHead(buildHead(uuid, number));
        html.setBody(buildBody(uuid, number));

        return html;
    }

    private IBody buildBody(String uuid, int number) {
        IBody body = new Body();

        IDivision bodyWrapper = new Division();
        bodyWrapper.setClass("body-wrapper");

        IDivision title = new Division();
        title.setClass("title");
        title.addContent(new Header("Grading In Progress", 1));
        bodyWrapper.addContent(title);

        IDivision content = new Division();
        content.setClass("content");
        content.addContent(new StudentDataNavBar());
        IDivision note = new Division();
        note.setClassName("center");
        IParagraph waitNote = new Paragraph();
        if (number >= 0) {
            int pending = GraderPool.getPendingForRun(number);

            if (pending > 0) {
                waitNote.addContent(new Text("There are " + pending + " submissions before you in the grading queue."));
            } else if (pending == 0) {
                waitNote.addContent(new Text("You are now being graded."));
            }
        } else {
            waitNote.addContent(new Text("Grading queue position is not available. This will not effect your submission."));
        }
        note.addContent(waitNote);
        IParagraph refreshNote = new Paragraph();
        refreshNote.addContent(new Text("This page will reload every 5 seconds to check for grading completion. If it does not, click below."));
        note.addContent(refreshNote);
        IHyperlink link = new Hyperlink();
        link.setTarget(LinkTarget.SELF);
        if (number >= 0) {
            link.setURL(SERVER_ROOT_ADDRESS + PAGE_ADDRESS + "?id=" + uuid + "&amp;num=" + number);
        } else {
            link.setURL(SERVER_ROOT_ADDRESS + PAGE_ADDRESS + "?id=" + uuid);
        }
        link.addContent(new Text("Refresh"));
        note.addContent(link);
        content.addContent(note);
        bodyWrapper.addContent(content);
        body.addElement(bodyWrapper);

        return body;
    }

    private IHead buildHead(String uuid, int number) {
        ITitle title = new Title("Grading in Process");

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

        IMetaAttr refresh = new MetaAttr();
        refresh.addMetaAttribute("http-equiv", "refresh");
        refresh.addMetaAttribute("content", "5; url=" + SERVER_ROOT_ADDRESS + PAGE_ADDRESS + "?id=" + uuid + "&amp;num=" + number);

        return new Head(title, charset, faviconLink, cssLink, refresh);
    }

    @Override
    public String[] getValidMethods() {
        return methods;
    }
}
