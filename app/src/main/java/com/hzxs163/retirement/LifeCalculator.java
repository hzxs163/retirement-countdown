package com.hzxs163.retirement;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/**
 * 生命倒计时：按“预期寿命”估算终点日，再算剩余天数。
 * 岁数用十分之一岁存整数（786 = 78.6 岁），避免浮点误差；
 * 换算成日期时：整数年 + round(小数 × 12) 个月，规则固定、可复现。
 */
public final class LifeCalculator {

    public static final int DEFAULT_YEARS_TENTHS = 786;
    public static final int MIN_YEARS_TENTHS = 300;
    public static final int MAX_YEARS_TENTHS = 1200;

    private LifeCalculator() {
    }

    public static Result calculate(LocalDate birthday, int yearsTenths, LocalDate today) {
        if (birthday == null) {
            throw new IllegalArgumentException("birthday is null");
        }
        if (today == null) {
            throw new IllegalArgumentException("today is null");
        }

        int clamped = clamp(yearsTenths);
        LocalDate endDate = birthday.plusYears(clamped / 10).plusMonths((clamped % 10) * 12 / 10);
        long days = ChronoUnit.DAYS.between(today, endDate);

        Result result = new Result();
        result.endDate = endDate;
        result.yearsTenths = clamped;
        result.days = days;
        result.daysRemaining = Math.max(0, days);
        result.remaining = days > 0 ? Period.between(today, endDate) : Period.ZERO;
        return result;
    }

    public static int clamp(int yearsTenths) {
        return Math.max(MIN_YEARS_TENTHS, Math.min(MAX_YEARS_TENTHS, yearsTenths));
    }

    public static String yearsText(int yearsTenths) {
        return String.format(Locale.CHINA, "%.1f 岁", clamp(yearsTenths) / 10.0);
    }

    public static final class Result {
        public LocalDate endDate;
        public int yearsTenths;
        public long days;
        public long daysRemaining;
        public Period remaining;
    }
}