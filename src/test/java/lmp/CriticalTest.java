package lmp;

import org.junit.After;
import org.junit.Test;

public class CriticalTest {

    @After
    public void cleanup(){
        LMP.setThreadCount(LMP.getDefaultThreadCount());
        LMP.setExceptionModel(LMP.ExceptionModel.DEFAULT);
    }


    @Test(expected = LMP.OutsideParallel.class)
    public void shouldThrowExceptionWhenOutsideParallel(){
        LMP.critical(() -> {});
    }

}
