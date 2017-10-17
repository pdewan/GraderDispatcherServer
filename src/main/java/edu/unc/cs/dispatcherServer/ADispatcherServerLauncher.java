package edu.unc.cs.dispatcherServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import edu.unc.cs.niograderserver.GraderWebServer;
import inputport.ConnectionListener;
import inputport.InputPort;
import inputport.rpc.duplex.AnAbstractDuplexRPCServerPortLauncher;
import inputport.rpc.duplex.DuplexRPCServerInputPort;
import port.ATracingConnectionListener;
import edu.unc.cs.dispatcherServer.DispatcherRegistry;

public class ADispatcherServerLauncher extends AnAbstractDuplexRPCServerPortLauncher implements DispatcherServerLauncher   {
	static final String GRADER_REGISTRY_FILE_NAME = "config/graderRegistry.csv";
	static DispatcherServerLauncher singleton;
	Map<String, String> graderRegistry = new HashMap<>();
	
	private ExecutorService executor;
	
	void init() {
		singleton = this;
		executor = new ThreadPoolExecutor(1,Integer.MAX_VALUE,10,TimeUnit.SECONDS, new LinkedBlockingQueue<>());
		
		try {
		List<String> aLines = Files.readAllLines(Paths.get(GRADER_REGISTRY_FILE_NAME));
		for (String aLine:aLines) {
			String[] aPair = aLine.split(",");
			graderRegistry.put(aPair[0].trim(), aPair[1].trim());
		}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	protected void execAll() {
		for (String aClientName:graderRegistry.keySet()) {
			exec(aClientName);
		}
	}
	@Override
	public Future<Void> exec(String aClientName) {
		
		String aCommand = getCommand(aClientName);
		if (aCommand != null) {
			// runs the command and will restart it on failure unless it fails 3 times in one second
			System.out.println("*** Submitting command: " + aCommand);
			return executor.submit(RestartingExternalGraderCallable.of(aCommand, 3, 1, ChronoUnit.SECONDS));
		} else {
			System.out.println("*** Null command");
			return null;
		}
		
	}
	@Override
	public String getCommand(String aClientName) {
		return graderRegistry.get(aClientName);
	}
	
	public static DispatcherServerLauncher getSingleton() {
		return singleton;
	}
	
	public ADispatcherServerLauncher(String aServerName,
			String aServerPort) {
		super (aServerName, aServerPort);
		init();
	}
	public ADispatcherServerLauncher() {
		init();
	}

	protected DispatcherRegistry getDispatcherRegistry() {
		return new ADispatcherRegistry();
	}
	
	protected  ConnectionListener getConnectionListener (InputPort anInputPort) {
		return new ATracingConnectionListener(anInputPort);
	}
//	protected PortAccessKind getPortAccessKind() {
//		return PortAccessKind.DUPLEX;
//	}

	protected void registerRemoteObjects() {
		DuplexRPCServerInputPort anRPCServerInputPort = (DuplexRPCServerInputPort) mainPort;
		DispatcherRegistry aDispatcherRegistry = getDispatcherRegistry();
		anRPCServerInputPort.register(aDispatcherRegistry);
	}
	
//	public static String computeServerId(int aServerNumber) {
//		int aBaseNumber = Integer.parseInt(DRIVER_SERVER_ID);
//
//		return "" + (aBaseNumber + aServerNumber);
//	}
	
	public static void main (String[] args) {

		ADispatcherServerLauncher laucher = new ADispatcherServerLauncher(DISPATCHER_SERVER_NAME, DISPATCHER_SERVER_ID);
		laucher.launch();
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		// on JVM shutdown kill any processes the executor is running
		Runtime.getRuntime().addShutdownHook(new Thread(() -> laucher.executor.shutdownNow()));
		
		// start all grading handlers in the config file
		laucher.execAll();
		
		try {
			GraderWebServer.main(new String[]{});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
