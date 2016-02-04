package edu.unc.cs.niograderserver.graderHandler.util;

import java.util.List;

/**
 * @author Andrew Vitkus
 *
 */
public interface IJSONReader {

    public String[][] getGrading();

    public String[] getComments();

    public INoteData getNotes();

    public boolean[] getExtraCredit();

    public List<List<String>> getGradingTests();
}
