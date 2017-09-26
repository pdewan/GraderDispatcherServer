package edu.unc.cs.dispatcherServer;

import inputport.ConnectionType;
import port.ATracingConnectionListener;

public class AServerConnectionListener extends ATracingConnectionListener{
	
	public void disconnected(String aRemoteEnd, boolean anExplicitClose, String aSystemMessage, ConnectionType aConnectionType) {
		try {
		traceDisconnected(aRemoteEnd, anExplicitClose, aSystemMessage, aConnectionType);
		AGraderServerManager.getDispatcherManager().unregister(aRemoteEnd);
		String aCommand = ADispatcherServerLauncher.getSingleton().getCommand(aRemoteEnd);
		if (aCommand != null) {
			Runtime.getRuntime().exec(aCommand);
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
