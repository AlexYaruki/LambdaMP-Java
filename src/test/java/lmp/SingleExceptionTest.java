package lmp;

import org.junit.Test;

public class SingleExceptionTest extends LMPBaseTest {

    @Test(expected = RuntimeException.class)
    public void shouldPropagateExceptionInPropagateMode() {
        LMP.setExceptionModel(LMP.ExceptionModel.PROPAGATE);
        LMP.parallel(() -> {
            LMP.single(() -> {
                throw new RuntimeException();
            });
        });
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
    }


}
