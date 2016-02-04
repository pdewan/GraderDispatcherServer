package edu.unc.cs.niograderserver.graderHandler.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andrew Vitkus
 *
 */
public class NoteData implements INoteData {

    private final Map<String, Map<String, List<String>>> noteData;

    private String currentSection;
    private String currentPart;

    public NoteData() {
        noteData = new LinkedHashMap<>(5);
    }

    @Override
    public void addSection(String section) {
        if (!noteData.containsKey(section)) {
            noteData.put(section, new LinkedHashMap<>(5));
        }
        currentSection = section;
    }

    @Override
    public void addPart(String part) {
        if (currentSection == null) {
            addPart("");
        }
        Map<String, List<String>> partMap = noteData.get(currentSection);
        if (!partMap.containsKey(part)) {
            partMap.put(part, new ArrayList<>(5));
        }
        currentPart = part;
    }

    @Override
    public void addNote(String note) {
        if (currentPart == null) {
            addPart("");
        }
        noteData.get(currentSection).get(currentPart).add(note);
    }

    @Override
    public String[] getSections() {
        return noteData.keySet().toArray(new String[noteData.size()]);
    }

    @Override
    public String[] getPartsForSection(String section) {
        Map<String, List<String>> parts = noteData.get(section);
        return parts.keySet().toArray(new String[parts.size()]);
    }

    @Override
    public String[] getNotesForPart(String section, String part) {
        List<String> notes = noteData.get(section).get(part);
        return notes.toArray(new String[notes.size()]);
    }

    @Override
    public boolean isEmpty() {
        return noteData.isEmpty();
    }
}
