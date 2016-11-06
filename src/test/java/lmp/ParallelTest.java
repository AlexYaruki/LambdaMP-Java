package lmp;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.assertEquals;

public class ParallelTest {

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
