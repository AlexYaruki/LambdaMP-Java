package lmp.exceptions;

import lmp.LMP;
import lmp.LMPBaseTest;
import org.junit.Test;

import java.util.Map;
import java.util.Optional;

import static junit.framework.TestCase.*;

public class SingleExceptionTest extends LMPBaseTest {

    @Test
    public void shouldPropagateExceptionInPropagateMode() {
        LMP.setExceptionModel(LMP.ExceptionModel.PROPAGATE);
        LMP.ParallelException savedException = null;
        try {
            LMP.parallel(() -> {
                LMP.single(() -> {
                    throw new RuntimeException();
                });
            });
        } catch (LMP.ParallelException exception) {
            savedException = exception;
        }
        assertNotNull(savedException);
        Map<Thread, Throwable> causes = savedException.getCauses();
        assertTrue(causes.size() == 1);
        Map.Entry<Thread, Throwable> entry = causes.entrySet().stream().findFirst().get();
        assertTrue(entry.getValue() instanceof RuntimeException);
    }

    //TODO: Only one exception should be trowed
    @Test
    public void shouldPropagateNullRegionExceptionInPropagateMode() {
        LMP.setThreadCount(1);
        LMP.setExceptionModel(LMP.ExceptionModel.PROPAGATE);
        boolean checkExceptionCount = false;
        boolean checkExceptionType = false;
        try {
            LMP.parallel(() -> {
                LMP.single(null);
            });
        } catch (LMP.ParallelException pe){
            checkExceptionCount = pe.getCauses().size() == 1;
            checkExceptionType = pe.getCauses().values().stream().findFirst().get().getClass() == LMP.NullRegion.class;
        }
        assertTrue(checkExceptionCount);
        assertTrue(checkExceptionType);
    }


}
