package edu.unc.cs.niograderserver.pages.error;

import edu.unc.cs.htmlBuilder.HTMLFile;
import edu.unc.cs.htmlBuilder.IHTMLFile;
import edu.unc.cs.htmlBuilder.body.Body;
import edu.unc.cs.htmlBuilder.body.Division;
import edu.unc.cs.htmlBuilder.body.Header;
import edu.unc.cs.htmlBuilder.body.IBody;
import edu.unc.cs.htmlBuilder.body.IDivision;
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
import edu.unc.cs.niograderserver.pages.parts.StudentDataNavBar;
import java.util.Optional;
import org.apache.commons.fileupload.FileItem;


public class Error403PageGenerator implements IError403PageGenerator {
    
    private static final String[] METHODS = new String[]{"GET", "POST"};

    @Override
    public String getPage(Optional<FileItem[]> request) throws ResponseStatusNotice {
        return buildHtmlFile().getHTML();
    }
    
    private IHTMLFile buildHtmlFile() {
        IHTMLFile html = new HTMLFile();
        html.setDoctype(new HTML5Doctype());
        html.setHead(buildHead());
        html.setBody(buildBody());
        
        return html;
    }

    private IBody buildBody() {
        IBody body = new Body();

        IDivision bodyWrapper = new Division();
        bodyWrapper.setClass("body-wrapper");

        IDivision title = new Division();
        title.setClass("title");
        title.addContent(new Header("Error 403: Forbidden", 1));
        bodyWrapper.addContent(title);

        IDivision content = new Division();
        content.setClass("content");
        content.addContent(new StudentDataNavBar());
        IDivision note = new Division();
        note.setClassName("center");
        IParagraph waitNote = new Paragraph();
        waitNote.addContent(new Text("You aren't allowed here. If you should have been, please contact a TA."));
        note.addContent(waitNote);
        content.addContent(note);
        bodyWrapper.addContent(content);
        body.addElement(bodyWrapper);

        return body;
    }

    private IHead buildHead() {
        ITitle title = new Title("Error 403");

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

    @Override
    public String[] getValidMethods() {
        return METHODS;
    }
    
}
