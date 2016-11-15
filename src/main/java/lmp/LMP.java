package lmp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.IntConsumer;

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

        public static ParallelContext getContext(SectionsContext sectionsContext) {
            return contexts.values().stream().filter((p) -> p.getSectionsContext() == sectionsContext).findFirst().get();
        }
    }

    private static class ParallelContext {

        private int threadCount;
        private CountDownLatch startupLatch;
        private CountDownLatch barrierLatch;
        private Set<Thread> threads;
        private SingleContext singleContext;
        private SectionsContext sectionsContext;

        public ParallelContext(int threadCount) {
            this.threadCount = threadCount;
            startupLatch = new CountDownLatch(threadCount);
            threads = new HashSet<>();
            singleContext = new SingleContext();
            sectionsContext = new SectionsContext();
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

        public SingleContext getSingleContext() {
            return singleContext;
        }

        public SectionsContext getSectionsContext() {
            return sectionsContext;
        }

        public synchronized CountDownLatch getBarrierLatch() {
            if(barrierLatch == null || barrierLatch.getCount() == 0){
                barrierLatch = new CountDownLatch(threadCount);
            }
            return barrierLatch;
        }
    }

    private static class SingleContext {

        private Lock lock;
        private boolean done;
        private CountDownLatch latch;

        SingleContext() {
            lock = new ReentrantLock();
            done = false;
            latch = new CountDownLatch(LMP.getThreadCount());
        }

        public void lock() {
            lock.lock();
            if (latch.getCount() == 0) {
                latch = new CountDownLatch(LMP.getThreadCount());
                done = false;
            }
        }

        public void markDone() {
            done = true;
        }

        public boolean isDone() {
            return done;
        }

        public void sync() {
            lock.unlock();
            latch.countDown();
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private static class SectionsContext {

        private ThreadLocal<Integer> sectionCounter;

        private Set<Integer> sectionsExecuted;

        public SectionsContext(){
            sectionsExecuted = new HashSet<>();
            sectionCounter = new ThreadLocal<>();
        }

        public synchronized boolean checkExecution(Runnable sectionRegion) {
            int sectionId = getSectionId();
            if(sectionsExecuted.contains(sectionId)){
                return false;
            } else {
                sectionsExecuted.add(sectionId);
                return true;
            }
        }


        public int getSectionId() {
            if(sectionCounter.get() == null) {
                sectionCounter.set(0);
            }
            int sectionId = sectionCounter.get();
            sectionCounter.set(sectionCounter.get() + 1);
            return sectionId;
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

    public static void single(Runnable singleRegion) {
        if (singleRegion == null) {
            throw new NullPointerException("Provided single region is empty (Runnable object is null");
        }
        ParallelContext context = Control.getContext();
        if (context == null) {
            return;
        }
        SingleContext singleContext = context.getSingleContext();
        singleContext.lock();
        if (!singleContext.isDone()) {
            singleRegion.run();
            singleContext.markDone();
        }
        singleContext.sync();
    }

    public static void loop(int from, int to, int step,IntConsumer loopRegion){
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static void sections(Runnable sectionsRegion){
        if(sectionsRegion == null){
            throw new IllegalArgumentException("Sections region is empty (Runnable is null)");
        }
        sectionsRegion.run();
        LMP.barrier();
    }

    public static void section(Runnable sectionRegion){
        if(sectionRegion == null){
            throw new IllegalArgumentException("Section region is empty (Runnable is null)");
        }
        SectionsContext context = Control.getContext().getSectionsContext();
        if(context.checkExecution(sectionRegion)){
            sectionRegion.run();
        }
    }

    public static void barrier(){
        CountDownLatch barrierLatch = Control.getContext().getBarrierLatch();
        barrierLatch.countDown();
        try {
            barrierLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void critical(Runnable criticalRegion){
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static boolean inParallel(){
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
