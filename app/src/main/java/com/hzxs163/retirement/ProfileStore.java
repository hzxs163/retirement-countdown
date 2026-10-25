package com.hzxs163.retirement;

import android.content.Context;
import android.content.SharedPreferences;

import java.time.LocalDate;

public final class ProfileStore {

    private static final String PREFS_NAME = "retirement_profile";
    private static final String KEY_BIRTHDAY = "birthday";
    private static final String KEY_CATEGORY = "category";
    private static final String KEY_CUSTOM_AGE = "custom_age";
    private static final String KEY_NEXT_ALARM = "next_alarm_at";

    private ProfileStore() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static LocalDate getBirthday(Context context) {
        String raw = prefs(context).getString(KEY_BIRTHDAY, null);
        if (raw == null) {
            return null;
        }
        try {
            return LocalDate.parse(raw);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    public static void setBirthday(Context context, LocalDate birthday) {
        if (birthday == null) {
            prefs(context).edit().remove(KEY_BIRTHDAY).apply();
        } else {
            prefs(context).edit().putString(KEY_BIRTHDAY, birthday.toString()).apply();
        }
    }

    public static int getCategory(Context context) {
        int value = prefs(context).getInt(KEY_CATEGORY, RetirementCalculator.CATEGORY_MALE);
        if (value < RetirementCalculator.CATEGORY_MALE || value > RetirementCalculator.CATEGORY_CUSTOM) {
            return RetirementCalculator.CATEGORY_MALE;
        }
        return value;
    }

    public static void setCategory(Context context, int category) {
        prefs(context).edit().putInt(KEY_CATEGORY, category).apply();
    }

    public static int getCustomAge(Context context) {
        int age = prefs(context).getInt(KEY_CUSTOM_AGE, 60);
        return Math.max(1, Math.min(100, age));
    }

    public static void setCustomAge(Context context, int age) {
        prefs(context).edit().putInt(KEY_CUSTOM_AGE, age).apply();
    }

    public static boolean isConfigured(Context context) {
        return getBirthday(context) != null;
    }

    public static void setNextAlarmAt(Context context, long millis) {
        prefs(context).edit().putLong(KEY_NEXT_ALARM, millis).apply();
    }

    public static long getNextAlarmAt(Context context) {
        return prefs(context).getLong(KEY_NEXT_ALARM, 0L);
    }
}
