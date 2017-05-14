package lmp;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.function.IntConsumer;

class LoopContext {

    private static class LoopTask {
        public final int value;
        public final IntConsumer task;

        LoopTask(int value, IntConsumer task) {
            this.value = value;
            this.task = task;
        }
    }

    private boolean initialized;
    private ConcurrentLinkedQueue<LoopTask> tasks;
    private CountDownLatch latch;

    LoopContext() {
        initialized = false;
        tasks = new ConcurrentLinkedQueue<>();
    }

    public synchronized void init(int start, LMP.LoopCondition condition, LMP.LoopStep step, IntConsumer region) {
        if(!initialized) {
            for(int i = start; condition.checkCondition(i); i = step.step(i)) {
                tasks.add(new LoopTask(i,region));
            }
            latch = new CountDownLatch(LMP.getThreadCount());
            initialized = true;
        }
    }

    public void run() {
        LoopTask task;
        while((task = tasks.poll()) != null) {
            task.task.accept(task.value);
        }
        latch.countDown();
    }

    public void finish() {
        try {
            latch.await();
            initialized = false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
