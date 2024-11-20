package com.example.terminal;

import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

public class GestureListener extends GestureDetector.SimpleOnGestureListener {
    @Override
    public boolean onDown(@NonNull MotionEvent e) {
        return true;
    }
}
