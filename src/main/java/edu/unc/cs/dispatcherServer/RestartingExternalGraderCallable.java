package edu.unc.cs.dispatcherServer;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Callable;

public class RestartingExternalGraderCallable implements Callable<Void> {
	private final String command;
	private final int retries;
	private final long time;
	private final ChronoUnit unit;

	public RestartingExternalGraderCallable(String command, int retries, long time, ChronoUnit unit) {
		this.command = command;
		this.retries = retries;
		this.time = time;
		this.unit = unit;
	}

	@Override
	public Void call() throws Exception {
		try {
			Instant start = Instant.now();
			System.out.println("--- Calling");
			ExternalGraderCallable.of(command).call();
			System.out.println("--- Call returned");
			int times = 0;
			while (times < retries) {
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
				times++;
				Instant now = Instant.now();
				if (start.plus(Duration.of(time, unit)).isBefore(now)) {
					times = 0;
					start = now;
				}
				System.out.println("*** Restarting command: " + command);
				System.out.println("--- Calling");
				ExternalGraderCallable.of(command).call();
				System.out.println("--- Call returned");
			}
			System.out.println("*** Retry count exceeded: " + command);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			System.out.println("*** interrupted: " + command);
			throw e;
		}
		return null;
	}

	public static RestartingExternalGraderCallable of(String command, int retries, long time, ChronoUnit unit) {
		if (retries < 0) {
			retries = 0;
		}
		return new RestartingExternalGraderCallable(command, retries, time, unit);
	}
}
