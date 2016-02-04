package edu.unc.cs.niograderserver.graderHandler.pages;

import edu.unc.cs.niograderserver.graderHandler.util.INoteData;

/**
 *
 * @author Andrew
 */
public interface ISuccessPage extends IGraderResponsePage {

    public void setGrading(String[][] grading);

    public String[][] getGrading();
    
    public void setExtraCredit(boolean[] extraCredit);
    
    public boolean[] getExtraCredit();

    public void setComments(String[] comments);

    public String[] getComments();

    public void setNotes(INoteData notes);

    public INoteData getNotes();
    
    public void setCheckstyleNotes(INoteData checkstyleNotes);
    
    public INoteData getCheckstyleNotes();
}
