package com.ethanjhowell.friendly;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public abstract class MessageGestureListener implements View.OnTouchListener {
    private static final String TAG = MessageGestureListener.class.getCanonicalName();
    private GestureDetector gestureDetector;

    public MessageGestureListener(Context c) {
        gestureDetector = new GestureDetector(c, new GestureListener());
    }

    public boolean onTouch(final View view, final MotionEvent motionEvent) {
        view.performClick();
        return gestureDetector.onTouchEvent(motionEvent);
    }

    public abstract void onDoubleTap(MotionEvent e);

    public abstract void onLongPress(MotionEvent e);

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public void onLongPress(MotionEvent e) {
            MessageGestureListener.this.onLongPress(e);
            super.onLongPress(e);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            super.onDoubleTap(e);
            MessageGestureListener.this.onDoubleTap(e);
            return true;
        }
    }
}