package com.ethanjhowell.friendly.proxy;

import android.util.Log;

public class BackgroundManager {
    private static final String TAG = BackgroundManager.class.getCanonicalName();
    private boolean allPassed = true;
    private int threadCounter;
    private final Runnable callback;
    private final BackgroundTasks[] tasks;

    public BackgroundManager(Runnable callback, BackgroundTasks... tasks) {
        this.callback = callback;
        threadCounter = tasks.length;
        this.tasks = tasks;
    }

    private void countDown() {
        threadCounter--;
        if (threadCounter <= 0) {
            if (allPassed) {
                callback.run();
            } else {
                Log.w(TAG, "countDown: A background task didn't pass");
            }
        }
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
