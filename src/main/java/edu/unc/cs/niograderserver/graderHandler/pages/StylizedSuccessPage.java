package edu.unc.cs.niograderserver.graderHandler.pages;

import edu.unc.cs.htmlBuilder.HTMLFile;
import edu.unc.cs.htmlBuilder.body.Body;
import edu.unc.cs.htmlBuilder.body.Division;
import edu.unc.cs.htmlBuilder.body.Header;
import edu.unc.cs.htmlBuilder.body.IBody;
import edu.unc.cs.htmlBuilder.body.IDivision;
import edu.unc.cs.htmlBuilder.body.Text;
import edu.unc.cs.htmlBuilder.doctype.HTML5Doctype;
import edu.unc.cs.htmlBuilder.head.Head;
import edu.unc.cs.htmlBuilder.head.ILink;
import edu.unc.cs.htmlBuilder.head.IMetaAttr;
import edu.unc.cs.htmlBuilder.head.Link;
import edu.unc.cs.htmlBuilder.head.MetaAttr;
import edu.unc.cs.htmlBuilder.head.Title;
import edu.unc.cs.htmlBuilder.list.IList;
import edu.unc.cs.htmlBuilder.list.IUnorderedList;
import edu.unc.cs.htmlBuilder.list.ListItem;
import edu.unc.cs.htmlBuilder.list.UnorderedList;
import edu.unc.cs.htmlBuilder.table.ITable;
import edu.unc.cs.htmlBuilder.table.ITableHeader;
import edu.unc.cs.htmlBuilder.table.ITableRow;
import edu.unc.cs.htmlBuilder.table.Table;
import edu.unc.cs.htmlBuilder.table.TableData;
import edu.unc.cs.htmlBuilder.table.TableHeader;
import edu.unc.cs.htmlBuilder.table.TableRow;
import java.util.Arrays;
import edu.unc.cs.niograderserver.graderHandler.util.INoteData;
import edu.unc.cs.niograderserver.pages.parts.StudentDataNavBar;

/**
 *
 * @author Andrew
 */
public class StylizedSuccessPage extends HTMLFile implements ISuccessPage {

    private String[] comments;
    private String[][] grading;
    private boolean[] extraCredit;
    private String name;
    private INoteData notes;
    private INoteData checkstyleNotes;

    @Override
    public String getAssignmentName() {
        return name;
    }

    @Override
    public void setAssignmentName(String name) {
        this.name = name;
    }

    @Override
    public String[] getComments() {
        return Arrays.copyOf(comments, comments.length);
    }

    @Override
    public void setComments(String[] comments) {
        this.comments = Arrays.copyOf(comments, comments.length);
    }

    @Override
    public String[][] getGrading() {
        return Arrays.copyOf(grading, grading.length);
    }

    @Override
    public void setGrading(String[][] grading) {
        this.grading = Arrays.copyOf(grading, grading.length);
    }

    @Override
    public boolean[] getExtraCredit() {
        return extraCredit;
    }

    @Override
    public void setExtraCredit(boolean[] extraCredit) {
        this.extraCredit = extraCredit;
    }

    @Override
    public String getHTML() {
        buildParts();
        return super.getHTML();
    }

    @Override
    public INoteData getNotes() {
        return notes;
    }

    @Override
    public void setNotes(INoteData notes) {
        this.notes = notes;
    }

    private void buildBody() {
        IBody body = new Body();
        IDivision bodyWrapper = new Division();
        bodyWrapper.setClass("body-wrapper");

        IDivision title = new Division();
        title.setClass("title");
        title.addContent(new Header("Grading response for " + name, 1));
        bodyWrapper.addContent(title);

        IDivision content = new Division();
        content.setClass("content");
        content.addContent(new StudentDataNavBar());
        content.addContent(new Header("Grading:", 2));
        content.addContent(buildGradeTable());

        if (notes != null && !notes.isEmpty()) {
            content.addContent(new Header("Notes:", 2));
            content.addContent(buildNoteList());
        }
        
        if (checkstyleNotes != null) {
            content.addContent(new Header("Checkstyle:", 2));
            content.addContent(buildCheckstyleNoteList());
        }

        if (comments != null && comments.length != 0) {
            content.addContent(new Header("Comments:", 2));
            content.addContent(buildCommentList());
        }
        bodyWrapper.addContent(content);
        body.addElement(bodyWrapper);
        setBody(body);
    }

    private IList buildCommentList() {
        IUnorderedList list = new UnorderedList();

        for (String comment : comments) {
            list.addListItem(new ListItem(new Text(comment)));
        }

        return list;
    }

    private ITable buildGradeTable() {
        ITable table = new Table();

        ITableRow headerRow = new TableRow();

        ITableHeader requirementsHeader = new TableHeader(new Text("Requirement"));
        //requirementsHeader.addStyle("width", "55%");

        ITableHeader autoGradeHeader = new TableHeader(new Text("%&nbsp;Autograded"));
        //autoGradeHeader.addStyle("width", "15%");

        ITableHeader pointsHeader = new TableHeader(new Text("Points"));
        //pointsHeader.addStyle("width", "15%");

        ITableHeader possibleHeader = new TableHeader(new Text("Possible"));
        //possibleHeader.addStyle("width", "15%");
        
        ITableHeader ecHeader = new TableHeader(new Text("Extra Credit"));

        headerRow.addDataPart(requirementsHeader);
        headerRow.addDataPart(autoGradeHeader);
        headerRow.addDataPart(pointsHeader);
        headerRow.addDataPart(possibleHeader);
        headerRow.addDataPart(ecHeader);
        table.addRow(headerRow);

        for (int i = 0; i < grading.length; i++) {
            String[] requirement = grading[i];
            ITableRow row = new TableRow();
            if (i % 2 == 1) {
                row.setClassName("highlight-row");
            }
            for (String part : requirement) {
                row.addDataPart(new TableData(new Text(part)));
            }
            row.addDataPart(new TableData(new Text(extraCredit[i] ? "Yes" : "No")));
            table.addRow(row);
        }

        return table;
    }

    private void buildHead() {
        Title title = new Title(name + " Grading");

        IMetaAttr charset = new MetaAttr();
        charset.addAttribute("charset", "UTF-8");

        ILink faviconLink = new Link();
        faviconLink.setRelation("shortcut icon");
        faviconLink.addLinkAttribtue("href", "favicon.ico");
        faviconLink.addLinkAttribtue("type", "image/vnd.microsoft.icon");

        setHead(new Head(title, charset, faviconLink));

        addCSSFile("grader.css");
    }

    private IList buildNoteList() {
        IUnorderedList list = new UnorderedList();

        for (String section : notes.getSections()) {
            list.addListItem(new ListItem(new Text(section)));
            IUnorderedList partList = new UnorderedList();

            for (String part : notes.getPartsForSection(section)) {
                partList.addListItem(new ListItem(new Text(part)));
                IUnorderedList noteList = new UnorderedList();

                for (String note : notes.getNotesForPart(section, part)) {
                    noteList.addListItem(new ListItem(new Text(note)));

                }
                partList.addListItem(noteList);
            }
            list.addListItem(partList);
        }

        return list;
    }
    
    private IList buildCheckstyleNoteList() {
        IUnorderedList list = new UnorderedList();

        for (String section : checkstyleNotes.getSections()) {
            list.addListItem(new ListItem(new Text(section)));
            IUnorderedList partList = new UnorderedList();

            for (String part : checkstyleNotes.getPartsForSection(section)) {
                partList.addListItem(new ListItem(new Text(part)));
                IUnorderedList noteList = new UnorderedList();

                for (String note : checkstyleNotes.getNotesForPart(section, part)) {
                    noteList.addListItem(new ListItem(new Text(note)));

                }
                partList.addListItem(noteList);
            }
            list.addListItem(partList);
        }

        return list;
    }

    private void buildParts() {
        setDoctype(new HTML5Doctype());
        buildHead();
        buildBody();
    }

    @Override
    public void setCheckstyleNotes(INoteData checkstyleNotes) {
        this.checkstyleNotes = checkstyleNotes;
    }

    @Override
    public INoteData getCheckstyleNotes() {
        return checkstyleNotes;
    }
}
