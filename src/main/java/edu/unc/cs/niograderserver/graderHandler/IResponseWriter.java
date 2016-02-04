package edu.unc.cs.niograderserver.graderHandler;

import edu.unc.cs.htmlBuilder.IHTMLFile;

/**
 * @author Andrew Vitkus
 *
 */
public interface IResponseWriter {

    public String getResponseText();

    public IHTMLFile getResponse();

    public void setAssignmentName(String name);
}
