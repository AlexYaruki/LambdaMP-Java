package lmp.exceptions;

import lmp.LMP;
import lmp.LMPBaseTest;
import org.junit.Test;

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
        assertTrue(savedException.getCause().getClass().equals(RuntimeException.class));
    }

    @Test
    public void shouldPropagateNullRegionExceptionInPropagateMode() {
        LMP.setExceptionModel(LMP.ExceptionModel.PROPAGATE);
        final int threadCount = LMP.getThreadCount();
        boolean checkExceptionCount = false;
        boolean checkExceptionType = false;
        try {
            LMP.parallel(() -> {
                LMP.single(null);
            });
        } catch (LMP.MultiParallelException mpe){
            checkExceptionCount = mpe.getCauses().size() == threadCount;
            checkExceptionType = mpe.getCauses().entrySet().stream().allMatch((e) -> {
                return e.getValue().getClass().equals(LMP.NullRegion.class);
            });
        }
        assertTrue(checkExceptionCount);
        assertTrue(checkExceptionType);
    }


}
