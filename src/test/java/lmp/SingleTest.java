package lmp;

import lmp.LMP;
import org.junit.Test;
import static org.junit.Assert.*;


import java.util.concurrent.ConcurrentLinkedDeque;

public class SingleTest {

    @Test(expected = LMP.OutsideParallel.class)
    public void shouldThrowNullRegionWhenOutsideParallel() {
        LMP.single(() -> {});
    }

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
