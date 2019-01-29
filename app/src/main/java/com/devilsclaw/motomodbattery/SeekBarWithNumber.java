package com.devilsclaw.motomodbattery;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.widget.SeekBar;

public class SeekBarWithNumber extends AppCompatSeekBar {
    public SeekBarWithNumber(Context context) {
        super(context);
    }

    public SeekBarWithNumber(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SeekBarWithNumber(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int thumb_x = (int) ((double)this.getWidth() / ((double)2)) - 40;
        float middle = (float) (this.getHeight() - 76);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(64);
        canvas.drawText(String.format("%d%%",this.getProgress()), thumb_x, middle, paint);
    }
}
