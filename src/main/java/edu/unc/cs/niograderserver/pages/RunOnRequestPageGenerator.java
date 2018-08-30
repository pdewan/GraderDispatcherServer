package edu.unc.cs.niograderserver.pages;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.httpclient.HttpStatus;

import edu.unc.cs.httpserver.util.ResponseStatusNotice;

public class RunOnRequestPageGenerator implements IRunOnRequestPageGenerator {

    private static final Logger LOG = Logger.getLogger(RunOnRequestPageGenerator.class.getName());

    private final String[] methods = new String[]{"GET"};
    
    private final String logMessage;
    
    private final Runnable reaction;
    private final long delay;
    private final TimeUnit unit;

    public RunOnRequestPageGenerator(String page, Runnable reaction) {
    	this(new String[]{page}, reaction);
    }
    
    public RunOnRequestPageGenerator(String page, Runnable reaction, long time, TimeUnit unit) {
    	this(new String[]{page}, reaction, time, unit);
    }

    public RunOnRequestPageGenerator(String[] pages, Runnable reaction) {
    	this(pages, reaction, 0, null);
    }
    
    public RunOnRequestPageGenerator(String[] pages, Runnable reaction, long time, TimeUnit unit) {
    	this.delay = time;
    	this.unit = unit;
    	this.reaction = reaction;
    	StringBuilder sb = new StringBuilder();
  	
    	if (pages.length == 1) {
    		sb.append("Request for invalid page '" + pages[0] + "'");
    	} else {
    		sb.append("Reqeust for one of the following invalid pages:");
    		for(String s : pages) {
    			sb.append(" ").append(s);
    		}
    	}
    	sb.append(". Running reaction.");
    	logMessage = sb.toString();
    }

    @Override
    public String getPageString(Optional<FileItem[]> request) throws ResponseStatusNotice {
    	LOG.log(Level.SEVERE, logMessage);
    	ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    	
    	if (delay == 0) {
    		executor.submit(reaction);
    	} else {
    		executor.schedule(reaction, delay, unit);
    	}
    	
    	throw new ResponseStatusNotice(HttpStatus.SC_NO_CONTENT);
    }

    @Override
    public String[] getValidMethods() {
        return methods;
    }
}
