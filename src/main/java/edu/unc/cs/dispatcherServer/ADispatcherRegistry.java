package edu.unc.cs.dispatcherServer;

import gradingTools.server.DriverServerObject;

public class ADispatcherRegistry implements DispatcherRegistry {

	@Override
	public void registerDriverServer(DriverServerObject aServerObject) {
		aServerObject.drive(new String[]{});
	}

	

}
