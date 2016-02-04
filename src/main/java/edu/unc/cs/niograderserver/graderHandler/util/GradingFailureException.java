package edu.unc.cs.niograderserver.graderHandler.util;

public class GradingFailureException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of <code>GradingErrorException</code> without
     * detail message.
     */
    public GradingFailureException() {
    }

    /**
     * Constructs an instance of <code>GradingErrorException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public GradingFailureException(String msg) {
        super(msg);
    }
}
