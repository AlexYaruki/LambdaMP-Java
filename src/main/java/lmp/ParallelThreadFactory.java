package lmp;

import java.util.concurrent.ThreadFactory;

class ParallelThreadFactory implements ThreadFactory {

    private ParallelContext context;

    public ParallelThreadFactory(ParallelContext context){
        this.context = context;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        LMPControl.mapContext(thread,context);
        context.addThread(thread);
        return thread;
    }
}