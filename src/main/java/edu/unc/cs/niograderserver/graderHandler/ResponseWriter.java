package edu.unc.cs.niograderserver.graderHandler;

import edu.unc.cs.niograderserver.graderHandler.pages.FailPage;
import edu.unc.cs.niograderserver.graderHandler.pages.IGraderResponsePage;
import edu.unc.cs.htmlBuilder.IHTMLFile;

public class ResponseWriter implements IResponseWriter {

    protected IGraderResponsePage response;

    protected ResponseWriter() {
        response = new FailPage();
    }

    @Override
    public IHTMLFile getResponse() {
        return response;
    }

    @Override
    public String getResponseText() {
        return response.getHTML();
    }

    @Override
    public void setAssignmentName(String name) {
        response.setAssignmentName(name);
    }
}
