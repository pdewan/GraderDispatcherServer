package edu.unc.cs.niograderserver.graderHandler.util;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Calendar;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import util.trace.Tracer;

public class FileTreeManager {

    private static final Path root = Paths.get("graderProgram", "data");
    private static final boolean isPosix =
        FileSystems.getDefault().supportedFileAttributeViews().contains("posix");

    public static final Set<PosixFilePermission> S_ug_rwx;
    public static final FileAttribute<?> FA_ug_rwx;
    
    static {
    	S_ug_rwx = Collections.unmodifiableSet(PosixFilePermissions.fromString("rwxrwx---"));
    	FA_ug_rwx = PosixFilePermissions.asFileAttribute(S_ug_rwx);
    }


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
        if(!Files.exists(p)) {
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
        Files.copy(file, copy, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
    }
    
    public static void copyRWX(Path file, Path copy)  throws FileNotFoundException, IOException {
    Files.copy(file, copy, StandardCopyOption.REPLACE_EXISTING);
    if (isPosix) {
    	Files.setPosixFilePermissions(copy, S_ug_rwx);
    } else {
    	File f = copy.toFile();
    	f.setReadable(true);
    	f.setWritable(true);
    	f.setExecutable(true);
    }
}

    public static void copy(Path source, Path destination) throws IOException {
        Objects.requireNonNull(source, "Source path cannot be null");
        Objects.requireNonNull(destination, "Source destination path cannot be null");
        if (!source.toFile().exists()) {
            return;
        }
        if (!destination.toFile().exists()) {
        	FileTreeManager.createRWXDirectories(destination);
            //Files.createDirectories(destination, ug_rwx);
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
        if (!Files.exists(destination)) {
        	FileTreeManager.createRWXDirectories(destination);
            //Files.createDirectories(destination, ug_rwx);
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
    
    public static Path createRWXFile(Path path) throws UnsupportedOperationException, FileAlreadyExistsException, IOException, SecurityException {
    	if (isPosix) {
    		System.out.println("Creating posix file: " + path);
    		Files.createFile(path, FA_ug_rwx);
    		Files.setPosixFilePermissions(path, S_ug_rwx);
        	return path;
    	} else {
    		System.out.println("Creating file: " + path);
    		Files.createFile(path);
    		File file = path.toFile();
    		file.setReadable(true);
    		file.setWritable(true);
    		file.setExecutable(true);
    		return path;
    	}
    }

    public static Path createRWXDirectories(Path path) throws UnsupportedOperationException, FileAlreadyExistsException, IOException, SecurityException {
    	if (isPosix) {
    		System.out.println("Creating posix directory: " + path);
        	return createRWXPosixDirectories(path);
    	} else {
    		System.out.println("Creating directory: " + path);
    		return createRWXDirectoriesNotPosix(path);
    	}
    }
    
    private static Path createRWXPosixDirectories(Path path) throws UnsupportedOperationException, FileAlreadyExistsException, IOException, SecurityException {
    	if (Files.exists(path)) {
    		return path;
    	}
    	Path parent = path.getParent();
    	if (!Files.exists(parent)) {
    		createRWXPosixDirectories(parent);
    	}
    	Files.createDirectory(path, FA_ug_rwx);
		Files.setPosixFilePermissions(path, S_ug_rwx);
		return path;
    }
    
    private static Path createRWXDirectoriesNotPosix(Path path) throws UnsupportedOperationException, FileAlreadyExistsException, IOException, SecurityException {
    	if (Files.exists(path)) {
    		return path;
    	}
    	Path parent = path.getParent();
    	if (!Files.exists(parent)) {
    		createRWXDirectoriesNotPosix(parent);
    	}
		Files.createDirectory(path);
		File dir = path.toFile();
		dir.setReadable(true);
		dir.setWritable(true);
		dir.setExecutable(true);
		return path;
    }
    

    private FileTreeManager() {
    }
}
