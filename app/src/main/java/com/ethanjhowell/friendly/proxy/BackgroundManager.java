package com.ethanjhowell.friendly.proxy;

public class BackgroundManager {
    private boolean allPassed = true;
    private int threadCounter;
    private Runnable callback;
    private BackgroundTasks[] tasks;

    public BackgroundManager(Runnable callback, BackgroundTasks... tasks) {
        this.callback = callback;
        threadCounter = tasks.length;
        this.tasks = tasks;
    }

    private void countDown() {
        threadCounter--;
        if (threadCounter <= 0)
            callback.run();
    }

    public void failed(Exception e) {
        allPassed = false;
        countDown();
    }

    public void succeeded() {
        countDown();
    }

    public void run() {
        for (BackgroundTasks task : tasks) {
            task.run(this);
        }
    }
}
