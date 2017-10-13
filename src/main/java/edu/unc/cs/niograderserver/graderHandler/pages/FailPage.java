package edu.unc.cs.niograderserver.graderHandler.pages;

import util.trace.Tracer;
import edu.unc.cs.htmlBuilder.HTMLFile;
import edu.unc.cs.htmlBuilder.body.Body;
import edu.unc.cs.htmlBuilder.body.Header;
import edu.unc.cs.htmlBuilder.body.HorizontalRule;
import edu.unc.cs.htmlBuilder.body.IBody;
import edu.unc.cs.htmlBuilder.body.IHorizontalRule;
import edu.unc.cs.htmlBuilder.body.IParagraph;
import edu.unc.cs.htmlBuilder.body.Paragraph;
import edu.unc.cs.htmlBuilder.body.Text;
import edu.unc.cs.htmlBuilder.doctype.HTML5Doctype;
import edu.unc.cs.htmlBuilder.head.Head;
import edu.unc.cs.htmlBuilder.head.IHead;
import edu.unc.cs.htmlBuilder.head.IMetaAttr;
import edu.unc.cs.htmlBuilder.head.ITitle;
import edu.unc.cs.htmlBuilder.head.MetaAttr;
import edu.unc.cs.htmlBuilder.head.Title;
import edu.unc.cs.htmlBuilder.util.IColors;
import edu.unc.cs.htmlBuilder.util.IStyleManager;

/**
 *
 * @author Andrew Vitkus
 */
public class FailPage extends HTMLFile implements IFailPage {

    private String name;

    @Override
    public void setAssignmentName(String name) {
        this.name = name;
    }

    @Override
    public String getAssignmentName() {
        return name;
    }

    private void buildParts() {
        setDoctype(new HTML5Doctype());
        setHead(buildHead());
        setBody(buildBody());
    }

    private IHead buildHead() {

        ITitle title = new Title(name + " Grading Error");

        IMetaAttr charset = new MetaAttr();
        charset.addAttribute("charset", "UTF-8");

        IHead head = new Head(title, charset);
        return head;
    }

    private IBody buildBody() {
        IBody body = new Body();
        body.addStyle("width", "90%");
        body.addStyle("margin-left", "auto");
        body.addStyle("margin-right", "auto");
        body.setBGColor("#F8F8F8");

        body.addElement(new Header("Error grading " + name, 1));
        IHorizontalRule headingRule = new HorizontalRule();
        headingRule.addStyle(IStyleManager.BGCOLOR, IColors.DARK_GRAY);
        headingRule.addStyle("height", "2px");
        body.addElement(headingRule);
        IParagraph p = new Paragraph();
        Exception e = new Exception();
        Tracer.info(this, "Returning unavailable grading data");
        e.printStackTrace();
        p.addContent(new Text("Grading data unavailable, please contact your professor or a TA."));
        body.addElement(p);
        return body;
    }

    @Override
    public String getHTML() {
        buildParts();
        return super.getHTML();
    }
}
