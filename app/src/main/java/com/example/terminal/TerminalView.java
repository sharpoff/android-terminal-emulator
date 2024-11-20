package com.example.terminal;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.OverScroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TerminalView extends View {
    private static final float LINE_SPACING = 0.0f;
    private Paint paint;
    private GestureDetector gestureDetector;
    private OverScroller scroller;
    private TerminalEmulator terminal;

    private String outputBuffer = "";

    public int windowWidth;
    public int windowHeight;

    private int cursorRow = 0;
    private int cursorCol = 0;

    private int rows = 0;

    private int scrolled = 1;

    private int maxRows;
    private int maxColumns;

    public float characterWidth;
    public float characterHeight;

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        windowWidth = getWidth();
        windowHeight = getHeight();
        maxRows = (int)(windowHeight / characterHeight);
        maxColumns = (int)(windowWidth / characterWidth);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        // draw output
        int lineRow = scrolled;

        // TODO: i guess this could be done better
        for (String line: outputBuffer.split("\n")) {
            // break line into equalStrings of size "maxColumns"
            for (String equalStr : line.split("(?<=\\G.{" + maxColumns + "})")) {
                canvas.drawText(equalStr, 0.0f, lineRow*characterHeight, paint);
                lineRow += 1;
            }
        }
        rows = lineRow;
        Log.d("DebugTag", String.valueOf(rows));

        // draw cursor
        canvas.drawRect(characterWidth*cursorCol, characterHeight*cursorRow,
                characterWidth+characterWidth*cursorCol, characterHeight+characterHeight*cursorRow, paint);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.inputType = InputType.TYPE_CLASS_TEXT;
        outAttrs.imeOptions = EditorInfo.IME_FLAG_NO_FULLSCREEN;

        return new BaseInputConnection(this, true) {
            @Override
            public boolean finishComposingText() {
                return super.finishComposingText();
            }
        };
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        windowWidth = w;
        windowHeight = h;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTypeface(Typeface.MONOSPACE);
        paint.setAntiAlias(true);
        paint.setTextSize(32);
        paint.setColor(getContext().getColor(R.color.white));

        characterHeight = (int)Math.ceil(paint.getFontSpacing());
        characterWidth = paint.measureText("@", 0, 1);
    }

    public TerminalView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();

        gestureDetector = new GestureDetector(getContext(), new GestureListener() {
            @Override
            public boolean onDown(@NonNull MotionEvent e) {
                scroller.forceFinished(true);
                Log.d("DebugTag", "DOO SMTHING");
                return true;
            }

//            @Override
//            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//                double SCALE = 0.25;
//                scroller.fling(0, 0, 0, -(int) (velocityY * SCALE),0, 0, 100, 0);
//                postInvalidate();
//
//                return true;
//            }

            @Override
            public boolean onScroll(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
                Log.d("DebugTag", "DOO SCROOOOLL");
                int deltaRows = (int) (distanceY / characterHeight);
                scrollViewVertically(deltaRows);
                return true;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        gestureDetector.onTouchEvent(event); // crash on tihs
        return true;
    }

    private void scrollViewVertically(int rows) {
        boolean isUp = rows < 0;
        scrolled = Math.min(0, Math.max(-2000, scrolled + (isUp ? -1 : 1)));
        invalidate();
    }

    public void appendText(String s) {
        outputBuffer += s;
        invalidate();
    }

    public void moveCursorLeft() {
        if (cursorCol > 0) {
            cursorCol -= 1;
            invalidate();
        }
    }

    public void moveCursorRight() {
        if (cursorCol < maxColumns) {
            cursorCol += 1;
            invalidate();
        }
    }

    public void moveCursorUp() {
        if (cursorRow > 0)
            cursorRow -= 1;
        invalidate();
    }

    public void moveCursorDown() {
        if (cursorRow < maxRows) {
            cursorRow += 1;
            invalidate();
        }
    }
}
