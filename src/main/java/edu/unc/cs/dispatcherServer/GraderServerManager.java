package edu.unc.cs.dispatcherServer;
import gradingTools.server.RemoteGraderServer;
public interface GraderServerManager {
	public RemoteGraderServer getGraderServerObject(GraderServerDescription aGraderServerDescription);
	public void register (GraderServerRegistration aDriverRegistration);
	void unregister(String aClientName);

}
