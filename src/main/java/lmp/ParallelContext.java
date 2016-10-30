package lmp;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

class ParallelContext {

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
            LMPControl.removeContextByThread(thread);
        }
        threads.clear();
    }
}