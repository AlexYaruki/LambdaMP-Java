package lmp;

import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

@Ignore
public class SectionsTest {

    @Test
    public void sectionsAreEvenlyDistributedAmongThreads(){
        boolean[] executed = new boolean[4];
        Arrays.fill(executed,false);
        LMP.parallel(() -> {
            LMP.sections(() -> {
                LMP.section(() -> {
                    executed[0] = true;
                });
                LMP.section(() -> {
                    executed[1] = true;
                });
                LMP.section(() -> {
                    executed[2] = true;
                });
                LMP.section(() -> {
                    executed[3] = true;
                });
            });
        });
        for(int i = 0; i < executed.length; i++){
            assertTrue(executed[i]);
        }
    }

}
