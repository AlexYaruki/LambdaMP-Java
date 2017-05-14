package lmp;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class ForLoopTest {

    @Test(expected = LMP.OutsideParallel.class)
    public void cannotExecuteOutsideParallel() {
        LMP.forLoop(0,i -> i < 10, i -> i+1, (i) -> {

        });
    }

    @Test
    public void allIterationsExecuted() {
        List<Integer> results = Collections.synchronizedList(new ArrayList<>());
        LMP.parallel(() -> {
            LMP.forLoop(0,i -> i < 10, i -> i+1, (i) -> {
                results.add(i);
            });
        });
        for(int i  = 0; i < 10; i++) {
            assertTrue(results.contains(i));
        }
    }

}
