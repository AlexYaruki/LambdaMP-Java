package lmp;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class CriticalTest extends LMPBaseTest{

    @Test(expected = LMP.OutsideParallel.class)
    public void shouldThrowExceptionWhenOutsideParallel(){
        LMP.critical(() -> {});
    }

}
