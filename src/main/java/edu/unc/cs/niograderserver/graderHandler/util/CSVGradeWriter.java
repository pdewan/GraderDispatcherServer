package edu.unc.cs.niograderserver.graderHandler.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class CSVGradeWriter extends GradeWriter implements ICSVGradeWriter {

    public CSVGradeWriter(String assignmentName, Path gradeFile) throws IOException, FileNotFoundException, InterruptedException, ExecutionException {
        super(assignmentName, gradeFile);
    }

    public CSVGradeWriter(String assignmentName, String gradeFile) throws IOException, FileNotFoundException, InterruptedException, ExecutionException {
        super(assignmentName, gradeFile);
    }

    @Override
    protected String formatData(IGradingData data) {
        StringBuilder line = new StringBuilder(100);
        line.append(data.getOnyen()).append(",");
        line.append(data.getOnyen()).append(",");
        line.append(data.getLastName()).append(",");
        line.append(data.getFirstName()).append(",");
        line.append(data.getScore() / data.getPossible()).append("\r\n");

        return line.toString();
    }

    @Override
    protected void initializeFile() throws FileNotFoundException, IOException, InterruptedException, ExecutionException {
        Future<FileLock> flock = gradeFile.lock();
        String header = assignmentName + ",Scores,,,\r\nDisplay ID,ID,Last Name,First Name,grade\r\n,,,,\r\n";
        FileLock lock = flock.get();
        byte[] bytes = header.getBytes("UTF-16");
        ByteBuffer expectedHeader = ByteBuffer.wrap(bytes);
        ByteBuffer fileData = ByteBuffer.allocate(expectedHeader.limit());
        ((AsynchronousFileChannel) lock.acquiredBy()).read(fileData, 0).get();
        fileData.position(0);
        if (!expectedHeader.equals(fileData)) {
            expectedHeader.position(0);
            ((AsynchronousFileChannel) lock.acquiredBy()).truncate(0);
            ((AsynchronousFileChannel) lock.acquiredBy()).write(expectedHeader, 0).get();
        }
        lock.release();
    }
}
