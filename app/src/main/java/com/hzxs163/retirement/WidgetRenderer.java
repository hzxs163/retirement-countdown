package com.hzxs163.retirement;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class WidgetRenderer {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy年M月d日", Locale.CHINA);
    private static final DateTimeFormatter UPDATE_FORMAT =
            DateTimeFormatter.ofPattern("M月d日 HH:mm", Locale.CHINA);

    private WidgetRenderer() {
    }

    public static RemoteViews build(Context context) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_retirement);
        PendingIntent openApp = openAppIntent(context);
        views.setOnClickPendingIntent(R.id.widget_root, openApp);
        views.setOnClickPendingIntent(R.id.widget_hint, openApp);
        views.setTextViewText(R.id.widget_updated, "更新于 " + LocalDateTime.now().format(UPDATE_FORMAT));

        LocalDate birthday = ProfileStore.getBirthday(context);
        if (birthday == null) {
            views.setTextViewText(R.id.widget_label, "退休倒计时");
            views.setTextViewText(R.id.widget_badge, "未设置");
            views.setTextViewText(R.id.widget_days, "--");
            views.setTextViewText(R.id.widget_unit, "天");
            views.setTextViewText(R.id.widget_line1, "点一下，设置生日");
            views.setTextViewText(R.id.widget_line2, "自动计算退休日期");
            views.setTextViewText(R.id.widget_hint, "去设置 ›");
            views.setContentDescription(R.id.widget_root, "退休倒计时，还没有设置生日。点击打开应用设置。");
            return views;
        }

        int category = ProfileStore.getCategory(context);
        int customAge = ProfileStore.getCustomAge(context);
        RetirementCalculator.Result result =
                RetirementCalculator.calculate(birthday, category, customAge, LocalDate.now());

        long shownDays = result.retired ? result.daysRetired : result.daysRemaining;
        views.setTextViewText(R.id.widget_days, formatNumber(shownDays));
        views.setTextViewText(R.id.widget_unit, "天");
        views.setTextViewText(R.id.widget_badge, categoryBadge(category, customAge));

        String label;
        if (result.retired) {
            label = "已经退休";
        } else if (result.days == 0) {
            label = "今天退休";
        } else {
            label = "距退休还有";
        }
        views.setTextViewText(R.id.widget_label, label);

        String dateLine;
        if (result.days <= 0) {
            dateLine = "退休日 " + result.retirementDate.format(DATE_FORMAT);
        } else {
            dateLine = "预计 " + result.retirementDate.format(DATE_FORMAT) + " 退休";
        }
        views.setTextViewText(R.id.widget_line1, dateLine);

        String subLine;
        if (result.retired) {
            subLine = "祝退休生活愉快";
        } else if (result.days == 0) {
            subLine = "从今天起，时间归你";
        } else {
            subLine = "还需 " + remainingText(result.remaining)
                    + " · " + ageText(result) + "退休";
        }
        views.setTextViewText(R.id.widget_line2, subLine);
        views.setTextViewText(R.id.widget_hint, "打开设置 ›");

        String description;
        if (result.retired) {
            description = "退休倒计时，已经退休 " + formatNumber(result.daysRetired)
                    + " 天。点击打开设置。";
        } else {
            description = "退休倒计时，距离退休还有 " + formatNumber(result.daysRemaining)
                    + " 天，预计 " + result.retirementDate.format(DATE_FORMAT) + " 退休。点击打开设置。";
        }
        views.setContentDescription(R.id.widget_root, description);
        return views;
    }

    public static String categoryBadge(int category, int customAge) {
        switch (category) {
            case RetirementCalculator.CATEGORY_MALE:
                return "男职工";
            case RetirementCalculator.CATEGORY_FEMALE_55:
                return "女职工 · 55岁档";
            case RetirementCalculator.CATEGORY_FEMALE_50:
                return "女职工 · 50岁档";
            case RetirementCalculator.CATEGORY_CUSTOM:
                return "自定义 " + customAge + " 岁";
            default:
                return "男职工";
        }
    }

    public static String ageText(RetirementCalculator.Result result) {
        if (result.retirementAgeMonths == 0) {
            return result.retirementAgeYears + " 岁";
        }
        return result.retirementAgeYears + " 岁 " + result.retirementAgeMonths + " 个月";
    }

    public static String remainingText(Period period) {
        if (period.getYears() > 0) {
            if (period.getMonths() == 0) {
                return period.getYears() + " 年";
            }
            return period.getYears() + " 年 " + period.getMonths() + " 个月";
        }
        if (period.getMonths() > 0) {
            if (period.getDays() == 0) {
                return period.getMonths() + " 个月";
            }
            return period.getMonths() + " 个月 " + period.getDays() + " 天";
        }
        return period.getDays() + " 天";
    }

    private static String formatNumber(long value) {
        return NumberFormat.getIntegerInstance(Locale.CHINA).format(value);
    }

    private static PendingIntent openAppIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(
                context,
                2001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}
