package edu.unc.cs.dispatcherServer;

import gradingTools.server.RemoteGraderServer;

public interface GraderServerRegistration {
	public RemoteGraderServer getRemoteGraderServer() ;
	
	public GraderServerDescription getGraderServerDescription() ;

}
