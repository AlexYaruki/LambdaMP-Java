package lmp;

public class ThreadContextView {

    private int threadId;

    ThreadContextView(int threadId){

        this.threadId = threadId;
    }

    public int getThreadId() {
        return threadId;
    }
}
