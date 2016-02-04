package edu.unc.cs.niograderserver.graderHandler.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Andrew Vitkus
 *
 */
public class JSONReader implements IJSONReader {

    private final JSONObject root;

    public JSONReader(File json) throws FileNotFoundException, IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(json)))) {
            StringBuilder jsonStr = new StringBuilder();
            while (br.ready()) {
                jsonStr.append(br.readLine());
            }
            root = new JSONObject(jsonStr.toString());
        }
    }

    @Override
    public String[][] getGrading() {
        List<String[]> grading = new ArrayList<>(15);
        JSONArray features = root.getJSONArray("featureResults");
        for (int i = 0; i < features.length(); i++) {
            JSONObject feature = features.getJSONObject(i);
            int score = feature.getInt("score");
            JSONObject target = feature.getJSONObject("target");
            String desc = target.getString("name");
            int possible = target.getInt("points");

            int autoGradeCount = 0;
            JSONArray results = feature.getJSONArray("results");
            for (int j = 0; j < results.length(); j++) {
                JSONObject result = results.getJSONObject(j);
                if (result.getBoolean("autoGraded")) {
                    autoGradeCount++;
                }
            }

            String autoGradeStr = Double.toString(Math.round((double) autoGradeCount / results.length() * 1000.) / 10.);

            grading.add(new String[]{desc, autoGradeStr, Integer.toString(score), Integer.toString(possible)});
        }
        JSONArray restrictions = root.getJSONArray("restrictionResults");
        for (int i = 0; i < restrictions.length(); i++) {
            JSONObject restriction = restrictions.getJSONObject(i);
            int score = restriction.getInt("score");
            JSONObject target = restriction.getJSONObject("target");
            String desc = target.getString("name");
            int possible = target.getInt("points");

            int autoGradeCount = 0;
            JSONArray results = restriction.getJSONArray("results");
            for (int j = 0; j < results.length(); j++) {
                JSONObject result = results.getJSONObject(j);
                if (result.getBoolean("autoGraded")) {
                    autoGradeCount++;
                }
            }

            String autoGradeStr = Double.toString(Math.round((double) autoGradeCount / results.length() * 1000) / 10);

            grading.add(new String[]{desc, autoGradeStr, Integer.toString(score), Integer.toString(possible)});
        }
        return grading.toArray(new String[grading.size()][4]);
    }

    @Override
    public INoteData getNotes() {
        INoteData notes = new NoteData();
        JSONArray features = root.getJSONArray("featureResults");
        for (int i = 0; i < features.length(); i++) {
            JSONObject feature = features.getJSONObject(i);
            String summary = feature.getString("summary");
            if (!summary.isEmpty()) {
                JSONObject target = feature.getJSONObject("target");
                String section = target.getString("name");
                JSONArray results = feature.getJSONArray("results");
                for (int j = 0; j < results.length(); j++) {
                    JSONObject result = results.getJSONObject(j);
                    String notesList = result.getString("notes");
                    if (!notesList.isEmpty()) {
                        notes.addSection(section);
                        notes.addPart(result.getString("name"));
                        for (String note : notesList.split(";")) {
                            notes.addNote(note);
                        }
                    }
                }
            }
        }
        JSONArray restrictions = root.getJSONArray("restrictionResults");
        for (int i = 0; i < restrictions.length(); i++) {
            JSONObject restriction = restrictions.getJSONObject(i);
            String summary = restriction.getString("summary");
            if (!summary.isEmpty()) {
                JSONObject target = restriction.getJSONObject("target");
                String section = target.getString("name");
                JSONArray results = restriction.getJSONArray("results");
                for (int j = 0; j < results.length(); j++) {
                    JSONObject result = results.getJSONObject(j);
                    String notesList = result.getString("notes");
                    if (!notesList.isEmpty()) {
                        notes.addSection(section);
                        notes.addPart(result.getString("name"));
                        for (String note : notesList.split(";")) {
                            notes.addNote(note);
                        }
                    }
                }
            }
        }
        return notes;
    }

    @Override
    public String[] getComments() {
        List<String> comments = new ArrayList<>(5);
        String commentsStr = root.getString("comments");
        for (String comment : commentsStr.split(";")) {
            if (!comment.isEmpty()) {
                comments.add(comment);
            }
        }
        return comments.toArray(new String[comments.size()]);
    }

    @Override
    public boolean[] getExtraCredit() {
        List<Boolean> ec = new ArrayList<>(5);
        JSONArray features = root.getJSONArray("featureResults");
        for (int i = 0; i < features.length(); i++) {
            JSONObject feature = features.getJSONObject(i);
            JSONObject target = feature.getJSONObject("target");
            if (target.has("extraCredit")) {
                ec.add(target.getBoolean("extraCredit"));
            } else {
                ec.add(false);
            }
        }
        JSONArray restrictions = root.getJSONArray("restrictionResults");
        for (int i = 0; i < restrictions.length(); i++) {
            ec.add(false);
        }
        boolean[] bools = new boolean[ec.size()];
        for(int i = 0; i < bools.length; i++) {
            bools[i] = ec.get(i);
        }
        return bools;
    }

    @Override
    public List<List<String>> getGradingTests() {
        List<List<String>> tests = new ArrayList<>(5);
        JSONArray features = root.getJSONArray("featureResults");
        for (int i = 0; i < features.length(); i++) {
            JSONObject feature = features.getJSONObject(i);
            JSONArray results = feature.getJSONArray("results");
            JSONObject target = feature.getJSONObject("target");
            for (int j = 0; j < results.length(); j++) {
                JSONObject result = results.getJSONObject(j);
                ArrayList<String> testParts = new ArrayList<>(5);
                testParts.add(target.getString("name"));
                testParts.add(result.getString("name"));
                testParts.add(Double.toString(result.getDouble("percentage")));
                testParts.add(Boolean.toString(result.getBoolean("autoGraded")));
                testParts.add(result.getString("notes").replaceAll("\\t", ""));
                tests.add(testParts);
            }
        }
        JSONArray restrictions = root.getJSONArray("restrictionResults");
        for (int i = 0; i < restrictions.length(); i++) {
            JSONObject restriction = restrictions.getJSONObject(i);
            JSONArray results = restriction.getJSONArray("results");
            JSONObject target = restriction.getJSONObject("target");
            for (int j = 0; j < results.length(); j++) {
                JSONObject result = results.getJSONObject(j);
                ArrayList<String> testParts = new ArrayList<>(5);
                testParts.add(target.getString("name"));
                testParts.add(result.getString("name"));
                testParts.add(Double.toString(result.getDouble("percentage")));
                testParts.add(Boolean.toString(result.getBoolean("autoGraded")));
                testParts.add(result.getString("notes").replaceAll("\\t", ""));
                tests.add(testParts);
            }
        }
        return tests;
    }
}
