package edu.unc.cs.niograderserver.gradingProgram;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Andrew Vitkus
 *
 */
public interface IGraderSetup {

    public Path setupFiles() throws IOException;

    public void writeConfig() throws FileNotFoundException, IOException;

    public String[] getCommandArgs();
}
