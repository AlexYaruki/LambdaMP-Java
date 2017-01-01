package lmp;


import org.junit.After;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class ParallelTest extends LMPBaseTest{

    /*
        Thread count is set to default value at the end of each test to simulate unmodified environment
     */
    @After
    public void init(){
        int defaultThreadCount = LMP.getDefaultThreadCount();
        LMP.setThreadCount(defaultThreadCount);
        LMP.setExceptionModel(LMP.getExceptionModel().DEFAULT);
    }

    @Test
    public void shouldReturnThreadId(){
        int threadCount = LMP.getDefaultThreadCount();
        if(threadCount == 1) {
            threadCount = 2;
        }
        LMP.setThreadCount(threadCount);

        Set<Integer> ids = Collections.synchronizedSet(new HashSet<>());
        LMP.parallel(() -> {
            ids.add(LMP.getThreadId());
        });
        for(int i = 0; i < threadCount; i++){
            assertTrue(ids.contains(i));
        }
    }

    @Test
    public void shouldReturnThreadIdZeroWhenOutsideParallel() {
        final int threadId = LMP.getThreadId();
        assertEquals(0,threadId);
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
    public void threadCountChangeVisibleInParallel(){
        final int threadCount = LMP.getThreadCount();
        LMP.setThreadCount(threadCount + 1);
        final int changedThreadCount = LMP.getThreadCount();
        List<Integer> checks = Collections.synchronizedList(new ArrayList<>());
        LMP.parallel(() -> {
            checks.add(LMP.getThreadCount());
        });
        assertEquals(changedThreadCount,checks.size());
        assertTrue(checks.stream().allMatch(val -> val == changedThreadCount));
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
