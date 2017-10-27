package edu.unc.cs.niograderserver.graderHandler.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import util.trace.Tracer;

/**
 *
 * @author Andrew
 */
public class GradeWriter implements IGradeWriter {

    protected final AsynchronousFileChannel gradeFile;
    protected final String assignmentName;
    
    private static final String ENCODING = "UTF-8";
    
    public GradeWriter(String assignmentName, Path gradeFile) throws IOException, FileNotFoundException, InterruptedException, ExecutionException {
        if (!Files.exists(gradeFile)) {
        	Tracer.info(this, "Creating file:" + gradeFile.toFile().getAbsolutePath());
        	FileTreeManager.createRWXFile(gradeFile);
//            Files.createFile(gradeFile, FileTreeManager.ug_rwx);
        }
        this.gradeFile = AsynchronousFileChannel.open(gradeFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ);
        this.assignmentName = assignmentName;
        initializeFile();
    }

    public GradeWriter(String assignmentName, String gradeFilePath) throws IOException, FileNotFoundException, InterruptedException, ExecutionException {
        this(assignmentName, Paths.get(gradeFilePath));
    }

    @Override
    public void write(IGradingData data) throws FileNotFoundException, IOException, InterruptedException, ExecutionException {
    	Future<FileLock> flock = null;
    	do {
    		try {
    			gradeFile.lock();
    		} catch(OverlappingFileLockException e) { }
    	} while(flock == null);
        String newLine = formatData(data);
        Tracer.info (this, "Writing to file:" +newLine);

        FileLock lock = null;
        do {
        	try {
        		lock = flock.get();
        	} catch (InterruptedException e) { }
        } while (lock == null);
        /*
         * Read the file in then build an array of the lines.
         */
        ByteBuffer fileData = ByteBuffer.allocate((int) ((AsynchronousFileChannel) lock.acquiredBy()).size());
        int readLen = ((AsynchronousFileChannel) lock.acquiredBy()).read(fileData, 0).get();
        fileData.position(0);
        StringBuilder str = new StringBuilder(readLen);
        while (fileData.hasRemaining()) {
            str.append(fileData.getChar());
        }
        String[] lines = str.toString().split("\r\n");
        boolean onyenSet = false;
        int loc = 0;
        for (String line : lines) {
            ByteBuffer buf;
            if (line.startsWith(data.getOnyen())) {
                onyenSet = true;
                buf = ByteBuffer.wrap(newLine.getBytes(ENCODING));
            } else {
                line += "\r\n";
                buf = ByteBuffer.wrap(line.getBytes(ENCODING));
            }
            ((AsynchronousFileChannel) lock.acquiredBy()).write(buf, loc);
            loc += buf.capacity();
        }
        if (!onyenSet) {
            ByteBuffer buf = ByteBuffer.wrap(newLine.getBytes(ENCODING));
            ((AsynchronousFileChannel) lock.acquiredBy()).write(buf, loc);
        }
        lock.release();
    }

    protected String formatData(IGradingData data) {
        StringBuilder line = new StringBuilder();
        line.append(data.getOnyen()).append("\t");
        line.append(data.getOnyen()).append("\t");
        line.append(data.getLastName()).append("\t");
        line.append(data.getFirstName()).append("\t");
        line.append(data.getScore() / data.getPossible()).append("\r\n");

        return line.toString();
    }

    protected void initializeFile() throws FileNotFoundException, IOException, InterruptedException, ExecutionException {
        System.out.println ("Initializing file");
        Future<FileLock> flock = null;
    	do {
    		try {
    			gradeFile.lock();
    		} catch(OverlappingFileLockException e) { }
    	} while(flock == null);
        String header = assignmentName + "\tScores\t\t\t\r\nDisplay ID\tID\tLast Name\tFirst Name\tgrade\r\n\t\t\t\t\r\n";
        FileLock lock = null;
        do {
        	try {
        		lock = flock.get();
        	} catch (InterruptedException e) { }
        } while (lock == null);
        ByteBuffer expectedHeader = ByteBuffer.wrap(header.getBytes(ENCODING));
        ByteBuffer fileData = ByteBuffer.allocate(expectedHeader.capacity());
        ((AsynchronousFileChannel) lock.acquiredBy()).read(fileData, 0).get();
        if (!expectedHeader.equals(fileData)) {
            ((AsynchronousFileChannel) lock.acquiredBy()).truncate(0);
            ((AsynchronousFileChannel) lock.acquiredBy()).write(expectedHeader, 0).get();
        }
        lock.release();
    }
}
