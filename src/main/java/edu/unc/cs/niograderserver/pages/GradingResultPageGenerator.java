package edu.unc.cs.niograderserver.pages;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import edu.unc.cs.niograderserver.pages.helpers.GradePageManager;
import edu.unc.cs.httpserver.pages.IPageGenerator;
import edu.unc.cs.httpserver.util.ResponseStatusNotice;
import edu.unc.cs.niograderserver.utils.ConfigReader;
import edu.unc.cs.niograderserver.utils.IConfigReader;
import org.apache.commons.fileupload.FileItem;
import org.apache.http.HttpStatus;

public class GradingResultPageGenerator implements IGradingResultPageGenerator {

    private static final Logger LOG = Logger.getLogger(GradingResultPageGenerator.class.getName());
    private final String[] METHODS = new String[]{"GET", "POST"};
    private static final boolean DO_IP_REGULATE;
    
    static {
        boolean ipRegulate = false;
        try {
            IConfigReader config = new ConfigReader("config/config.properties");
            ipRegulate = config.getBoolean("grader.ip_regulate", false).get();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        DO_IP_REGULATE = ipRegulate;
    }
    
    @Override
    public String getPage(Optional<FileItem[]> request) throws ResponseStatusNotice {
        String id = null;
        String ip = null;
        
        FileItem[] args = request.orElseThrow(() -> new ResponseStatusNotice(HttpStatus.SC_BAD_REQUEST));
        for (FileItem arg : args) {
            switch (arg.getFieldName()) {
                case "id":
                    id = arg.getString();
                    break;
                case "ip":
                    ip = arg.getString();
                    break;
            }
        }
        
        if (id == null || ip == null) {
            throw new ResponseStatusNotice(HttpStatus.SC_BAD_REQUEST);
        }
        if (!isAllowed(id, ip)) {
            throw new ResponseStatusNotice(HttpStatus.SC_FORBIDDEN);
        }
        if (!doesExist(id)) {
            throw new ResponseStatusNotice(HttpStatus.SC_NOT_FOUND);
        }

        GradePageManager.refresh(id);
        IPageGenerator generator = getPageGenerator(id);
        
        return generator.getPage(request);
    }

    private boolean doesExist(String id) {
        Optional<IPageGenerator> file = GradePageManager.get(id);
        return file.isPresent();
    }

    private IPageGenerator getPageGenerator(String id) {
        Optional<IPageGenerator> generator = GradePageManager.get(id);
        return generator.get();
    }

    private boolean isAllowed(String id, String ip) {
        if (DO_IP_REGULATE) {
            if (id != null && !id.isEmpty()) {
                Optional<String> pageIp = GradePageManager.getIP(id);
                return pageIp.orElse("").equals(ip);
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    public String[] getValidMethods() {
        return METHODS;
    }
}
