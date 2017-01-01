package lmp.exceptions;

import lmp.ExceptionHandler;
import lmp.LMP;
import lmp.LMPBaseTest;
import lmp.ThreadContextView;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class ParallelExceptionTest extends LMPBaseTest {

    @Test(expected = LMP.NullRegion.class)
    public void nullParallelRegion_throwsException(){
        LMP.parallel(null);
    }

    @Test
    public void shouldSetExceptionModel(){
        LMP.setExceptionModel(LMP.ExceptionModel.HANDLE);
        assertEquals(LMP.ExceptionModel.HANDLE,LMP.getExceptionModel());
    }

    @Test
    public void shouldSetExceptionHandler() {
        ExceptionHandler exceptionHandler = (t, tcv, ex) -> {};
        LMP.setExceptionHandler(exceptionHandler);
        assertEquals(exceptionHandler,LMP.getExceptionHandler());
    }

    @Test
    public void shouldUseExceptionHandlerInHandlerMode() {
        LMP.setThreadCount(1);
        LMP.setExceptionModel(LMP.ExceptionModel.HANDLE);
        AtomicBoolean check = new AtomicBoolean(false);
        LMP.setExceptionHandler((Thread thread, ThreadContextView threadContextView, Throwable throwable) -> {
            check.set(true);
        });
        LMP.parallel(() -> {
            throw new RuntimeException();
        });
        assertTrue(check.get());
    }

    @Test
    public void shouldNotUseExceptionHandlerInDefaultMode() {
        LMP.setThreadCount(1);
        LMP.setExceptionModel(LMP.ExceptionModel.DEFAULT);
        AtomicBoolean check = new AtomicBoolean(false);
        LMP.setExceptionHandler((Thread thread, ThreadContextView threadContextView, Throwable throwable) -> {
            check.set(true);
        });
        LMP.parallel(() -> {
            throw new RuntimeException();
        });
        assertFalse(check.get());
    }

    @Test
    public void shouldNotUseExceptionHandlerInPropagateMode() {
        LMP.setThreadCount(1);
        LMP.setExceptionModel(LMP.ExceptionModel.PROPAGATE);
        AtomicBoolean check = new AtomicBoolean(false);
        LMP.setExceptionHandler((Thread thread, ThreadContextView threadContextView, Throwable throwable) -> {
            check.set(true);
        });
        try {
            LMP.parallel(() -> {
                throw new RuntimeException();
            });
        } catch (Throwable e) {

        }
        assertFalse(check.get());
    }

    @Test(expected = LMP.ParallelException.class)
    public void shouldPropagateExceptionHandlerInPropagateMode() {
        LMP.setThreadCount(1);
        LMP.setExceptionModel(LMP.ExceptionModel.PROPAGATE);
        LMP.parallel(() -> {
            throw new RuntimeException();
        });
    }

    @Test
    public void whenExceptionThrownSafeThreadsExecuteAsNormal() {
        LMP.setThreadCount(4);
        int[] checks = {-1,-1,-1,-1};
        LMP.parallel(() -> {
            if(LMP.getThreadId() == 0) {
                throw new RuntimeException();
            } else {
                checks[LMP.getThreadId()] = LMP.getThreadId();
            }
        });
        final int[] expected = {-1,1,2,3};
        assertArrayEquals(expected,checks);
    }
}
