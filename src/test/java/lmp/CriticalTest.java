package lmp;

import org.junit.Test;

public class CriticalTest {

    @Test(expected = LMP.OutsideParallel.class)
    public void shouldThrowExceptionWhenOutsideParallel(){
        LMP.critical(() -> {});
    }


}
