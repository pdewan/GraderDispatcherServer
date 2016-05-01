package edu.unc.cs.niograderserver.graderHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import edu.unc.cs.niograderserver.graderHandler.pages.FailPage;
import edu.unc.cs.niograderserver.graderHandler.pages.ISuccessPage;
import edu.unc.cs.niograderserver.graderHandler.pages.StylizedSuccessPage;
import edu.unc.cs.niograderserver.graderHandler.pages.SuccessPage;
import edu.unc.cs.niograderserver.graderHandler.util.CheckstyleNotesUtil;
import edu.unc.cs.niograderserver.graderHandler.util.IJSONReader;
import edu.unc.cs.niograderserver.graderHandler.util.INoteData;
import edu.unc.cs.niograderserver.graderHandler.util.JSONReader;
import java.util.Arrays;

/**
 * @author Andrew Vitkus
 *
 */
public class JSONBasedResponseWriter extends ResponseWriter {

    public JSONBasedResponseWriter(String jsonFileLoc, String checkstyleFileLoc, boolean stylized) throws FileNotFoundException, IOException {
        this(new File(jsonFileLoc), new File(checkstyleFileLoc));
    }

    public JSONBasedResponseWriter(String jsonFileLoc, String checkstyleFileLoc) throws FileNotFoundException, IOException {
        this(new File(jsonFileLoc), new File(checkstyleFileLoc), false);
    }

    public JSONBasedResponseWriter(File json, File checkstyle) throws FileNotFoundException, IOException {
        this(json, checkstyle, false);
    }

    public JSONBasedResponseWriter(File json, File checkstyle, boolean stylized) throws FileNotFoundException, IOException {
        System.out.println("Checking JSON file:" + json.getAbsolutePath());
    	if (json.exists()) {
            IJSONReader reader = new JSONReader(json);
            response = stylized ? new StylizedSuccessPage() : new SuccessPage();
            String[][] grading = reader.getGrading();
            INoteData notes = reader.getNotes();
            String[] comments = reader.getComments();
            ((ISuccessPage) response).setGrading(grading);
            ((ISuccessPage) response).setExtraCredit(reader.getExtraCredit());
            ((ISuccessPage) response).setNotes(notes);
            ((ISuccessPage) response).setCheckstyleNotes(CheckstyleNotesUtil.readCheckstyleNotes(checkstyle.toPath()));
            ((ISuccessPage) response).setComments(comments);
            if (grading != null && notes != null && comments != null) {
                return;
            }
        }
        System.out.println("JSON file does not exist:" + json.getName());

        response = new FailPage();
    }
}
