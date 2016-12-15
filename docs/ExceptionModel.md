Ideas for exception model in LambdaMP-Java:

1. By default, exception should be handled in same region that throws it using standard Java try-catch-finally block
2. If exception is not handled in corresponding region, one of the options could be available available:
    1. DEFAULT - Exception is rethrown immediately in implementation, so it could interfere others threads execution and result in program shutdown (?)
    2. PROPAGATE - Exception is saved in implementation and propagated outside parallel region. Thread that thrown exception will wait for other threads in parallel region to complete.
        Other threads where exception was not thrown, execute as planned. When parallel region returns "LMP.parallel()",
        If only one thread throws an exception, it is rethrown as it was thrown by "LMP.parallel", for example:
        ```java
               try {
                   LMP.parallel(() -> {
                       if(LMP.getThreadId() == 0) {
                           throw new RuntimeException();
                       }
                   });
               } catch (RuntimeException re) {
                   re.printStackTrace();
               }
        ```
        If other threads also are throwing exceptions, all of them are saved and packaged into LMP.MultiException
        ```java
               LMP.setThreadCount(4);
               try {
                   LMP.parallel(() -> {
                           throw new RuntimeException();
                   });
               } catch (MultiException me) {
                   for(Throwable t : me.getThrowables()) {
                       t.printStackTrace();
                   }
               }
        ```
                        
    3.  HANDLE_IMMEDIATELY - Exception is handled immediately by implementation using provided handler. Thread stops after that.   
    4.  IDEA: THREAD_DROP - Threads where exception was not thrown, are immediately interrupted.