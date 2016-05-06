/**
 *
 */
package edu.unc.cs.niograderserver.gradingProgram;

import edu.unc.cs.dispatcherServer.AGraderServerManager;
import edu.unc.cs.niograderserver.utils.ConfigReader;
import edu.unc.cs.niograderserver.utils.IConfigReader;
import gradingTools.client.AGraderServerClientLauncher;
import gradingTools.client.GraderServerClientLauncher;
import gradingTools.server.GraderServerLauncher;
import gradingTools.server.RemoteGraderServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.Callable;

/**
 * @author Andrew
 * @author Prasun
 *
 */
public class GraderCallable implements Callable<String> {
	public static final String GRADER_SERVER_CLASS_NAME = "gradingTools.server.ADriverServerLauncher";
    public static final String GRADER_SERVER_HOST_NAME = "localhost";
    public static final int MIN_TIME_BETWEEN_ATTEMPTS = 10000000;
    protected long lastServerCreationAttempt;
	private final String[] args;
    int serverNumber = 0;
    protected Map<Integer, GraderServerClientLauncher> graderServerToClient = new HashMap();
    protected Map<Integer, ProcessBuilder> graderServerToProcessBuilder = new HashMap();
    protected Map<Integer, BufferedReader> graderServerToOutput = new HashMap();

    public GraderCallable(String[] args) {
        this.args = Arrays.copyOf(args, args.length);
    }
    public GraderCallable(String[] args, int aServerNumber) {
    	this (args);
    	serverNumber = aServerNumber;
    }

    @Override
    public String call() throws Exception {
    	try {
//       return forkGraderDriver();
//    	return useGraderServerOrDriver();
    		return useRegisteredGraderServer();
    	} catch (Exception e) {
    		e.printStackTrace();
    		return null;
    	}
    }
    protected String getOutput(int aServerNumber) {
    	BufferedReader aBufferedReader = graderServerToOutput.get(aServerNumber);
    	if (aBufferedReader == null) {
    		System.out.println("Using existing grader server, cannot get output, returning empty string");
    		return "";
    	}
    	StringBuilder output = new StringBuilder(500);
        try  {
            while (aBufferedReader.ready()) {
            	String aNextLine = aBufferedReader.readLine();
                output.append(aNextLine).append("\n");
                if (RemoteGraderServer.END_DRIVE.equals(aNextLine))
                	break;
            }
//        }
            System.out.println ("Got output:" + output);
        return output.toString();
        } catch (Exception e) {
        	e.printStackTrace();
        	return "";
        }
    	
    }
    protected String useGraderServerOrDriver() throws Exception{
    	GraderServerClientLauncher aClientLauncher = getOrCreateConnectedGraderClient(serverNumber);
    	if (aClientLauncher == null) {
    		return forkGraderDriver();
    	}
    	System.out.println("Getting driver server proxy and calling drive");
    	Object retVal = aClientLauncher.getDriverServerProxy().drive(args);
    	System.out.println("Received from drive:" + retVal);

    	if (retVal != null) {
    		System.err.println("Could not successfully call remote method in server");
    		((Exception)retVal).printStackTrace();
    		return ((Exception) retVal).getMessage();
    	} else {
    		
//    		return getOutput(serverNumber);
    		return "";
    	}
    }
    /**
     * @return
     * @throws Exception
     */
    protected String useRegisteredGraderServer() throws Exception{
    	RemoteGraderServer aDriverServerObject = AGraderServerManager.getDispatcherManager().getGraderServerObject(null);
    	if (aDriverServerObject == null) {
    		System.out.println ("could not find driver server object, forking grader server");
    		return forkGraderDriver();
    	}
		System.out.println ("Calling drive in grader server with args:" + Arrays.toString(args));
    	Object retVal = aDriverServerObject.drive(args);
    	if (retVal != null) {
    		System.err.println("Could not successfully call remote method in server");
    		((Exception)retVal).printStackTrace();
    		return ((Exception) retVal).getMessage();
    	} else {
    		
    		return "";
    	}
    	
    }
    protected GraderServerClientLauncher getOrCreateConnectedGraderClient(int aServerNumber) throws Exception {
    	GraderServerClientLauncher aDriverClientLauncher = graderServerToClient.get(aServerNumber);
    	if (aDriverClientLauncher == null) {
    		System.out.println ("creating first client for" + aServerNumber);
    		aDriverClientLauncher = createGraderClient(aServerNumber);
    		
    	}
    	
    	if (!aDriverClientLauncher.getMainPort().isConnected(GraderServerLauncher.DRIVER_SERVER_NAME)) {
    		if (lastServerCreationAttempt != 0 && (System.currentTimeMillis() - lastServerCreationAttempt) < MIN_TIME_BETWEEN_ATTEMPTS ) {
        		System.out.println ("not forking server as last attempt was made recently");
        		return null;
        	}
    		forkGraderServer(aServerNumber);
		}
    	if (!aDriverClientLauncher.getMainPort().isConnected(GraderServerLauncher.DRIVER_SERVER_NAME)) {
    		return null;
    	}
    	
    	return aDriverClientLauncher; 
    	
    }
    
    
    protected GraderServerClientLauncher createGraderClient(int aServerNumber) throws Exception {
    	GraderServerClientLauncher aDriverClientLauncher = AGraderServerClientLauncher.createAndLaunch(GRADER_SERVER_HOST_NAME, aServerNumber);
    	System.out.println ("Created testing client launcher");
//    	Thread.sleep(1000); // just wait a bit for connection
    	if (aDriverClientLauncher == null || !aDriverClientLauncher.getMainPort().isConnected(GraderServerLauncher.DRIVER_SERVER_NAME)) {
    	forkGraderServer(aServerNumber); 
    	 aDriverClientLauncher = AGraderServerClientLauncher.createAndLaunch(GRADER_SERVER_HOST_NAME, aServerNumber);
    	} else {
    		System.out.println ("Connected to existing grader server");
    	}
    	 graderServerToClient.put(aServerNumber, aDriverClientLauncher);
		aDriverClientLauncher.getSynchronizingConnectionListener().waitForConnectionStatus();
		

    	return aDriverClientLauncher;
    }

    protected String forkGraderServer(int aServerNumber) throws Exception {
    	System.out.println ("forking grader server");
    	lastServerCreationAttempt = System.currentTimeMillis();
    	
        ProcessBuilder lsPb;
        if (System.getProperty("os.name").contains("Windows")) {
            lsPb = new ProcessBuilder("dir", "target");
        } else {
            lsPb = new ProcessBuilder("ls", "target");
        }
        lsPb.redirectErrorStream(true);
        lsPb.directory(new File("graderProgram"));
        Process lspr = lsPb.start();
        lspr.waitFor();
        StringBuilder sb = new StringBuilder(100);
        try (InputStream is = lspr.getInputStream()) {
            while (is.available() != 0) {
                sb.append((char) is.read());
            }
        }

        String sbstr = sb.toString();

        int start = sbstr.indexOf("comp401-grader-");
        int end = sbstr.indexOf(".jar", start);

        String jar = Paths.get(".", "target", sbstr.substring(start, end + 4)).toString();

        
        IConfigReader config = new ConfigReader("./config/config.properties");
        Optional<String> loc = config.getString("grader.java.loc");
        ArrayList<String> command = new ArrayList<>(5);
        if (loc.isPresent()) {
            command.add(loc.get());
        } else {
            command.add("java");
        }
        
        command.add("-cp");
        StringBuilder runClasspath = new StringBuilder();
        String separator = System.getProperty("path.separator");
        runClasspath.append(".").append(separator).append(jar);
        Optional<String> classpath = config.getString("grader.java.classpath");
        if (classpath.isPresent()) {
            runClasspath.append(separator).append(classpath.get());
            /*String systemClasspath = System.getProperty("java.class.path");
            if (systemClasspath == null || systemClasspath.isEmpty()) {
                command.add("\"." + separator + classpath.get() + "\"");
            } else {
                command.add("\"." + separator + systemClasspath + separator + classpath.get() + "\"");
            }*/
        }
        command.add(runClasspath.toString());
        //command.add("-jar");
        //command.add(jar);
        command.add(GRADER_SERVER_CLASS_NAME);
//          args to the server
        String[] serverArgs = new String[] {"" + aServerNumber};
        command.addAll(Arrays.asList(serverArgs));
        
        System.out.print("\nCMD: ");
        for(String s : command) {
            System.out.print(s + " ");
        }
        System.out.println();
//        System.out.println ("Fork #:" + numGraderServerStarts);
//        numGraderServerStarts++;
        

        ProcessBuilder jarPb = new ProcessBuilder(command);
        jarPb.inheritIO();
//        jarPb.redirectErrorStream(true);
        jarPb.directory(new File("graderProgram"));
        System.out.println ("Starting Grader Server");

        Process pr = jarPb.start();
        // we have to now wait for start output, not termination
       // pr.waitFor();
//        System.out.println ("Grader Server Returned Output");
        StringBuilder output = new StringBuilder(500);
//        Scanner aScanner = new Scanner(pr.getInputStream());
        BufferedReader br = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        System.out.println("waiting for ready buffered reader");
        int maxTries = 20;
        int numTries = 0;
//        while (!br.ready() && numTries < maxTries) {
//        	Thread.sleep(10000);
//        	numTries++;
//        }
        String aNextLine;
        while (numTries < maxTries){
        	aNextLine = br.readLine();
        	if (aNextLine == null) {
        		System.out.println ("waiting for br");
        		Thread.sleep(10000);
            	numTries++;
        	} else
        		break;
        }
        System.out.println("Buffered reader ready or numTries == maxTries:" + numTries);

//        try (BufferedReader br = new BufferedReader(new InputStreamReader(pr.getInputStream()))) {
//            while (aScanner.hasNext()) { 
            while (true) { 


//        	while (br.ready()) { 
            	System.out.println ("waiting for next line");
//            	String aNextLine = aScanner.nextLine();
            	aNextLine = br.readLine();
            	if (aNextLine == null) {
            		System.out.println("EOF reached");
            		break;
            	}
            		

                output.append(aNextLine).append("\n");
                System.out.println ("Got output line:" + aNextLine);
                if (GraderServerLauncher.DRIVER_SERVER_START_MESSAGE.equals(aNextLine)) {
                	break;
                }
            }
//            graderServerToOutput.put(serverNumber, br);
            graderServerToProcessBuilder.put(serverNumber, jarPb);
//        }        
        System.out.println ("Grading Server Started, gave output:" + output);
        return output.toString();
    }
    protected String forkGraderDriver() throws Exception {
        System.out.println ("forking grader driver");
    	ProcessBuilder lsPb;
        if (System.getProperty("os.name").contains("Windows")) {
            lsPb = new ProcessBuilder("dir", "target");
        } else {
            lsPb = new ProcessBuilder("ls", "target");
        }
        lsPb.redirectErrorStream(true);
        lsPb.directory(new File("graderProgram"));
        Process lspr = lsPb.start();
        lspr.waitFor();
        StringBuilder sb = new StringBuilder(100);
        try (InputStream is = lspr.getInputStream()) {
            while (is.available() != 0) {
                sb.append((char) is.read());
            }
        }

        String sbstr = sb.toString();

        int start = sbstr.indexOf("comp401-grader-");
        int end = sbstr.indexOf(".jar", start);

        String jar = Paths.get(".", "target", sbstr.substring(start, end + 4)).toString();

        
        IConfigReader config = new ConfigReader("./config/config.properties");
        Optional<String> loc = config.getString("grader.java.loc");
        ArrayList<String> command = new ArrayList<>(5);
        if (loc.isPresent()) {
            command.add(loc.get());
        } else {
            command.add("java");
        }
        
        command.add("-cp");
        StringBuilder runClasspath = new StringBuilder();
        String separator = System.getProperty("path.separator");
        runClasspath.append(".").append(separator).append(jar);
        Optional<String> classpath = config.getString("grader.java.classpath");
        if (classpath.isPresent()) {
            runClasspath.append(separator).append(classpath.get());
            /*String systemClasspath = System.getProperty("java.class.path");
            if (systemClasspath == null || systemClasspath.isEmpty()) {
                command.add("\"." + separator + classpath.get() + "\"");
            } else {
                command.add("\"." + separator + systemClasspath + separator + classpath.get() + "\"");
            }*/
        }
        command.add(runClasspath.toString());
        //command.add("-jar");
        //command.add(jar);
        command.add("gradingTools.Driver");
        command.addAll(Arrays.asList(args));
        
        System.out.print("\nCMD: ");
        for(String s : command) {
            System.out.print(s + " ");
        }
        System.out.println();

        ProcessBuilder jarPb = new ProcessBuilder(command);
        jarPb.inheritIO();
        //jarPb.redirectErrorStream(true);
        jarPb.directory(new File("graderProgram"));

        Process pr = jarPb.start();
        pr.waitFor();
        StringBuilder output = new StringBuilder(500);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(pr.getInputStream()))) {
            while (br.ready()) {
                output.append(br.readLine()).append("\n");
            }
        }
        return output.toString();
    }
}
