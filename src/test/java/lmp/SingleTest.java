package lmp;

import org.junit.Test;

import java.util.concurrent.ConcurrentLinkedDeque;

import static org.junit.Assert.*;

public class SingleTest {

    @Test
    public void oneThreadExecutedSingleRegion() {
        ConcurrentLinkedDeque<Integer> resultsSingle = new ConcurrentLinkedDeque<>();
        ConcurrentLinkedDeque<Integer> resultsGlobal = new ConcurrentLinkedDeque<>();
        LMP.setThreadCount(4);
        LMP.parallel(() -> {
            resultsGlobal.add(Thread.currentThread().hashCode());
            LMP.single(() -> {
                resultsSingle.add(Thread.currentThread().hashCode());
            });
        });
        assertEquals(1, resultsSingle.size());
        assertEquals(4, resultsGlobal.size());
    }

}
