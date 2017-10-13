package edu.unc.cs.niograderserver.graderHandler.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.nio.file.attribute.BasicFileAttributes;
import java.util.Calendar;
import java.util.Objects;

import util.trace.Tracer;

public class FileTreeManager {

    private static final Path root = Paths.get("graderProgram", "data");

    public static void purgeSubmission(Path submission) throws IOException {
    	System.out.println ("Purging submission:" + submission);

        purge(submission);
    }

    public static void checkPurgeRoot() throws IOException {
        if (doPurgeRoot()) {
        	Tracer.info(FileTreeManager.class, "Purging root");
            purge(root);
        }
    }
    
    protected final static boolean PURGE_ENABLED = false;

    private static boolean doPurgeRoot() {
    	Tracer.info(FileTreeManager.class, "Checking Purging root");
    	if (!PURGE_ENABLED)
    		return false;
        int year = Calendar.getInstance().get(Calendar.YEAR);
        return root.resolve(Integer.toString(year - 1)).toFile().exists();
    }

    private static void purge(Path p) throws IOException {
//    	System.out.println ("Purging path:" + p);
        if (!p.toFile().exists()) {
            return;
        }
        Files.walkFileTree(p, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!file.toFile().getName().endsWith(".bak") && !file.toFile().getName().equals("grades.csv")) {
//                	System.out.println ("deleting file:" + file);
                	try {
                		Files.deleteIfExists(file);
                	} catch (Exception e) {
                		
                	}
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                if (e == null) {
                    try {
                    	Tracer.info (this,  "deleting dir:" + dir);

                        Files.deleteIfExists(dir);
                    } catch (IOException ex) {
                        if (!(ex instanceof DirectoryNotEmptyException)) {
                            throw ex;
                        }
                    }
                    return FileVisitResult.CONTINUE;
                } else {
                    // directory iteration failed
                    throw e;
                }
            }
        });
    }

    public static void backup(Path file, Path copy) throws FileNotFoundException, IOException {
        /*if (copy.toFile().exists()) {
            Files.delete(copy);
        }
        Files.createFile(copy);*/
        Files.copy(file, copy, StandardCopyOption.REPLACE_EXISTING);
    }

    public static void copy(Path source, Path destination) throws IOException {
        Objects.requireNonNull(source, "Source path cannot be null");
        Objects.requireNonNull(destination, "Source destination path cannot be null");
        if (!source.toFile().exists()) {
            return;
        }
        if (!destination.toFile().exists()) {
            Files.createDirectories(destination);
        }
        CopyOption[] options = new CopyOption[]{COPY_ATTRIBUTES, REPLACE_EXISTING, NOFOLLOW_LINKS};
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Objects.requireNonNull(file);
                Objects.requireNonNull(attrs);
                Files.copy(file, destination.resolve(file), options);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                Objects.requireNonNull(dir);
                if (e == null) {
                    return FileVisitResult.CONTINUE;
                } else {
                    // directory iteration failed
                    throw e;
                }
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Objects.requireNonNull(dir);
                Objects.requireNonNull(attrs);
                Files.copy(dir, destination.resolve(dir), options);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static void move(Path source, Path destination) throws IOException {
        Objects.requireNonNull(source, "Source path cannot be null");
        Objects.requireNonNull(destination, "Source destination path cannot be null");
        if (!source.toFile().exists()) {
            return;
        }
        if (!destination.toFile().exists()) {
            Files.createDirectories(destination);
        }
        CopyOption[] options = new CopyOption[]{COPY_ATTRIBUTES, REPLACE_EXISTING, NOFOLLOW_LINKS};
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Objects.requireNonNull(file);
                Objects.requireNonNull(attrs);
                Files.copy(file, destination.resolve(file), options);
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                Objects.requireNonNull(dir);
                if (e == null) {
                    Files.deleteIfExists(dir);
                    return FileVisitResult.CONTINUE;
                } else {
                    // directory iteration failed
                    throw e;
                }
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Objects.requireNonNull(dir);
                Objects.requireNonNull(attrs);
                Files.copy(dir, destination.resolve(dir), options);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private FileTreeManager() {
    }
}
