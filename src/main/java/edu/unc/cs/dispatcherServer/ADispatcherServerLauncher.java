package edu.unc.cs.dispatcherServer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.unc.cs.niograderserver.GraderWebServer;
import gradingTools.server.RemoteGraderServer;
import inputport.ConnectionListener;
import inputport.InputPort;
import inputport.rpc.duplex.AnAbstractDuplexRPCServerPortLauncher;
import inputport.rpc.duplex.DuplexRPCServerInputPort;
import port.ATracingConnectionListener;
import port.PortAccessKind;




public class ADispatcherServerLauncher extends AnAbstractDuplexRPCServerPortLauncher implements DispatcherServerLauncher   {
	static final String GRADER_REGISTRY_FILE_NAME = "config/graderRegistry.csv";
	static DispatcherServerLauncher singleton;
	Map<String, String> graderRegistry = new HashMap();
	
	void init() {
		singleton = this;
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
		


		(new ADispatcherServerLauncher(DISPATCHER_SERVER_NAME, DISPATCHER_SERVER_ID)).launch();
		try {
			GraderWebServer.main(new String[]{});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
