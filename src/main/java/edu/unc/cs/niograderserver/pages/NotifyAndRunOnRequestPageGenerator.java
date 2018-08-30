package edu.unc.cs.niograderserver.pages;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.httpclient.HttpStatus;

import edu.unc.cs.httpserver.util.ResponseStatusNotice;

public class NotifyAndRunOnRequestPageGenerator implements INotifyAndRunOnRequestPageGenerator {

    private static final Logger LOG = Logger.getLogger(NotifyAndRunOnRequestPageGenerator.class.getName());

    private final String[] methods = new String[]{"GET"};
    
    private final String emailList;
    
    private final String subject;
    private final String mailBody;
    
    private final Runnable reaction;
    private final long delay;
    private final TimeUnit unit;
    
    private final String logMessage;
    
    public NotifyAndRunOnRequestPageGenerator(String page, String[] emails, String subject, String message, Runnable reaction) {
    	this(new String[]{page}, emails, subject, message, reaction, 0, null);
    }

    public NotifyAndRunOnRequestPageGenerator(String page, String[] emails, String subject, String message, Runnable reaction, long time, TimeUnit unit) {
    	this(new String[]{page}, emails, subject, message, reaction, time, unit);
    }
    
    public NotifyAndRunOnRequestPageGenerator(String[] pages, String[] emails, String subject, String message, Runnable reaction) {
    	this(pages, emails, subject, message, reaction, 0, null);
    }
    
    public NotifyAndRunOnRequestPageGenerator(String[] pages, String[] emails, String subject, String message, Runnable reaction, long time, TimeUnit unit) {
    	this.reaction = reaction;
    	this.delay = time;
    	this.unit = unit;
    	StringBuilder sb = new StringBuilder();
    	
    	for(String s : emails) {
    		sb.append(s).append(" ");
    	}
    	
    	emailList = sb.toString();
    	this.subject = subject;
    	sb.setLength(0);
    	
    	if (pages.length == 1) {
    		sb.append("Request for invalid page '" + pages[0] + "' detected!\n\n");
    	} else {
    		sb.append("Reqeust for one of the following invalid pages detected!\n");
    		for(String s : pages) {
    			sb.append("    ").append(s).append("\n");
    		}
    		sb.append("\n");
    	}
    	
    	sb.append("Automatic message:\n").append(message);
    	
    	mailBody = sb.toString();
    	
    	sb.setLength(0);
    	if (pages.length == 1) {
    		sb.append("Request for invalid page '" + pages[0] + "'");
    	} else {
    		sb.append("Reqeust for one of the following invalid pages:");
    		for(String s : pages) {
    			sb.append(" ").append(s);
    		}
    	}
    	sb.append(". Sending emails and running reaction.");
    	logMessage = sb.toString();
    }

    @Override
    public String getPageString(Optional<FileItem[]> request) throws ResponseStatusNotice {
    	LOG.log(Level.SEVERE, logMessage);
        try {
    		LOG.log(Level.FINER, "Sending mail in response to request for invalid page.");
    		ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", "echo \""+ mailBody + "\" | mail " + emailList + " -s \"[Grader Message] " + subject + "\"");
    		Process p = pb.start();
    		if (!p.waitFor(1,TimeUnit.SECONDS)) {
    			p.destroy();
				LOG.log(Level.WARNING, "Mail command took more than 1 seconds, assuming failed.");
    		}
    		if (p.exitValue() != 0) {
				LOG.log(Level.WARNING, "Mail command failed with exit value " + p.exitValue() + ".");
    		}
		} catch (IOException | InterruptedException e) {
			LOG.log(Level.WARNING, "Couldn't send error notification message.", e);
		}
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
