package lmp;

class ParallelRunner implements Runnable {

    private Runnable parallelRegion;
    private ParallelContext context;


    public ParallelRunner(Runnable parallelRegion,ParallelContext context){
        this.parallelRegion = parallelRegion;
        this.context = context;
    }

    @Override
    public void run() {
        context.liftStartupLatch();
        parallelRegion.run();
    }
}