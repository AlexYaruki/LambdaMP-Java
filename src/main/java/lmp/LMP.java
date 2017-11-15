package lmp;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.IntConsumer;
import java.util.logging.Logger;
import java.util.logging.Level;

public final class LMP {

    private final static Logger log;

    public enum ParallelMode {
        Concurrent,
        Serial
    }

    static {
        log = Logger.getLogger("LMP");
    }

    public interface LoopCondition {
        boolean checkCondition(int i);
    }

    public interface LoopStep {
        int step(int i);
    }

    private static ExceptionHandler exceptionHandler;

    private LMP(){

    }

    public static void setExceptionModel(ExceptionModel exceptionModel) {
        Control.exceptionModel = exceptionModel;
    }

    public static ExceptionModel getExceptionModel() {
        return Control.exceptionModel;
    }

    public static Integer getThreadId() {
        final ParallelContext context = Control.getContext();
        if (context == null) {
            return 0;
        }
        final Map<Thread, Integer> threadInteger = context.getThreadRegistry();
        return threadInteger.get(Thread.currentThread());
    }

    public static void setExceptionHandler(ExceptionHandler exceptionHandler) {
        LMP.exceptionHandler = exceptionHandler;
    }

    public static ExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    public enum ExceptionModel {
        DEFAULT,
        HANDLE,
        PROPAGATE,
        //TODO: TERMINATE - Exception terminates when unhandled exception is detected
    }

    private static class Control {
        private static final int DEFAULT_THREAD_COUNT;
        private static final AtomicInteger threadCount;
        private static Map<Thread,ParallelContext> contexts;
        private static ExceptionModel exceptionModel;

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

        private int nextThreadId = 0;
        private int threadCount;
        private CountDownLatch startupLatch;
        private CountDownLatch barrierLatch;
        private Set<Thread> threads;
        private SingleContext singleContext;
        private CriticalContext criticalContext;
        private SectionsContext sectionsContext;
        private Map<Thread, Integer> threadRegistry;
        private Map<Thread,Throwable> exceptionMap;
        private CountDownLatch exceptionFinalizedLatch;
        private LoopContext loopContext;

        public ParallelContext(int threadCount) {
            this.threadCount = threadCount;
            startupLatch = new CountDownLatch(threadCount);
            threads = new HashSet<>();
            singleContext = new SingleContext();
            sectionsContext = new SectionsContext();
            criticalContext = new CriticalContext();
            loopContext = new LoopContext();
            threadRegistry = new HashMap<>();
            exceptionMap = new HashMap<>();
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

        public int addThread(Thread thread){
            final int threadId = nextThreadId++;
            threads.add(thread);
            threadRegistry.put(thread,threadId);
            return threadId;
        }

        public void cleanup() {
            for(Thread thread : threads){
                Control.removeContextByThread(thread);
            }
            threads.clear();
            if(exceptionFinalizedLatch != null){
                try {
                    exceptionFinalizedLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
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

        public CriticalContext getCriticalContext() {
            return criticalContext;
        }

        public Map<Thread,Integer> getThreadRegistry() {
            return threadRegistry;
        }

        public void setExceptionFinalizedLatch(CountDownLatch exceptionFinalizedLatch) {
            this.exceptionFinalizedLatch = exceptionFinalizedLatch;
        }

        public CountDownLatch getExceptionFinalizedLatch() {
            return exceptionFinalizedLatch;
        }

        public void saveException(Thread thread, Throwable e) {
            System.out.println("Saving exception " + e + " for thread " + thread);
            exceptionMap.put(thread,e);
        }

        public LoopContext getLoopContext() {
            return loopContext;
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

    private static class CriticalContext {

        private ThreadLocal<Integer> criticalCounter;
        private Map<Integer,Lock> locks;
        public CriticalContext() {
            criticalCounter = new ThreadLocal<>();
            locks = new ConcurrentHashMap<>();
        }


        public synchronized Lock getCriticalLock(){
            if(criticalCounter.get() == null) {
                criticalCounter.set(0);
            }
            final int criticalId = criticalCounter.get();
            criticalCounter.set(criticalId + 1);
            Lock criticalLock = locks.get(criticalId);
            if(criticalLock == null){
                criticalLock = new ReentrantLock();
                locks.put(criticalId,criticalLock);
            }
            return criticalLock;
        }
    }

    private static class ParallelThreadFactory implements ThreadFactory {

        private ParallelContext context;
        private CountDownLatch exceptionFinalizedLatch = null;

        public ParallelThreadFactory(ParallelContext context) {
            this.context = context;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("LMP.Thread - " + UUID.randomUUID().toString());
            Control.mapContext(thread,context);
            final int threadId = context.addThread(thread);
            if(LMP.getExceptionModel() == ExceptionModel.HANDLE){
                ThreadContextView threadContextView = new ThreadContextView(threadId);
                ExceptionHandler exceptionHandler = LMP.getExceptionHandler();
                if(exceptionFinalizedLatch == null) {
                    exceptionFinalizedLatch = new CountDownLatch(context.getThreadCount());
                    context.setExceptionFinalizedLatch(exceptionFinalizedLatch);
                }
                thread.setUncaughtExceptionHandler((t, ex) -> {
                    exceptionHandler.handleException(t,threadContextView,ex);
                    exceptionFinalizedLatch.countDown();
                });
            }
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
            try {
                parallelRegion.run();
            } catch (Throwable e) {
                if(LMP.getExceptionModel() == ExceptionModel.PROPAGATE) {
                    context.saveException(Thread.currentThread(), e);
                    System.out.println("Saving exception ");

                } else {
                    throw e;
                }
            }
            CountDownLatch latch = context.getExceptionFinalizedLatch();
            if(latch != null){
                latch.countDown();
            }
        }
    }

    private static class LoopRange {

        private final int begin;
        private int current;
        private final int end;

        public LoopRange(int from, int to, int tid, int threadCount) {
            int size = to - from + 1;
            int[] loopSizes = new int[threadCount];
            Arrays.fill(loopSizes,size/threadCount);
            int mod = size%threadCount;
            for(int i = 0; i < threadCount && mod > 0; i++, mod--){
                loopSizes[i]++;
            }
            System.out.println(Arrays.toString(loopSizes));
            for(int i = 1; i < threadCount; i++){
                loopSizes[i] += loopSizes[i-1];
            }

            if(tid == 0) {
                begin = from;
            } else {
                begin = from + loopSizes[tid-1];
            }

            if(tid == threadCount-1) {
                end = to;
            } else {
                end = from + loopSizes[tid] -1;
            }
            current = begin;
        }

        public void step(){
            current++;
        }

        public int getCurrent(){
            return current;
        }

        public boolean hasMoreWork() {
            return current <= end;
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
        parallel(ParallelMode.Concurrent,parallelRegion);
    }


    public static void parallel(ParallelMode parallelMode, Runnable parallelRegion) {
        if(parallelMode == null) {
            throw new NullPointerException("Parallel mode is null");
        }
        if (parallelRegion == null) {
            throw new LMP.NullRegion();
        }
        ParallelContext context;
        if(parallelMode.equals(ParallelMode.Concurrent)) {
            context = new ParallelContext(Control.threadCount.get());
        } else {
            context = new ParallelContext(1);
        }
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
        if(!context.exceptionMap.isEmpty()){
            if(log.isLoggable(Level.FINE)) {
                log.fine("Exception map count: " + context.exceptionMap.size());
            }
            throw new ParallelException("Exception thrown during " + context,Collections.unmodifiableMap(context.exceptionMap));
        }
    }

    public static void single(Runnable singleRegion) {
        ParallelContext context = Control.getContext();
        if (context == null) {
            throw new LMP.OutsideParallel();
        }
        if (singleRegion == null) {
            throw new LMP.NullRegion();
        }
        SingleContext singleContext = context.getSingleContext();
        singleContext.lock();
        boolean isMarking = false;
        try {
            if(!singleContext.isDone()){
                isMarking = true;
                singleRegion.run();
            }
        } finally {
            if(isMarking){
                singleContext.markDone();
            }
            singleContext.sync();
        }
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

    public static void critical(Runnable criticalRegion) {
        if (criticalRegion == null) {
            throw new LMP.NullRegion();
        }
        ParallelContext context = Control.getContext();
        if(context == null) {
            throw new LMP.OutsideParallel();
        }
        CriticalContext criticalContext = context.getCriticalContext();
        Lock criticalLock = criticalContext.getCriticalLock();
        criticalLock.lock();
        criticalRegion.run();
        criticalLock.unlock();
    }

    public static void forLoop(int start, LoopCondition condition, LoopStep step, IntConsumer region) {
        ParallelContext context = Control.getContext();
        if (context == null) {
            throw new LMP.OutsideParallel();
        }
        if (condition == null) {
            throw new NullPointerException("Loop condition is null");
        }
        if (step == null) {
            throw new NullPointerException("Loop step is null");
        }
        if (region == null) {
            throw new NullPointerException("Region is null");
        }

        LoopContext loopContext = context.getLoopContext();
        loopContext.init(start,condition,step,region);
        loopContext.run();
        loopContext.finish();
    }

    public static void forLoop(final int start, final int exclusiveStop, IntConsumer region) {
        ParallelContext context = Control.getContext();
        if (context == null) {
            throw new LMP.OutsideParallel();
        }
        if (region == null) {
            throw new NullPointerException("Region is null");
        }
        LoopCondition condition;
        LoopStep step;
        if(start < exclusiveStop) {
            condition = i -> i < exclusiveStop;
            step = i -> i++;
        } else  {
            condition = i -> i > exclusiveStop;
            step = i -> i--;
        }
        LoopContext loopContext = context.getLoopContext();
        loopContext.init(start,condition,step,region);
        loopContext.run();
        loopContext.finish();
    }

    public static boolean inParallel(){
        ParallelContext context = Control.getContext();
        return context != null;
    }

    public static class NullRegion extends RuntimeException {
    }

    public static class OutsideParallel extends RuntimeException {
    }

    public static class ParallelException extends RuntimeException {
        private final Map<Thread, Throwable> causes;

        public ParallelException(String message, Map<Thread,Throwable> causes) {
            super(message);
            this.causes = causes;
        }

        public Map<Thread, Throwable> getCauses() {
            return causes;
        }
    }
}
