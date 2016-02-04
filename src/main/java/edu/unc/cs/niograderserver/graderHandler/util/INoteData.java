package edu.unc.cs.niograderserver.graderHandler.util;

/**
 * @author Andrew Vitkus
 *
 */
public interface INoteData {

    public void addSection(String section);

    public void addPart(String part);

    public void addNote(String note);

    public String[] getSections();

    public String[] getPartsForSection(String section);

    public String[] getNotesForPart(String section, String part);

    public boolean isEmpty();
}
