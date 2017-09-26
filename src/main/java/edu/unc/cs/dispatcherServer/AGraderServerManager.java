package edu.unc.cs.dispatcherServer;

import java.util.ArrayList;
import java.util.List;

import gradingTools.server.RemoteGraderServer;

public class AGraderServerManager implements GraderServerManager{
	List<GraderServerRegistration> graderServerDescrptions = new ArrayList();
	@Override
	public void register(GraderServerRegistration aDriverRegistration) {
		graderServerDescrptions.add(aDriverRegistration);
//		aDriverRegistration.getRemoteGraderServer().drive(new String[]{});
	}
	@Override
	public void unregister(String aClientName) {
		int aRegistryIndex = 0;
		for (;aRegistryIndex < graderServerDescrptions.size(); aRegistryIndex++) {
			if (graderServerDescrptions.get(
					aRegistryIndex).getGraderServerDescription().getClientName().equals(aClientName)) {
				break;
			}
		}
		if (aRegistryIndex < graderServerDescrptions.size() ) {
			graderServerDescrptions.remove(aRegistryIndex);
		} else {
			System.err.println("Client " + aClientName + " not found");
		}
	}

	@Override
	public RemoteGraderServer getGraderServerObject(
			GraderServerDescription anAssignmentDescription) {
		if (graderServerDescrptions.size() == 0)
			return null;
		return graderServerDescrptions.get(0).getRemoteGraderServer();
	}
	
	static GraderServerManager singleton = new AGraderServerManager();
	public static GraderServerManager getDispatcherManager() {
		return singleton;
	}
}
