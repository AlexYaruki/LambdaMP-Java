package lmp.parallel;

import lmp.LMP;
import lmp.LMPBaseTest;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;


public class ParallelModesTest extends LMPBaseTest {

    @Test
    public void shouldRunInParallelWhenIfClauseNotPresent() {
        Map<Thread,UUID> map = Collections.synchronizedMap(new HashMap<>());
        LMP.setThreadCount(2);
        LMP.parallel(() -> map.put(Thread.currentThread(),UUID.randomUUID()));
        assertEquals(2,map.size());
    }

    @Test
    public void shouldRunConcurrentlyWhenInConcurrentMode() {
        Map<Thread,UUID> map = Collections.synchronizedMap(new HashMap<>());
        LMP.setThreadCount(2);
        LMP.parallel(LMP.ParallelMode.Concurrent,() -> map.put(Thread.currentThread(),UUID.randomUUID()));
        assertEquals(2,map.size());

    }

    @Test
    public void shouldRunSeriallyWhenInSerialMode() {
        Map<Thread,UUID> map = Collections.synchronizedMap(new HashMap<>());
        LMP.setThreadCount(2);
        LMP.parallel(LMP.ParallelMode.Serial,() -> map.put(Thread.currentThread(),UUID.randomUUID()));
        assertEquals(1,map.size());

    }

}
