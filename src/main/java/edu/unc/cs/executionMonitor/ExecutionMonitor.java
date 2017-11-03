package edu.unc.cs.executionMonitor;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Collections;
import java.util.Set;

public class ExecutionMonitor {

	private static final boolean isPosix = FileSystems.getDefault().supportedFileAttributeViews().contains("posix");

	public static final Set<PosixFilePermission> S_ug_rw;
	public static final FileAttribute<?> FA_ug_rw;

	static {
		S_ug_rw = Collections.unmodifiableSet(PosixFilePermissions.fromString("rw-rw----"));
		FA_ug_rw = PosixFilePermissions.asFileAttribute(S_ug_rw);
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("No monitor file or kill script specified");
			System.exit(-1);
		} else if (args.length < 2) {
			System.out.println("No kill script specified");
		}
		Path monitorFile = Paths.get(args[0]);
		Path fullMonitorPath = monitorFile.toAbsolutePath().normalize();
		Path killScript = Paths.get(args[1]);
		Path monitorDir = fullMonitorPath.getParent();
		if (Files.notExists(fullMonitorPath)) {
			System.out.println("Creating monitor file");
			try {
				createRWFile(fullMonitorPath);
			} catch (UnsupportedOperationException | SecurityException | IOException e) {
				System.out.println("Couldn't create monitor file");
			}
		}
		try {
			WatchService watcher = FileSystems.getDefault().newWatchService();
			monitorDir.register(watcher, StandardWatchEventKinds.ENTRY_DELETE);
			while (!Thread.currentThread().isInterrupted()) {
				WatchKey key = null;
				try {
					key = watcher.take();
				} catch (InterruptedException e) {
					System.exit(-1);
				}

				for (WatchEvent<?> event : key.pollEvents()) {
					WatchEvent.Kind<?> kind = event.kind();
					if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
						@SuppressWarnings("unchecked")
						WatchEvent<Path> pEvent = (WatchEvent<Path>) event;
						Path deletedPath = pEvent.context().toAbsolutePath();
						if (fullMonitorPath.equals(deletedPath)) {
							System.out.println("Kill triggered");
							ProcessBuilder pb = new ProcessBuilder("bash", "-c", killScript.toString());
							pb.inheritIO();
							Process p = pb.start();
							try {
								p.waitFor();
							} catch (InterruptedException e) {
								System.exit(-1);
							} finally {
								System.out.println("Recreating monitor file");
								try {
									createRWFile(fullMonitorPath);
								} catch (UnsupportedOperationException | SecurityException | IOException e) {
									System.out.println("Couldn't recreate monitor file");
								}
							}
						}
					}
				}
				if (!key.reset()) {
					System.exit(-1);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private static Path createRWFile(Path path)
			throws UnsupportedOperationException, FileAlreadyExistsException, IOException, SecurityException {
		if (isPosix) {
			if (Files.notExists(path)) {
				Files.createFile(path, FA_ug_rw);
			}
			Files.setPosixFilePermissions(path, S_ug_rw);
			return path;
		} else {
			if (Files.notExists(path)) {
				Files.createFile(path);
			}
			File file = path.toFile();
			file.setReadable(true);
			file.setWritable(true);
			return path;
		}
	}

}
