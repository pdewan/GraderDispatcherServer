package edu.unc.cs.niograderserver.gradingProgram;

import java.util.concurrent.Future;

/**
 *
 * @author Andrew Vitkus
 */
public class GraderFutureHolder {

    private final Future<String> future;
    private final int number;

    public Future<String> getFuture() {
        return future;
    }

    public int getNumber() {
        return number;
    }

    public GraderFutureHolder(Future<String> future, int number) {
        this.future = future;
        this.number = number;
    }

}
