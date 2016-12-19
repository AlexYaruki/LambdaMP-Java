package lmp;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ParallelExceptionTest {


    @After
    public void init(){
        int defaultThreadCount = LMP.getDefaultThreadCount();
        LMP.setThreadCount(defaultThreadCount);
        LMP.setExceptionModel(LMP.getExceptionModel().DEFAULT);
    }

    @Test(expected = LMP.NullRegion.class)
    public void nullParallelRegion_throwsException(){
        LMP.parallel(null);
    }


    @Test
    public void shouldSetExceptionModel(){
        LMP.setExceptionModel(LMP.ExceptionModel.DROP);
        assertEquals(LMP.ExceptionModel.DROP,LMP.getExceptionModel());
    }

}
