package edu.unc.cs.dispatcherServer;

import gradingTools.server.RemoteGraderServer;

public class AGraderServerRegistration implements GraderServerRegistration{
	RemoteGraderServer driverServerObject;
	GraderServerDescription assignmentDescription;
	
	public AGraderServerRegistration(RemoteGraderServer driverServerObject,
			GraderServerDescription assignmentDescription) {
		super();
		this.driverServerObject = driverServerObject;
		this.assignmentDescription = assignmentDescription;
	}
	public RemoteGraderServer getRemoteGraderServer() {
		return driverServerObject;
	}
	
	public GraderServerDescription getGraderServerDescription() {
		return assignmentDescription;
	}
	
	

}
