package lmp;

import org.junit.After;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class ParallelCheckTest {

    @After
    public void clean(){
        LMP.setThreadCount(LMP.getDefaultThreadCount());
    }

    @Test
    public void shouldReturnFalseWhenOutsideParallel(){
        assertFalse(LMP.inParallel());
    }

    @Test
    public void shouldReturnTrueWhenInsideParallel(){
        AtomicBoolean check = new AtomicBoolean(false);
        LMP.setThreadCount(1);
        LMP.parallel(() -> {
            check.set(LMP.inParallel());
        });
        assertTrue(check.get());

    }

}
