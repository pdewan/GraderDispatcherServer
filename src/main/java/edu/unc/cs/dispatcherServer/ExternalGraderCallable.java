package edu.unc.cs.dispatcherServer;

import java.util.concurrent.Callable;

public class ExternalGraderCallable implements Callable<Integer> {
	private static int WAIT_FOR_KILL_SLEEP_TIME = 100;
	private static int MAX_WAIT_FOR_KILL_TIME_STEPS = 10;
	private static int FAIL_RETURN_VALUE = -2;
	private static int INTERRUPTED_RETURN_VALUE = -3;
	
	private final String command;
	
	public ExternalGraderCallable(String command) {
		this.command = command;
	}
	
	@Override
	public Integer call() throws Exception {
		try {
			ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
			pb.inheritIO();
			System.out.println("*** Running command: " + command);
			Process p = pb.start();
			boolean doWait = true;
			int ret = -1;
			while(doWait) {
				try {
					ret = p.waitFor();
					doWait = false;
				} catch (InterruptedException e) {
					System.out.println("*** Interrupted ***");
					p.destroy();
					System.out.println("destroying process");
					int sleeps = 0;
					boolean tryForcibly = true;
					try {
						while(p.isAlive() && sleeps < MAX_WAIT_FOR_KILL_TIME_STEPS) {
							Thread.sleep(WAIT_FOR_KILL_SLEEP_TIME);
							sleeps++;
						}
						if (sleeps < MAX_WAIT_FOR_KILL_TIME_STEPS) {
							tryForcibly = false;
						}
					} catch(InterruptedException e2) {
					}
					if (tryForcibly) {
						p.destroyForcibly();
						System.out.println("forcibly destroying process");
						sleeps = 0;
						try {
							while(p.isAlive() && sleeps < MAX_WAIT_FOR_KILL_TIME_STEPS) {
								Thread.sleep(WAIT_FOR_KILL_SLEEP_TIME);
								sleeps++;
							}
						} catch(InterruptedException e2) {
						}
					}
					Thread.currentThread().interrupt();
					throw e;
				}
			}
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public static ExternalGraderCallable of(String command) {
		return new ExternalGraderCallable(command);
	}
}
