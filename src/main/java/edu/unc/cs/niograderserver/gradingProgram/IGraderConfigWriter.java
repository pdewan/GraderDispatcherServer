package edu.unc.cs.niograderserver.gradingProgram;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author Andrew Vitkus
 *
 */
public interface IGraderConfigWriter {

    public static final int FEEDBACK_TXT = 1;
    public static final int FEEDBACK_JSON = 2;
    public static final int LOCAL_TXT = 4;
    public static final int LOCAL_JSON = 8;
    public static final int SPREADSHEET = 16;

    public static final String GUI_GRADING_MANAGER = "AGUIGradingManager";
    public static final String HEADLESS_GRADING_MANAGER = "AHeadlessGradingManager";
    public static final String SAKAI_PROJECT_DATABASE = "SakaiProjectDatabase";

    public void setProjectRequirements(String requirements);

    public void setCourseName(String name);

    public String getCourseName();

    public void setAssignmentName(String name);

    public void setController(String name);

    public void setPath(String path);

    public void setStartOnyen(String start);

    public void setEndOnyen(String end);

    public void setLogging(int... logging);

    public void setSpreadsheet(String path);

    public void setFrameworkGUI(boolean frameworkGUI);

    public String getProjectRequirements();

    public String getAssignmentName();

    public String getController();

    public String getPath();

    public String getStartOnyen();

    public String getEndOnyen();

    public int getLogging();

    public String getSpreadsheet();

    public boolean getFrameworkGUI();

    public String getConfigText();

    public void write(File file) throws FileNotFoundException, IOException;

    public String[] getCommandArgs();
}
