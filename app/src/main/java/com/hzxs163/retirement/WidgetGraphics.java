package com.hzxs163.retirement;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/** 小组件里不能用自定义 View，环和进度条先画成 Bitmap 再交给 ImageView。 */
public final class WidgetGraphics {

    private WidgetGraphics() {
    }

    public static Bitmap ring(int sizePx, int strokePx, float ratio, int color, int trackColor) {
        int size = Math.max(sizePx, strokePx * 3);
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokePx);
        paint.setStrokeCap(Paint.Cap.ROUND);

        float pad = strokePx / 2f + 1f;
        RectF rect = new RectF(pad, pad, size - pad, size - pad);

        paint.setColor(trackColor);
        canvas.drawArc(rect, -90f, 360f, false, paint);

        float sweep = 360f * Math.max(0f, Math.min(1f, ratio));
        if (sweep > 0.5f) {
            paint.setColor(color);
            canvas.drawArc(rect, -90f, sweep, false, paint);
        }
        return bitmap;
    }

    public static Bitmap bar(int widthPx, int heightPx, float ratio, int fillColor,
                             int trackColor, boolean knob) {
        int width = Math.max(widthPx, heightPx + 2);
        int height = Math.max(heightPx, 6);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        float radius = Math.max(2f, height * 0.14f);
        float cy = height / 2f;
        RectF rect = new RectF(radius, cy - radius, width - radius, cy + radius);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(trackColor);
        canvas.drawRoundRect(rect, radius, radius, paint);

        float span = width - radius * 2f;
        float filled = span * Math.max(0f, Math.min(1f, ratio));
        if (filled > 0.5f) {
            paint.setColor(fillColor);
            rect.set(radius, cy - radius, radius + Math.max(filled, radius * 2f), cy + radius);
            canvas.drawRoundRect(rect, radius, radius, paint);
        }

        if (knob) {
            float knobRadius = height * 0.42f;
            float kx = radius + filled;
            kx = Math.min(Math.max(kx, knobRadius), width - knobRadius);
            paint.setColor(0xFFFFFFFF);
            canvas.drawCircle(kx, cy, knobRadius, paint);
            paint.setColor(fillColor);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Math.max(2f, knobRadius * 0.3f));
            canvas.drawCircle(kx, cy, Math.max(1f, knobRadius - knobRadius * 0.15f), paint);
        }
        return bitmap;
    }
}
