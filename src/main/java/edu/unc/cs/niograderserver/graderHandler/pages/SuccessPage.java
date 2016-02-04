package edu.unc.cs.niograderserver.graderHandler.pages;

import java.util.Arrays;
import edu.unc.cs.niograderserver.graderHandler.util.INoteData;
import edu.unc.cs.htmlBuilder.HTMLFile;
import edu.unc.cs.htmlBuilder.body.Body;
import edu.unc.cs.htmlBuilder.body.Header;
import edu.unc.cs.htmlBuilder.body.HorizontalRule;
import edu.unc.cs.htmlBuilder.body.IBody;
import edu.unc.cs.htmlBuilder.body.IHorizontalRule;
import edu.unc.cs.htmlBuilder.body.Text;
import edu.unc.cs.htmlBuilder.doctype.HTML5Doctype;
import edu.unc.cs.htmlBuilder.head.Head;
import edu.unc.cs.htmlBuilder.head.IHead;
import edu.unc.cs.htmlBuilder.head.IMetaAttr;
import edu.unc.cs.htmlBuilder.head.ITitle;
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
import edu.unc.cs.htmlBuilder.util.IBorderStyles;
import edu.unc.cs.htmlBuilder.util.IColors;
import edu.unc.cs.htmlBuilder.util.IStyleManager;

/**
 *
 * @author Andrew
 */
public class SuccessPage extends HTMLFile implements ISuccessPage {

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

    private IBody buildBody() {
        IBody body = new Body();
        body.addStyle("width", "90%");
        body.addStyle("margin-left", "auto");
        body.addStyle("margin-right", "auto");
        body.setBGColor("#F8F8F8");

        body.addElement(new Header("Grading response for " + name, 1));
        IHorizontalRule headingRule = new HorizontalRule();
        headingRule.addStyle(IStyleManager.BGCOLOR, IColors.DARK_GRAY);
        headingRule.addStyle("height", "2px");
        body.addElement(headingRule);

        body.addElement(new Header("Grading:", 2));
        body.addElement(buildGradeTable());

        if (notes != null && !notes.isEmpty()) {
            body.addElement(new Header("Notes:", 2));
            body.addElement(buildNoteList());
        }
        
        if (checkstyleNotes != null) {
            body.addElement(new Header("Checkstyle:", 2));
            body.addElement(buildCheckstyleNoteList());
        }

        if (comments != null && comments.length == 0) {
            body.addElement(new Header("Comments:", 2));
            body.addElement(buildCommentList());
        }
        return body;
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

        table.setBorder(2, IBorderStyles.SOLID);
        table.addStyle("width", "90%");

        ITableRow headerRow = new TableRow();
        headerRow.setBGColor("#B0B0B0");

        ITableHeader requirementsHeader = new TableHeader(new Text("Requirement"));
        requirementsHeader.addStyle("width", "55%");

        ITableHeader autoGradeHeader = new TableHeader(new Text("%&nbsp;Autograded"));
        autoGradeHeader.addStyle("width", "13%");

        ITableHeader pointsHeader = new TableHeader(new Text("Points"));
        pointsHeader.addStyle("width", "13%");

        ITableHeader possibleHeader = new TableHeader(new Text("Possible"));
        possibleHeader.addStyle("width", "13%");

        ITableHeader ecHeader = new TableHeader(new Text("Extra Credit"));
        possibleHeader.addStyle("width", "6%");

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
                row.setBGColor(IColors.LIGHT_GRAY);
            }
            for (String part : requirement) {
                row.addDataPart(new TableData(new Text(part)));
            }
            row.addDataPart(new TableData(new Text(extraCredit[i] ? "Yes" : "No")));
            table.addRow(row);
        }

        return table;
    }

    private IHead buildHead() {

        ITitle title = new Title(name + " Grading");

        IMetaAttr charset = new MetaAttr();
        charset.addAttribute("charset", "UTF-8");

        IHead head = new Head(title, charset);
        return head;
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
        setHead(buildHead());
        setBody(buildBody());
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
