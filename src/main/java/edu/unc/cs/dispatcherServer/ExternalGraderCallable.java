package edu.unc.cs.dispatcherServer;

import java.util.concurrent.Callable;

public class ExternalGraderCallable implements Callable<Integer> {
	private final String command;
	
	public ExternalGraderCallable(String command) {
		this.command = command;
	}
	
	@Override
	public Integer call() throws Exception {
		ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
		pb.inheritIO();
		try {
			System.out.println("*** Running command: " + command);
			Process p = pb.start();
			boolean doWait = true;
			int ret = -1;
			while(doWait) {
				try {
					ret = p.waitFor();
					doWait = false;
				} catch (InterruptedException e) {
					
				}
			}
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	public static ExternalGraderCallable of(String command) {
		return new ExternalGraderCallable(command);
	}
}
