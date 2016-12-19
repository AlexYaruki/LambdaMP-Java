package lmp;

import org.junit.After;

public class LMPBaseTest {

    @After
    public void cleanup(){
        LMP.setThreadCount(LMP.getDefaultThreadCount());
        LMP.setExceptionModel(LMP.ExceptionModel.DEFAULT);
        LMP.setExceptionHandler(null);
    }

}
