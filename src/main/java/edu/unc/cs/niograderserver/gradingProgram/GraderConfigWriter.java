package edu.unc.cs.niograderserver.gradingProgram;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Logger;

public class GraderConfigWriter implements IGraderConfigWriter {

    private static final String FILE_SEPARATOR = System.getProperty("file.separator");
    private static final Logger LOG = Logger.getLogger(GraderConfigWriter.class.getName());

    private String assignmentName;
    private String controller;
    private String courseName;
    private String endOnyen;
    private boolean frameworkGUI;
    private int logging;
    private String path;
    private String projectRequirements;
    private String spreadsheetPath;
    private String startOnyen;

    public GraderConfigWriter() {
        projectRequirements = "";
        assignmentName = "";
        controller = "";
        path = "";
        startOnyen = "";
        endOnyen = "";
        logging = 0;
        spreadsheetPath = "";
        frameworkGUI = false;
    }

    @Override
    public String getAssignmentName() {
        return assignmentName;
    }

    @Override
    public void setAssignmentName(String name) {
        assignmentName = name;
    }

    @Override
    public String[] getCommandArgs() {
        ArrayList<String> args = new ArrayList<>(15);
        args.add("--project-requirements");
        args.add(projectRequirements);
        //LOG.log(Level.INFO, "project.requirements = {0}", projectRequirements);
        args.add("--project-name ");
        args.add(assignmentName);
        //LOG.log(Level.INFO, "project.name = {0}", assignmentName);
        args.add("--grader-controller");
        args.add(controller);
        //LOG.log(Level.INFO, "grader.controller = {0}", controller);
        if (!path.isEmpty()) {
            args.add("--headless-path");
            args.add(path);
            //LOG.log(Level.INFO, "grader.headless.path = {0}", path);
        }
        if (!startOnyen.isEmpty()) {
            args.add("--headless-start");
            args.add(startOnyen);
            //LOG.log(Level.INFO, "\"grader.headless.start = {0}", startOnyen);
        }
        if (!endOnyen.isEmpty()) {
            args.add("--headless-end");
            args.add(endOnyen);
            //LOG.log(Level.INFO, "grader.headless.end = {0}", endOnyen);
        }
        if (!courseName.isEmpty()) {
            args.add("--course-name");
            args.add(courseName);
            //LOG.log(Level.INFO, "currentModule = {0}", courseName);
        }     
        if (logging != 0) {
            args.add("--logger");
            args.add(getLoggingStr());
            //LOG.log(Level.INFO, "grader.logger = {0}", getLoggingStr());
        }
        if (frameworkGUI) {
            args.add("--use-framework-gui");
            //LOG.log(Level.INFO, "grader.controller.useFrameworkGUI = true");
        } else {
            args.add("--no-framework-gui");
            //LOG.log(Level.INFO, "grader.controller.useFrameworkGUI = false");
        }
        args.add("--clean-slate");
        args.add(startOnyen);

        return args.toArray(new String[args.size()]);
    }

    @Override
    public String getConfigText() {
        StringBuilder config = new StringBuilder(200);

        config.append("project.requirements = ").append(projectRequirements).append("\n");
        //LOG.log(Level.INFO, "project.requirements = {0}", projectRequirements);
        config.append("project.name = ").append(assignmentName).append("\n");
        //LOG.log(Level.INFO, "project.name = {0}", assignmentName);
        config.append("grader.controller = ").append(controller).append("\n");
        //LOG.log(Level.INFO, "grader.controller = {0}", controller);
        if (!path.isEmpty()) {
            config.append("grader.headless.path = ").append(path).append("\n");
            //LOG.log(Level.INFO, "grader.headless.path = {0}", path);
        }
        if (!startOnyen.isEmpty()) {
            config.append("grader.headless.start = ").append(startOnyen).append("\n");
            //LOG.log(Level.INFO, "\"grader.headless.start = {0}", startOnyen);
        }
        if (!endOnyen.isEmpty()) {
            config.append("grader.headless.end = ").append(endOnyen).append("\n");
            //LOG.log(Level.INFO, "grader.headless.end = {0}", endOnyen);
        }
        if (logging != 0) {
            config.append("grader.logger = ").append(getLoggingStr()).append("\n");
            //LOG.log(Level.INFO, "grader.logger = {0}", getLoggingStr());
        }
        if (!spreadsheetPath.isEmpty()) {
            config.append("grader.logger.spreadsheetFilename = ").append(spreadsheetPath).append("\n");
            //LOG.log(Level.INFO, "grader.logger.spreadsheetFilename = {0}", spreadsheetPath);
        }
        config.append("grader.controller.useFrameworkGUI = ").append(frameworkGUI);
        //LOG.log(Level.INFO, "grader.controller.useFrameworkGUI = {0}", frameworkGUI);

        return config.toString();
    }

    @Override
    public String getController() {
        return controller;
    }

    @Override
    public void setController(String name) {
        controller = name;
    }

    @Override
    public String getCourseName() {
        return courseName;
    }

    @Override
    public void setCourseName(String name) {
        courseName = name;
    }

    @Override
    public String getEndOnyen() {
        return endOnyen;
    }

    @Override
    public void setEndOnyen(String end) {
        endOnyen = end;
    }

    @Override
    public boolean getFrameworkGUI() {
        return frameworkGUI;
    }

    @Override
    public void setFrameworkGUI(boolean frameworkGUI) {
        this.frameworkGUI = frameworkGUI;
    }

    @Override
    public int getLogging() {
        return logging;
    }

    @Override
    public void setLogging(int... logging) {
        this.logging = 0;
        for (int type : logging) {
            this.logging += type;
        }
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String getProjectRequirements() {
        return projectRequirements;
    }

    @Override
    public void setProjectRequirements(String requirements) {
        projectRequirements = requirements;
    }

    @Override
    public String getSpreadsheet() {
        return spreadsheetPath;
    }

    @Override
    public void setSpreadsheet(String path) {
        spreadsheetPath = path;
    }

    @Override
    public String getStartOnyen() {
        return startOnyen;
    }

    @Override
    public void setStartOnyen(String start) {
        startOnyen = start;
    }

    @Override
    public void write(File file) throws FileNotFoundException, IOException {
        if (file.exists()) {
            file.delete();
        }
        int folderEnd = file.toPath().toString().lastIndexOf(FILE_SEPARATOR);
        Files.createDirectories(Paths.get(file.toPath().toString().substring(0, folderEnd)));
        Files.createFile(file.toPath());
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
            bos.write(getConfigText().getBytes());
            bos.flush();
        }
    }

    private String getLoggingStr() {
        StringBuilder str = new StringBuilder(75);

        if ((logging | IGraderConfigWriter.FEEDBACK_TXT) == logging) {
            str.append("feedback-txt");
        }
        if ((logging | IGraderConfigWriter.FEEDBACK_JSON) == logging) {
            if (str.length() != 0) {
                str.append("+");
            }
            str.append("feedback-json");
        }
        if ((logging | IGraderConfigWriter.LOCAL_TXT) == logging) {
            if (str.length() != 0) {
                str.append("+");
            }
            str.append("local-txt");
        }
        if ((logging | IGraderConfigWriter.LOCAL_JSON) == logging) {
            if (str.length() != 0) {
                str.append("+");
            }
            str.append("local-json");
        }
        if ((logging | IGraderConfigWriter.SPREADSHEET) == logging) {
            if (str.length() != 0) {
                str.append("+");
            }
            str.append("spreadsheet");
        }

        return str.toString();
    }
}
