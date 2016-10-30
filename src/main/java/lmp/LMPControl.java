package lmp;

import java.util.HashMap;
import java.util.Map;

class LMPControl {

    private static final int DEFAULT_THREAD_COUNT;
    private static int threadCount;
    private static Map<Thread,ParallelContext> contexts;
    static {
        DEFAULT_THREAD_COUNT = Runtime.getRuntime().availableProcessors();
        threadCount = DEFAULT_THREAD_COUNT;
        contexts = new HashMap<>();
    }

    public static int getThreadCount() {
        return threadCount;
    }

    public static void setThreadCount(int threadCount) {
        LMPControl.threadCount = threadCount;
        if(threadCount < 1){
            LMPControl.threadCount = DEFAULT_THREAD_COUNT;
        }
    }

    static ParallelContext getContext(){
        return contexts.get(Thread.currentThread());
    }

    public static void removeContextByThread(Thread thread) {
        contexts.remove(thread);
    }

    public static void mapContext(Thread thread, ParallelContext context) {
        contexts.put(thread,context);
    }
}
