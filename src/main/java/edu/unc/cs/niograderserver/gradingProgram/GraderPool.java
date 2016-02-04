package edu.unc.cs.niograderserver.gradingProgram;

import edu.unc.cs.niograderserver.utils.ConfigReader;
import edu.unc.cs.niograderserver.utils.IConfigReader;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GraderPool {

    private static final int DEFAULT_GRADERS = 1;
    private static final ExecutorService graderPool;
    private static AtomicInteger current;
    private static AtomicInteger total;

    static {
        current = new AtomicInteger(0);
        total = new AtomicInteger(0);
        int graders;
        try {
            IConfigReader config = new ConfigReader("./config/config.properties");
            graders = config.getInt("grader.maxGraders").orElse(DEFAULT_GRADERS);
        } catch (IOException ex) {
            Logger.getLogger(GraderPool.class.getName()).log(Level.SEVERE, null, ex);
            graders = DEFAULT_GRADERS;
        }
        graderPool = Executors.newFixedThreadPool(graders);
    }

    public static int getPendingForRun(int run) {
        return run - current.get();
    }

    public static GraderFutureHolder runGrader(String[] args) throws IOException, InterruptedException {
        Future<String> grader = graderPool.submit(new GraderCallable(args));
        int number = total.incrementAndGet();
        GraderFutureHolder graderHolder = new GraderFutureHolder(grader, number - 1);

        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean doContinue = true;
                do {
                    if (grader.isDone()) {
                        current.incrementAndGet();
                        doContinue = false;
                        Logger.getLogger(GraderPool.class.getName()).log(Level.FINER, null, "Grading complete");
                    } else {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(GraderPool.class.getName()).log(Level.SEVERE, null, ex);
                            doContinue = false;
                        }
                    }
                } while (doContinue);
            }
        }).start();

        return graderHolder;
    }

    public static int getPendingTotal() {
        return total.get() - current.get();
    }

    private GraderPool() {
    }
}
