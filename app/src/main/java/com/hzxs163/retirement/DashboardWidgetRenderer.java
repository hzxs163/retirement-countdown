package com.hzxs163.retirement;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.widget.RemoteViews;

import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** 整屏仪表盘小组件：文字用真 TextView，圆环/进度条用 Bitmap。 */
public final class DashboardWidgetRenderer {

    private static final DateTimeFormatter HEAD_FORMAT =
            DateTimeFormatter.ofPattern("yyyy年M月d日 EEEE", Locale.CHINA);
    private static final DateTimeFormatter MONTH_FORMAT =
            DateTimeFormatter.ofPattern("yyyy年M月", Locale.CHINA);

    private static final int[] RING = {
            R.id.wd_ring_1, R.id.wd_ring_2, R.id.wd_ring_3, R.id.wd_ring_4,
            R.id.wd_ring_5, R.id.wd_ring_6, R.id.wd_ring_7, R.id.wd_ring_8,
            R.id.wd_ring_9, R.id.wd_ring_10, R.id.wd_ring_11, R.id.wd_ring_12,
            R.id.wd_ring_13,
    };
    private static final int[] EMOJI = {
            R.id.wd_emoji_1, R.id.wd_emoji_2, R.id.wd_emoji_3, R.id.wd_emoji_4,
            R.id.wd_emoji_5, R.id.wd_emoji_6, R.id.wd_emoji_7, R.id.wd_emoji_8,
            R.id.wd_emoji_9, R.id.wd_emoji_10, R.id.wd_emoji_11, R.id.wd_emoji_12,
            R.id.wd_emoji_13,
    };
    private static final int[] VALUE = {
            R.id.wd_value_1, R.id.wd_value_2, R.id.wd_value_3, R.id.wd_value_4,
            R.id.wd_value_5, R.id.wd_value_6, R.id.wd_value_7, R.id.wd_value_8,
            R.id.wd_value_9, R.id.wd_value_10, R.id.wd_value_11, R.id.wd_value_12,
            R.id.wd_value_13,
    };
    private static final int[] LABEL = {
            R.id.wd_label_1, R.id.wd_label_2, R.id.wd_label_3, R.id.wd_label_4,
            R.id.wd_label_5, R.id.wd_label_6, R.id.wd_label_7, R.id.wd_label_8,
            R.id.wd_label_9, R.id.wd_label_10, R.id.wd_label_11, R.id.wd_label_12,
            R.id.wd_label_13,
    };
    private static final int[] PERCENT = {
            R.id.wd_pct_1, R.id.wd_pct_2, R.id.wd_pct_3, R.id.wd_pct_4,
            R.id.wd_pct_5, R.id.wd_pct_6, R.id.wd_pct_7, R.id.wd_pct_8,
            R.id.wd_pct_9, R.id.wd_pct_10, R.id.wd_pct_11, R.id.wd_pct_12,
            R.id.wd_pct_13,
    };

    private DashboardWidgetRenderer() {
    }

    public static RemoteViews build(Context context, int widthDp) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_dashboard);
        views.setOnClickPendingIntent(R.id.wd_root, openApp(context));

        int width = Math.max(240, Math.min(620, widthDp));
        float density = context.getResources().getDisplayMetrics().density;
        int orange = context.getColor(R.color.dash_orange);
        int trackColor = context.getColor(R.color.dash_track);
        int accent = context.getColor(R.color.accent);

        LocalDate birthday = ProfileStore.getBirthday(context);
        if (birthday == null) {
            views.setTextViewText(R.id.wd_date, LocalDate.now().format(HEAD_FORMAT));
            views.setTextViewText(R.id.wd_birth, "未设置");
            views.setTextViewText(R.id.wd_end, "—");
            views.setTextViewText(R.id.wd_birth_box, "点一下设置生日");
            views.setTextViewText(R.id.wd_life_label, "预期寿命：—");
            views.setTextViewText(R.id.wd_life_value, "--");
            views.setTextViewText(R.id.wd_used_percent, "--");
            views.setTextViewText(R.id.wd_used_detail, "先设置出生日期");
            views.setTextViewText(R.id.wd_remain_detail, "点卡片打开 App");
            for (int i = 0; i < RING.length; i++) {
                views.setTextViewText(VALUE[i], "--");
                views.setTextViewText(PERCENT[i], "");
                views.setImageViewBitmap(RING[i],
                        WidgetGraphics.ring(px(density, 30), px(density, 5), 0f, trackColor, trackColor));
            }
            views.setImageViewBitmap(R.id.wd_track,
                    WidgetGraphics.bar(px(density, width - 190), px(density, 22), 0f, orange, trackColor, false));
            views.setImageViewBitmap(R.id.wd_bar,
                    WidgetGraphics.bar(px(density, width - 44), px(density, 10), 0f, orange, trackColor, false));
            views.setImageViewBitmap(R.id.wd_donut,
                    WidgetGraphics.ring(px(density, 92), px(density, 10), 0f, orange, trackColor));
            views.setImageViewBitmap(R.id.wd_life_slider,
                    WidgetGraphics.bar(px(density, 120), px(density, 18), 0.5f, accent, trackColor, true));
            applyLegend(views);
            return views;
        }

        LocalDate today = LocalDate.now();
        int yearsTenths = LifeCalculator.clamp(ProfileStore.getLifeYearsTenths(context));
        LifeStats stats = LifeStats.compute(birthday, yearsTenths, today);

        views.setTextViewText(R.id.wd_date, today.format(HEAD_FORMAT));
        views.setTextViewText(R.id.wd_birth, birthday.format(MONTH_FORMAT));
        views.setTextViewText(R.id.wd_end, stats.end.format(MONTH_FORMAT));
        views.setTextViewText(R.id.wd_birth_box, birthday.format(MONTH_FORMAT));
        views.setTextViewText(R.id.wd_life_label, "预期寿命：" + LifeCalculator.yearsText(yearsTenths));
        views.setTextViewText(R.id.wd_life_value,
                String.format(Locale.CHINA, "%.1f", yearsTenths / 10.0));
        views.setTextViewText(R.id.wd_used_percent,
                String.format(Locale.CHINA, "%.1f%%", stats.usedRatio * 100f));
        views.setTextViewText(R.id.wd_used_detail,
                periodText(stats.elapsedPeriod) + "（" + group(stats.elapsedDays) + " 天）");
        views.setTextViewText(R.id.wd_remain_detail,
                periodText(stats.remainPeriod) + "（" + group(stats.remainDays) + " 天）");

        ZonedDateTime now = ZonedDateTime.now();
        long toMidnight = Duration.between(now,
                now.toLocalDate().plusDays(1).atStartOfDay(now.getZone())).toMillis();
        views.setChronometer(R.id.wd_clock, SystemClock.elapsedRealtime() + Math.max(1000L, toMidnight),
                "%s", true);
        views.setChronometerCountDown(R.id.wd_clock, true);

        views.setImageViewBitmap(R.id.wd_track,
                WidgetGraphics.bar(px(density, width - 190), px(density, 22),
                        stats.usedRatio, orange, trackColor, true));
        views.setImageViewBitmap(R.id.wd_bar,
                WidgetGraphics.bar(px(density, width - 44), px(density, 10),
                        stats.usedRatio, orange, trackColor, false));
        views.setImageViewBitmap(R.id.wd_donut,
                WidgetGraphics.ring(px(density, 92), px(density, 10),
                        stats.usedRatio, orange, trackColor));

        float lifeRatio = (yearsTenths - LifeCalculator.MIN_YEARS_TENTHS)
                / (float) (LifeCalculator.MAX_YEARS_TENTHS - LifeCalculator.MIN_YEARS_TENTHS);
        views.setImageViewBitmap(R.id.wd_life_slider,
                WidgetGraphics.bar(px(density, Math.max(70, (width - 54) / 2 - 46)), px(density, 18),
                        lifeRatio, accent, trackColor, true));

        for (int i = 0; i < RING.length && i < stats.stats.size(); i++) {
            LifeStats.Stat stat = stats.stats.get(i);
            views.setTextViewText(EMOJI[i], stat.emoji);
            views.setTextViewText(VALUE[i], stat.value);
            views.setTextViewText(LABEL[i], stat.label);
            views.setTextViewText(PERCENT[i], stat.percent);
            views.setTextColor(PERCENT[i], stat.color);
            views.setImageViewBitmap(RING[i],
                    WidgetGraphics.ring(px(density, 30), px(density, 5),
                            stats.remainRatio, stat.color, trackColor));
        }

        applyLegend(views);
        return views;
    }

    private static void applyLegend(RemoteViews views) {
        views.setInt(R.id.wd_legend_1, "setBackgroundColor", LifeStats.COLOR_DAILY);
        views.setInt(R.id.wd_legend_2, "setBackgroundColor", LifeStats.COLOR_PHYSIO);
        views.setInt(R.id.wd_legend_3, "setBackgroundColor", LifeStats.COLOR_WORK);
        views.setInt(R.id.wd_legend_4, "setBackgroundColor", LifeStats.COLOR_SEASON);
    }

    private static String periodText(Period period) {
        StringBuilder builder = new StringBuilder();
        if (period.getYears() > 0) {
            builder.append(period.getYears()).append("年 ");
        }
        if (period.getYears() > 0 || period.getMonths() > 0) {
            builder.append(period.getMonths()).append("个月 ");
        }
        builder.append(period.getDays()).append("天");
        return builder.toString();
    }

    private static String group(long value) {
        return NumberFormat.getIntegerInstance(Locale.CHINA).format(value);
    }

    private static int px(float density, float dpValue) {
        return Math.round(dpValue * density);
    }

    private static PendingIntent openApp(Context context) {
        Intent intent = new Intent(context, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(
                context,
                4001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}
