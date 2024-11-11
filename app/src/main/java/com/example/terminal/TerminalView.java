package com.example.terminal;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Scroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;

public class TerminalView extends View {
    private static final float LINE_SPACING = 0.0f;
    private Paint paint;
    private Scroller scroller;

    private String outputBuffer = "";

    public int windowWidth;
    public int windowHeight;

    private int cursorRow = 0;
    private int cursorCol = 0;

    private int maxRows;
    private int maxColumns;

    private boolean isFirstDraw = true;

    public float characterWidth;
    public float characterHeight;

    public TerminalView(Context context) {
        super(context);
        init();
    }

    public TerminalView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TerminalView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
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

    public void appendText(String s) {
        outputBuffer += s;
        Log.d("DebugTag", "length of text: " + String.valueOf(s.substring(s.lastIndexOf('\n')).length()));
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
        int lineRow = 1;

        // TODO: i guess this could be done better
        for (String line: outputBuffer.split("\n")) {
            // break line into equalStrings of size "maxColumns"
            for (String equalStr : line.split("(?<=\\G.{" + maxColumns + "})")) {
                canvas.drawText(equalStr, 0.0f, lineRow*characterHeight, paint);
                lineRow += 1;
            }
        }

        // draw cursor
        canvas.drawRect(characterWidth*cursorCol, characterHeight*cursorRow,
                characterWidth+characterWidth*cursorCol, characterHeight+characterHeight*cursorRow, paint);
        isFirstDraw = false;
    }
}
