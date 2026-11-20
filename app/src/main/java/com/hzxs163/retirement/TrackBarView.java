package com.hzxs163.retirement;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/** 带圆点的进度条：底槽 + 已完成段 + 端点圆点。 */
public class TrackBarView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private float ratio = 0f;
    private int fillColor = 0xFFF26B21;
    private int trackColor = 0xFFE8E2D8;
    private float barHeight = 0f;
    private boolean showKnob = true;

    public TrackBarView(Context context) {
        super(context);
    }

    public TrackBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setRatio(float value) {
        this.ratio = Math.max(0f, Math.min(1f, value));
        invalidate();
    }

    public void setColors(int fill, int track) {
        this.fillColor = fill;
        this.trackColor = track;
        invalidate();
    }

    public void setBarHeight(float px) {
        this.barHeight = px;
        invalidate();
    }

    public void setShowKnob(boolean show) {
        this.showKnob = show;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        float bar = barHeight > 0 ? barHeight : Math.min(h, dp(6));
        float cy = h / 2f;
        float radius = bar / 2f;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(trackColor);
        rect.set(0f, cy - radius, w, cy + radius);
        canvas.drawRoundRect(rect, radius, radius, paint);

        float filled = w * ratio;
        if (filled > 0.5f) {
            paint.setColor(fillColor);
            rect.set(0f, cy - radius, Math.max(filled, bar), cy + radius);
            canvas.drawRoundRect(rect, radius, radius, paint);
        }

        if (showKnob) {
            float knobRadius = Math.max(bar, dp(9));
            float kx = Math.min(Math.max(filled, knobRadius), w - knobRadius);
            paint.setColor(0xFFFFFFFF);
            canvas.drawCircle(kx, cy, knobRadius, paint);
            paint.setColor(fillColor);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(3));
            canvas.drawCircle(kx, cy, knobRadius - dp(1.5f), paint);
            paint.setStyle(Paint.Style.FILL);
        }
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }
}
