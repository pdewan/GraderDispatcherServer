package edu.unc.cs.dispatcherServer;

import gradingTools.server.RemoteGraderServer;

public class ADispatcherRegistry implements DispatcherRegistry {


	@Override
	public void registerDriverServer(RemoteGraderServer aServerObject,
			GraderServerDescription anAssignmentDescription) {
		
		AGraderServerManager.getDispatcherManager().
		register(new AGraderServerRegistration(aServerObject, anAssignmentDescription));
	}

	

}
