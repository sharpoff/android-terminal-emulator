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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;

public class TerminalView extends View {
    private static final int OUTPUT_BUFFER_SIZE = 5000;
    private Paint paint;
    private GestureDetector gestureDetector;

    public String outputBuffer[] = new String[OUTPUT_BUFFER_SIZE];

    public int windowWidth;
    public int windowHeight;

    private int cursorRow = 0;
    private int cursorCol = 0;

    private int rows = 0;

    private int scrolled = 1;
    private float scrollRemainder = 0.0f;

    private int maxRows = 49;
    private int maxColumns = 56;

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
        for (String line: outputBuffer) {
            canvas.drawText(line == null ? "" : line, 0.0f, lineRow*characterHeight, paint);
            lineRow += 1;
        }
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

        gestureDetector = new GestureDetector(getContext(), new GestureListener() {
            @Override
            public boolean onScroll(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
                distanceY += scrollRemainder;
                int scrollCount = (int)(distanceY / characterHeight);
                scrollRemainder = distanceY - scrollCount * characterHeight;
                scroll(scrollCount);
                return true;
            }

            @Override
            public boolean onSingleTapUp(@NonNull MotionEvent e) {
                // TODO: show up keyboard
                return true;
            }
        });
    }

    public void scroll(int rowsCount) {
        rowsCount = -rowsCount;
        Log.d("TouchTag", "how much to scroll: " + rowsCount);
        boolean bottom = rowsCount < 0;
        if (bottom) {
            scrolled += rowsCount;
        } else {
            if ((scrolled + rowsCount) <= 1)
                scrolled += rowsCount;
        }
        Log.d("TouchTag", "Scrolled: " + String.valueOf(scrolled));
        Log.d("TouchTag", "rows: " + rows);
        invalidate();
    }

    public TerminalView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    public void appendText(String s) {
        int rowCharCount = 0;
        for (int i = 0; i < s.length(); i++) {
            rowCharCount++;
            if (s.charAt(i) == '\n' || rowCharCount > maxColumns) {
                rows++;
                rowCharCount = 0;
                continue;
            }

            if (outputBuffer[rows] == null) {
                outputBuffer[rows] = String.valueOf(s.charAt(i));
            } else {
                outputBuffer[rows] += String.valueOf(s.charAt(i));
            }
        }
        invalidate();

        // TODO: implement scroll when hit borders of window
//        if (rows > maxRows) {
//            scroll(rows - maxRows);
//            cursorRow = rows;
//        } else {
//            cursorRow = rows;
//        }
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

    public void clearDisplay() {
        outputBuffer = new String[OUTPUT_BUFFER_SIZE];
        cursorRow = 0;
        cursorCol = 0;
        rows = 0;
        scrolled = 1;
    }
}
