package lmp;

@FunctionalInterface
public interface ExceptionHandler {
    void handleException(Thread thread, ThreadContextView threadContextView, Throwable throwable);
}
