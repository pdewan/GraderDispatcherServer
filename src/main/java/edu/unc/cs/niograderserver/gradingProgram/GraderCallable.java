/**
 *
 */
package edu.unc.cs.niograderserver.gradingProgram;

import edu.unc.cs.niograderserver.utils.ConfigReader;
import edu.unc.cs.niograderserver.utils.IConfigReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * @author Andrew
 *
 */
public class GraderCallable implements Callable<String> {

    private final String[] args;
    
    public GraderCallable(String[] args) {
        this.args = Arrays.copyOf(args, args.length);
    }

    @Override
    public String call() throws Exception {
        ProcessBuilder lsPb;
        if (System.getProperty("os.name").contains("Windows")) {
            lsPb = new ProcessBuilder("dir", "target");
        } else {
            lsPb = new ProcessBuilder("ls", "target");
        }
        lsPb.redirectErrorStream(true);
        lsPb.directory(new File("graderProgram"));
        Process lspr = lsPb.start();
        lspr.waitFor();
        StringBuilder sb = new StringBuilder(100);
        try (InputStream is = lspr.getInputStream()) {
            while (is.available() != 0) {
                sb.append((char) is.read());
            }
        }

        String sbstr = sb.toString();

        int start = sbstr.indexOf("comp401-grader-");
        int end = sbstr.indexOf(".jar", start);

        String jar = Paths.get(".", "target", sbstr.substring(start, end + 4)).toString();

        
        IConfigReader config = new ConfigReader("./config/config.properties");
        Optional<String> loc = config.getString("grader.java.loc");
        ArrayList<String> command = new ArrayList<>(5);
        if (loc.isPresent()) {
            command.add(loc.get());
        } else {
            command.add("java");
        }
        
        command.add("-cp");
        StringBuilder runClasspath = new StringBuilder();
        String separator = System.getProperty("path.separator");
        runClasspath.append(".").append(separator).append(jar);
        Optional<String> classpath = config.getString("grader.java.classpath");
        if (classpath.isPresent()) {
            runClasspath.append(separator).append(classpath.get());
            /*String systemClasspath = System.getProperty("java.class.path");
            if (systemClasspath == null || systemClasspath.isEmpty()) {
                command.add("\"." + separator + classpath.get() + "\"");
            } else {
                command.add("\"." + separator + systemClasspath + separator + classpath.get() + "\"");
            }*/
        }
        command.add(runClasspath.toString());
        //command.add("-jar");
        //command.add(jar);
        command.add("gradingTools.Driver");
        command.addAll(Arrays.asList(args));
        
        System.out.print("\nCMD: ");
        for(String s : command) {
            System.out.print(s + " ");
        }
        System.out.println();

        ProcessBuilder jarPb = new ProcessBuilder(command);
        jarPb.inheritIO();
        //jarPb.redirectErrorStream(true);
        jarPb.directory(new File("graderProgram"));

        Process pr = jarPb.start();
        pr.waitFor();
        StringBuilder output = new StringBuilder(500);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(pr.getInputStream()))) {
            while (br.ready()) {
                output.append(br.readLine()).append("\n");
            }
        }
        return output.toString();
    }
}
