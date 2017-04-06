package lmp;

import org.junit.Test;

import java.util.concurrent.ConcurrentLinkedDeque;

import static org.junit.Assert.assertEquals;

public class SingleTest extends LMPBaseTest{

    @Test(expected = LMP.OutsideParallel.class)
    public void shouldThrowOutsideParallelWhenOutsideParallel() {
        LMP.single(() -> {});
    }

    @Test(expected = LMP.OutsideParallel.class)
    public void shouldThrowOutsideParallelWhenNullRegionOutsideParallel(){
        LMP.single(null);
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
