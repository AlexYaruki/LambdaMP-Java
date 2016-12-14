package lmp;


import org.junit.After;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.assertEquals;


public class ParallelTest {

    /*
        Thread count is set to default value at the end of each test to simulate unmodified environment
     */
    @After
    public void init(){
        int defaultThreadCount = LMP.getDefaultThreadCount();
        LMP.setThreadCount(defaultThreadCount);
    }

    @Test(expected = LMP.NullRegion.class)
    public void nullParallelRegion_throwsException(){
        LMP.parallel(null);
    }

    @Test
    public void regionExecutedDefaultAmountOfTimes() {
        List<Integer> checkValues = Collections.synchronizedList(new ArrayList<>());
        int defaultThreadCount = LMP.getDefaultThreadCount();
        LMP.parallel(() -> {
            checkValues.add(1);
        });
        assertEquals(defaultThreadCount,checkValues.size());
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
    public void threadCountChangePreservedBetweenRegions(){
        List<Integer> checks = Collections.synchronizedList(new ArrayList<>());
        final int defaultThreadCount = LMP.getDefaultThreadCount();
        final int changedThreadCount = defaultThreadCount + 1;
        LMP.setThreadCount(changedThreadCount);
        LMP.parallel(() -> {
            checks.add(1);
        });
        assertEquals(changedThreadCount,checks.size());
        LMP.parallel(() -> {
            checks.add(1);
        });
        assertEquals(changedThreadCount*2,checks.size());
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
