package lmp;

import org.junit.After;
import org.junit.BeforeClass;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LMPBaseTest {

    @BeforeClass
    public static void initGlobal() {
        Logger.getLogger("LMP").setLevel(Level.FINEST);
    }

    @After
    public void cleanup(){
        LMP.setThreadCount(LMP.getDefaultThreadCount());
        LMP.setExceptionModel(LMP.ExceptionModel.DEFAULT);
        LMP.setExceptionHandler(null);
    }

}
