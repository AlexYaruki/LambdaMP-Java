package lmp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class LMP {

    private LMP(){

    }

    private static class Control {
        static final int DEFAULT_THREAD_COUNT;
        static final AtomicInteger threadCount;
        private static Map<Thread,ParallelContext> contexts;
        static {
            DEFAULT_THREAD_COUNT = Runtime.getRuntime().availableProcessors();
            threadCount = new AtomicInteger(DEFAULT_THREAD_COUNT);
            contexts = new HashMap<>();
        }

        public static void removeContextByThread(Thread thread) {
            contexts.remove(thread);
        }

        public static void mapContext(Thread thread, ParallelContext context) {
            contexts.put(thread,context);
        }

        public static ParallelContext getContext() {
            return contexts.get(Thread.currentThread());
        }
    }

    private static class ParallelContext {

        private int threadCount;
        private CountDownLatch startupLatch;
        private Set<Thread> threads;

        public ParallelContext(int threadCount) {
            this.threadCount = threadCount;
            startupLatch = new CountDownLatch(threadCount);
            threads = new HashSet<>();
        }

        public int getThreadCount() {
            return threadCount;
        }

        public void liftStartupLatch() {
            startupLatch.countDown();
            try {
                startupLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void waitOnStartupLatch(){
            try {
                startupLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void addThread(Thread thread){
            threads.add(thread);
        }

        public void cleanup() {
            for(Thread thread : threads){
                Control.removeContextByThread(thread);
            }
            threads.clear();
        }
    }

    private static class ParallelThreadFactory implements ThreadFactory {

        private ParallelContext context;

        public ParallelThreadFactory(ParallelContext context){
            this.context = context;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            Control.mapContext(thread,context);
            context.addThread(thread);
            return thread;
        }
    }

    private static class ParallelRunner implements Runnable {

        private Runnable parallelRegion;
        private ParallelContext context;


        public ParallelRunner(Runnable parallelRegion,ParallelContext context){
            this.parallelRegion = parallelRegion;
            this.context = context;
        }

        @Override
        public void run() {
            context.liftStartupLatch();
            parallelRegion.run();
        }
    }

    public static int getDefaultThreadCount(){
        return Control.DEFAULT_THREAD_COUNT;
    }

    public static int getThreadCount(){
        ParallelContext context = Control.getContext();
        if(context == null){
            return Control.threadCount.get();
        } else {
            return context.getThreadCount();
        }
    }

    public static void setThreadCount(final int threadCount) {
        if(threadCount < 1){
            Control.threadCount.set(Control.DEFAULT_THREAD_COUNT);
        } else {
            Control.threadCount.set(threadCount);
        }
    }

    public static void parallel(Runnable parallelRegion) {
        if (parallelRegion == null) {
            throw new NullPointerException("Provided parallel region is empty (Runnable object is null");
        }
        ParallelContext context = new ParallelContext(Control.threadCount.get());
        ParallelThreadFactory threadFactory = new ParallelThreadFactory(context);
        ExecutorService pool = Executors.newFixedThreadPool(context.getThreadCount(), threadFactory);
        for (int i = 0; i < context.getThreadCount(); i++) {
            pool.execute(new ParallelRunner(parallelRegion, context));
        }
        context.waitOnStartupLatch();
        pool.shutdown();
        try {
            boolean terminated = false;
            while (!terminated) {
                terminated = pool.awaitTermination(10, TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        context.cleanup();
    }

    public static void single(Runnable singleRegion){

    }

}
