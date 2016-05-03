package edu.unc.cs.dispatcherServer;

import gradingTools.server.DriverServerObject;
import inputport.ConnectionListener;
import inputport.InputPort;
import inputport.rpc.duplex.AnAbstractDuplexRPCServerPortLauncher;
import inputport.rpc.duplex.DuplexRPCServerInputPort;
import port.ATracingConnectionListener;
import port.PortAccessKind;




public class ADispatcherServerLauncher extends AnAbstractDuplexRPCServerPortLauncher implements DispatcherServerLauncher   {
	public ADispatcherServerLauncher(String aServerName,
			String aServerPort) {
		super (aServerName, aServerPort);
	}
	public ADispatcherServerLauncher() {
	}

	protected DispatcherRegistry getDispatcherRegistry() {
		return new ADispatcherRegistry();
	}
	
//	protected  ConnectionListener getConnectionListener (InputPort anInputPort) {
//		return new ATracingConnectionListener(anInputPort);
//	}
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
	}
}
