package edu.unc.cs.niograderserver;

import edu.unc.cs.httpserver.HTTPServerFactory;
import edu.unc.cs.httpserver.IHttpServer;
import edu.unc.cs.httpserver.pages.nio.NGeneratedPage;
import edu.unc.cs.niograderserver.pages.GraderPageGenerator;
import edu.unc.cs.niograderserver.pages.GradingResultPageGenerator;
import edu.unc.cs.niograderserver.pages.StudentDataLookupPageGenerator;
import edu.unc.cs.niograderserver.pages.StudentDataStatisticsPageGenerator;
import edu.unc.cs.niograderserver.pages.UploadPageGenerator;
import edu.unc.cs.niograderserver.pages.error.Error400PageGenerator;
import edu.unc.cs.niograderserver.pages.error.Error403PageGenerator;
import edu.unc.cs.niograderserver.pages.error.Error404PageGenerator;
import edu.unc.cs.niograderserver.utils.ConfigReader;
import edu.unc.cs.niograderserver.utils.IConfigReader;
import edu.unc.cs.niograderserver.utils.SSLDataGenerator;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.XMLFormatter;

/**
 *
 * @author Andrew Vitkus
 */
public class GraderWebServer {
    
    private static final Logger LOG = Logger.getLogger(GraderWebServer.class.getName());
    
    private static final String DEFAULT_ROOT = "//";

    static {
        Logger serverLog = Logger.getLogger("edu.unc.cs.niograderserver");
        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.ALL);
        serverLog.addHandler(ch);
        try {
            FileHandler fh = new FileHandler("logs/server-%g.%u.log", 0, 50);
            fh.setLevel(Level.ALL);
            fh.setFormatter(new XMLFormatter());
            serverLog.addHandler(fh);
        } catch (IOException | SecurityException ex) {
            LOG.log(Level.FINE, "Failed to add log file writer.", ex);
        }
    }
    
    public static void main(String[] args) throws IOException {
        IConfigReader config = new ConfigReader("config/config.properties");

        Path root = Paths.get(config.getString("server.internal.root", DEFAULT_ROOT).get());
        
        if (config.getBoolean("server.http.enabled", false).get()) {
            config.getInt("server.http.port").ifPresent((port) -> {
                LOG.log(Level.INFO, "Starting HTTP server on {0}", port);
                IHttpServer server = HTTPServerFactory.getNHTTPServer(root, port);
                try {
                    startServer(server);
                    LOG.log(Level.INFO, "HTTP server start success");
                } catch (Exception ex) {
                    LOG.log(Level.INFO, "HTTP server start failure");
                    LOG.log(Level.SEVERE, null, ex);
                } finally {
                    LOG.log(Level.INFO, "End HTTP server startup");
                }
            });
        }
        if (config.getBoolean("server.https.enabled", false).get()) {
            config.getInt("server.https.port").ifPresent((port) -> {
                LOG.log(Level.INFO, "Starting HTTPS server on {0}", port);
                try {
                    IHttpServer server = HTTPServerFactory.getNHTTPSServer(root, port, SSLDataGenerator.getDefaultSSLData());
                    startServer(server);
                    LOG.log(Level.INFO, "HTTPS server start success");
                } catch (Exception ex) {
                    LOG.log(Level.SEVERE, null, ex);
                } finally {
                    LOG.log(Level.INFO, "End HTTPS server startup");
                }
            });
        }
    }
    
    private static void startServer(IHttpServer server) throws Exception {
        LOG.log(Level.INFO, "begin page loading");
        
        server.addPage(new NGeneratedPage(Paths.get("lookup.html"), new StudentDataLookupPageGenerator()));
        LOG.log(Level.INFO, "lookup.html loaded");
        server.addPage(new NGeneratedPage(Paths.get("stats.html"), new StudentDataStatisticsPageGenerator()));
        LOG.log(Level.INFO, "stats.html loaded");
        server.addPage(new NGeneratedPage(Paths.get("upload.html"), new UploadPageGenerator()));
        LOG.log(Level.INFO, "upload.html loaded");
        server.addPage(new NGeneratedPage(Paths.get("submit.html"), new GraderPageGenerator()));
        LOG.log(Level.INFO, "submit.html loaded");
        server.addPage(new NGeneratedPage(Paths.get("grading.html"), new GradingResultPageGenerator()));
        LOG.log(Level.INFO, "grading.html loaded");
        
        server.setErrorPage(400, new NGeneratedPage(null, new Error400PageGenerator()));
        LOG.log(Level.INFO, "error 400 page loaded");
        server.setErrorPage(403, new NGeneratedPage(null, new Error403PageGenerator()));
        LOG.log(Level.INFO, "error 403 page loaded");
        server.setErrorPage(404, new NGeneratedPage(null, new Error404PageGenerator()));
        LOG.log(Level.INFO, "error 404 page loaded");
        
        LOG.log(Level.INFO, "all pages loaded");
        
        LOG.log(Level.INFO, "begin server start");
        new Thread() {
            @Override
            public void run() {
                try {
                    server.start();
                    LOG.log(Level.INFO, "server started");
                } catch (Exception ex) {
                    LOG.log(Level.INFO, "server start failed");
                    LOG.log(Level.SEVERE, null, ex);
                }
            }
        }.start();
    }
    static {
    	System.out.println ("Turning off logging in:" + "GraderWebServer");
        LOG.setLevel(Level.OFF);
    }
}
