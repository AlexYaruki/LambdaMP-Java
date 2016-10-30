package lmp;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LMP {

    public static void parallel(Runnable parallelRegion){
        if(parallelRegion == null){
            throw new NullPointerException("Provided parallel region is empty (Runnable object is null");
        }
        ParallelContext context = new ParallelContext(LMPControl.getThreadCount());
        ParallelThreadFactory threadFactory = new ParallelThreadFactory(context);
        ExecutorService pool = Executors.newFixedThreadPool(context.getThreadCount(),threadFactory);
        for(int i = 0; i < context.getThreadCount(); i++){
            pool.execute(new ParallelRunner(parallelRegion,context));
        }
        context.waitOnStartupLatch();
        pool.shutdown();
        try {
            boolean terminated = false;
            while(!terminated) {
                terminated = pool.awaitTermination(10, TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        context.cleanup();
    }

}
