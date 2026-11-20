package com.hzxs163.retirement;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** 整屏仪表盘小组件：文字用真 TextView，圆环/进度条用 Bitmap。 */
public final class DashboardWidgetRenderer {

    private static final DateTimeFormatter RETIRE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy年M月d日", Locale.CHINA);

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
    private static final int[] QUOTES = {
            R.id.wd_quote_1, R.id.wd_quote_2, R.id.wd_quote_3,
            R.id.wd_quote_4, R.id.wd_quote_5, R.id.wd_quote_6,
    };

    private DashboardWidgetRenderer() {
    }

    public static RemoteViews build(Context context, int widthDp) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_dashboard);
        views.setOnClickPendingIntent(R.id.wd_root, openApp(context));
        WidgetRenderer.applyQuotes(views, QUOTES);

        int width = Math.max(240, Math.min(620, widthDp));
        float density = context.getResources().getDisplayMetrics().density;
        int orange = context.getColor(R.color.dash_orange);
        int trackColor = context.getColor(R.color.dash_track);

        LocalDate birthday = ProfileStore.getBirthday(context);
        if (birthday == null) {
            views.setTextViewText(R.id.wd_label, "退休倒计时");
            views.setTextViewText(R.id.wd_badge, "未设置");
            views.setTextViewText(R.id.wd_days, "--");
            views.setTextViewText(R.id.wd_unit, "天");
            views.setTextViewText(R.id.wd_line1, "点一下，设置生日");
            views.setTextViewText(R.id.wd_line2, "自动计算退休日期");
            views.setTextViewText(R.id.wd_used_percent, "--");
            views.setTextViewText(R.id.wd_used_detail, "先设置出生日期");
            views.setTextViewText(R.id.wd_remain_detail, "点卡片打开 App");
            for (int i = 0; i < RING.length; i++) {
                views.setTextViewText(VALUE[i], "--");
                views.setTextViewText(PERCENT[i], "");
                views.setImageViewBitmap(RING[i],
                        WidgetGraphics.ring(px(density, 28), px(density, 5), 0f, trackColor, trackColor));
            }
            views.setImageViewBitmap(R.id.wd_bar,
                    WidgetGraphics.bar(px(density, width - 44), px(density, 10), 0f, orange, trackColor, false));
            views.setImageViewBitmap(R.id.wd_donut,
                    WidgetGraphics.ring(px(density, 84), px(density, 10), 0f, orange, trackColor));
            applyLegend(views);
            return views;
        }

        int category = ProfileStore.getCategory(context);
        int customAge = ProfileStore.getCustomAge(context);
        LocalDate today = LocalDate.now();
        RetirementCalculator.Result result =
                RetirementCalculator.calculate(birthday, category, customAge, today);

        long shownDays = result.retired ? result.daysRetired : result.daysRemaining;
        views.setTextViewText(R.id.wd_days, group(shownDays));
        views.setTextViewText(R.id.wd_unit, "天");
        views.setTextViewText(R.id.wd_badge, WidgetRenderer.categoryBadge(category, customAge));

        String label;
        if (result.retired) {
            label = "已经退休";
        } else if (result.days == 0) {
            label = "今天退休";
        } else {
            label = "距退休还有";
        }
        views.setTextViewText(R.id.wd_label, label);

        String dateLine;
        if (result.days <= 0) {
            dateLine = "退休日 " + result.retirementDate.format(RETIRE_FORMAT);
        } else {
            dateLine = "预计 " + result.retirementDate.format(RETIRE_FORMAT) + " 退休";
        }
        views.setTextViewText(R.id.wd_line1, dateLine);

        String subLine;
        if (result.retired) {
            subLine = "祝退休生活愉快";
        } else if (result.days == 0) {
            subLine = "从今天起，时间归你";
        } else {
            subLine = "还需 " + WidgetRenderer.remainingText(result.remaining)
                    + " · " + WidgetRenderer.ageText(result) + "退休";
        }
        views.setTextViewText(R.id.wd_line2, subLine);

        int yearsTenths = LifeCalculator.clamp(ProfileStore.getLifeYearsTenths(context));
        LifeStats stats = LifeStats.compute(birthday, yearsTenths, today);

        views.setTextViewText(R.id.wd_used_percent,
                String.format(Locale.CHINA, "%.1f%%", stats.usedRatio * 100f));
        views.setTextViewText(R.id.wd_used_detail,
                periodText(stats.elapsedPeriod) + "（" + group(stats.elapsedDays) + " 天）");
        views.setTextViewText(R.id.wd_remain_detail,
                periodText(stats.remainPeriod) + "（" + group(stats.remainDays) + " 天）");

        views.setImageViewBitmap(R.id.wd_bar,
                WidgetGraphics.bar(px(density, width - 44), px(density, 10),
                        stats.usedRatio, orange, trackColor, false));
        views.setImageViewBitmap(R.id.wd_donut,
                WidgetGraphics.ring(px(density, 84), px(density, 10),
                        stats.usedRatio, orange, trackColor));

        for (int i = 0; i < RING.length && i < stats.stats.size(); i++) {
            LifeStats.Stat stat = stats.stats.get(i);
            views.setTextViewText(EMOJI[i], stat.emoji);
            views.setTextViewText(VALUE[i], stat.value);
            views.setTextViewText(LABEL[i], stat.label);
            views.setTextViewText(PERCENT[i], stat.percent);
            views.setTextColor(PERCENT[i], stat.color);
            views.setImageViewBitmap(RING[i],
                    WidgetGraphics.ring(px(density, 28), px(density, 5),
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
