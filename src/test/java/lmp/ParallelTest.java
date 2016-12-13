package lmp;


import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.assertEquals;


public class ParallelTest {

    @Test(expected = LMP.NullRegion.class)
    public void nullParallelRegion_throwsException(){
        LMP.parallel(null);
    }

    @Test
    public void threadCountChangeAffectExecution(){
        int threadCount = LMP.getThreadCount();
        Set<Thread> threads = new HashSet<>();
        Lock lock = new ReentrantLock();
        LMP.parallel(() -> {
            lock.lock();
            threads.add(Thread.currentThread());
            lock.unlock();
        });
        assertEquals(threadCount,threads.size());

        threads.clear();
        int changedThreadCount = threadCount*2;

        LMP.setThreadCount(changedThreadCount);
        LMP.parallel(() -> {
            lock.lock();
            threads.add(Thread.currentThread());
            lock.unlock();
        });
        assertEquals(changedThreadCount,threads.size());

    }



    @Test
    public void workExecutedBySeparateThreads(){
        Set<Integer> hashes = new HashSet<>();
        Lock lock = new ReentrantLock();
        int threadCount = LMP.getDefaultThreadCount();
        LMP.parallel(() -> {
            lock.lock();
            hashes.add(Thread.currentThread().hashCode());
            lock.unlock();
        });
        assertEquals(threadCount,hashes.size());
    }

    @Test
    public void allThreadsFinishAfterRegion(){
        Set<Thread> hashes = new HashSet<>();
        Lock lock = new ReentrantLock();
        LMP.parallel(() -> {
            lock.lock();
            hashes.add(Thread.currentThread());
            lock.unlock();
        });
        assertEquals(true, hashes.stream().allMatch((t) -> !t.isAlive()));
    }

}
