package com.hzxs163.retirement;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/** 环形占比图：底圈 + 已完成弧。用于大环和卡片上的小环。 */
public class DonutView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private float usedRatio = 0f;
    private int usedColor = 0xFFF26B21;
    private int restColor = 0xFFE8E2D8;
    private float strokeWidth = 0f;

    public DonutView(Context context) {
        super(context);
    }

    public DonutView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setUsedRatio(float ratio) {
        this.usedRatio = Math.max(0f, Math.min(1f, ratio));
        invalidate();
    }

    public void setColors(int used, int rest) {
        this.usedColor = used;
        this.restColor = rest;
        invalidate();
    }

    public void setRingWidth(float px) {
        this.strokeWidth = px;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        float stroke = strokeWidth > 0 ? strokeWidth : Math.min(w, h) * 0.12f;
        float pad = stroke / 2f + 1f;
        rect.set(pad, pad, w - pad, h - pad);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(stroke);
        paint.setStrokeCap(Paint.Cap.ROUND);

        paint.setColor(restColor);
        canvas.drawArc(rect, -90f, 360f, false, paint);

        if (usedRatio > 0f) {
            paint.setColor(usedColor);
            canvas.drawArc(rect, -90f, 360f * usedRatio, false, paint);
        }
    }
}
