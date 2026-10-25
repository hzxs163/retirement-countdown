package com.hzxs163.retirement;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class RefreshScheduler {

    private static final int REQUEST_CODE = 1001;
    private static final DateTimeFormatter NEXT_FORMAT =
            DateTimeFormatter.ofPattern("M月d日 HH:mm", Locale.CHINA);

    private RefreshScheduler() {
    }

    public static void scheduleNext(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        ZoneId zone = ZoneId.systemDefault();
        long triggerAt = LocalDate.now(zone)
                .plusDays(1)
                .atTime(0, 1)
                .atZone(zone)
                .toInstant()
                .toEpochMilli();

        PendingIntent operation = alarmIntent(context);
        boolean exact = exactAlarmsAllowed(context);

        try {
            if (exact) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, operation);
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, operation);
            }
            ProfileStore.setNextAlarmAt(context, triggerAt);
        } catch (SecurityException securityException) {
            try {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, operation);
                ProfileStore.setNextAlarmAt(context, triggerAt);
            } catch (RuntimeException ignored) {
                ProfileStore.setNextAlarmAt(context, 0L);
            }
        }
    }

    public static void cancel(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(alarmIntent(context));
        }
        ProfileStore.setNextAlarmAt(context, 0L);
    }

    public static boolean exactAlarmsAllowed(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true;
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        return alarmManager != null && alarmManager.canScheduleExactAlarms();
    }

    public static String formatNext(Context context) {
        long at = ProfileStore.getNextAlarmAt(context);
        if (at <= 0) {
            return "未安排";
        }
        if (at <= System.currentTimeMillis()) {
            return "等待系统执行";
        }
        return Instant.ofEpochMilli(at)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .format(NEXT_FORMAT);
    }

    private static PendingIntent alarmIntent(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_DAILY_ALARM);
        return PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}
