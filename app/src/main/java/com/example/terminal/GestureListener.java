package com.example.terminal;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

class GestureListener extends GestureDetector.SimpleOnGestureListener {
    @Override
    public boolean onDown(@NonNull MotionEvent e) {
        // gestures doesn't work without that
        return true;
    }

    @Override
    public boolean onSingleTapUp(@NonNull MotionEvent e) {
        Log.d("TouchTag", "TOUCH PLEAAAASE!!!!");
        return false;
    }

    @Override
    public boolean onScroll(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
        Log.d("TouchTag", "SCROLL PLEAAAASE!!!!");
        return true;
    }
}
