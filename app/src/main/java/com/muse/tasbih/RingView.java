package com.muse.tasbih;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

/** دائرة تقدم مرسومة: حلقة خلفية وقوس ذهبي ورقم العدّاد في المنتصف. */
public class RingView extends View {

    private int count = 0;
    private int target = 100;
    private int trackColor = 0x33FFFFFF;
    private int progressColor = 0xFFFFD54F;
    private int textColor = Color.WHITE;

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF oval = new RectF();

    public RingView(Context context) {
        super(context);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
    }

    public void setData(int count, int target) {
        this.count = count;
        this.target = target;
        invalidate();
    }

    public void setColors(int track, int progress, int text) {
        this.trackColor = track;
        this.progressColor = progress;
        this.textColor = text;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        float stroke = w * 0.055f;
        trackPaint.setStrokeWidth(stroke);
        trackPaint.setColor(trackColor);
        progressPaint.setStrokeWidth(stroke);
        progressPaint.setColor(progressColor);
        float pad = stroke / 2f + 4;
        oval.set(pad, pad, w - pad, h - pad);
        canvas.drawArc(oval, 0, 360, false, trackPaint);
        float frac = target > 0 ? Math.min(1f, (float) count / (float) target) : 0f;
        if (frac > 0) {
            canvas.drawArc(oval, -90, 360f * frac, false, progressPaint);
        }
        textPaint.setColor(textColor);
        textPaint.setTextSize(w * 0.30f);
        String s = String.valueOf(count);
        float ty = h / 2f - (textPaint.descent() + textPaint.ascent()) / 2f;
        canvas.drawText(s, w / 2f, ty, textPaint);
    }
}
