package com.muse.tasbih;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

/** رسم بياني بسيط: أعمدة ذهبية لآخر 7 أيام. */
public class BarChartView extends View {

    private int[] values = new int[0];
    private String[] labels = new String[0];
    private int barColor = 0xFFFFD54F;
    private int textColor = 0xFFFFFFFF;

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public BarChartView(Context context) {
        super(context);
        valuePaint.setTextAlign(Paint.Align.CENTER);
        valuePaint.setFakeBoldText(true);
        labelPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(int[] values, String[] labels, int barColor, int textColor) {
        this.values = values;
        this.labels = labels;
        this.barColor = barColor;
        this.textColor = textColor;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (values.length == 0) return;
        float w = getWidth();
        float h = getHeight();
        float labelH = h * 0.16f;
        float valueH = h * 0.16f;
        float chartH = h - labelH - valueH;
        int max = 1;
        for (int v : values) if (v > max) max = v;
        float slot = w / values.length;
        float barW = slot * 0.52f;
        barPaint.setColor(barColor);
        valuePaint.setColor(textColor);
        valuePaint.setTextSize(h * 0.085f);
        labelPaint.setColor(textColor);
        labelPaint.setTextSize(h * 0.085f);
        float baseline = valueH + chartH;
        for (int i = 0; i < values.length; i++) {
            float cx = slot * i + slot / 2f;
            float bh = chartH * ((float) values[i] / (float) max);
            if (values[i] > 0 && bh < 4) bh = 4;
            canvas.drawRect(cx - barW / 2f, baseline - bh, cx + barW / 2f, baseline, barPaint);
            // القيمة فوق العمود مباشرة
            canvas.drawText(String.valueOf(values[i]), cx, baseline - bh - 8, valuePaint);
            if (i < labels.length) {
                canvas.drawText(labels[i], cx, h - 4, labelPaint);
            }
        }
    }
}
