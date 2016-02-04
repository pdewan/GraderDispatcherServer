package edu.unc.cs.niograderserver.graderHandler.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author Andrew Vitkus
 */
public interface IGradeWriter {

    public void write(IGradingData grading) throws FileNotFoundException, IOException, InterruptedException, ExecutionException;
}
