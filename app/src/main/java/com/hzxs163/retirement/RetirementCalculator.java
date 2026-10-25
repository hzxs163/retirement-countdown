package com.hzxs163.retirement;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;

public final class RetirementCalculator {

    public static final int CATEGORY_MALE = 0;
    public static final int CATEGORY_FEMALE_55 = 1;
    public static final int CATEGORY_FEMALE_50 = 2;
    public static final int CATEGORY_CUSTOM = 3;

    private RetirementCalculator() {
    }

    public static Result calculate(LocalDate birthday, int category, int customAge, LocalDate today) {
        if (birthday == null) {
            throw new IllegalArgumentException("birthday is null");
        }
        if (today == null) {
            throw new IllegalArgumentException("today is null");
        }

        int baseAge;
        int delayMonths;
        switch (category) {
            case CATEGORY_MALE:
                baseAge = 60;
                delayMonths = progressiveDelayMonths(birthday, 1965, 4, 36);
                break;
            case CATEGORY_FEMALE_55:
                baseAge = 55;
                delayMonths = progressiveDelayMonths(birthday, 1970, 4, 36);
                break;
            case CATEGORY_FEMALE_50:
                baseAge = 50;
                delayMonths = progressiveDelayMonths(birthday, 1975, 2, 60);
                break;
            case CATEGORY_CUSTOM:
                if (customAge < 1 || customAge > 100) {
                    throw new IllegalArgumentException("customAge out of range: " + customAge);
                }
                baseAge = customAge;
                delayMonths = 0;
                break;
            default:
                throw new IllegalArgumentException("unknown category: " + category);
        }

        LocalDate retirementDate = birthday.plusYears(baseAge).plusMonths(delayMonths);
        long days = ChronoUnit.DAYS.between(today, retirementDate);

        Result result = new Result();
        result.retirementDate = retirementDate;
        result.baseAge = baseAge;
        result.delayMonths = delayMonths;
        result.retirementAgeYears = baseAge + delayMonths / 12;
        result.retirementAgeMonths = delayMonths % 12;
        result.days = days;
        result.retired = days < 0;
        result.daysRemaining = days > 0 ? days : 0;
        result.daysRetired = days < 0 ? -days : 0;
        result.remaining = days > 0 ? Period.between(today, retirementDate) : Period.ZERO;
        return result;
    }

    static int progressiveDelayMonths(LocalDate birthday, int firstYear, int monthsPerStep, int maxDelayMonths) {
        if (birthday.getYear() < firstYear) {
            return 0;
        }
        int elapsedMonths = (birthday.getYear() - firstYear) * 12 + (birthday.getMonthValue() - 1);
        return Math.min(maxDelayMonths, elapsedMonths / monthsPerStep + 1);
    }

    public static final class Result {
        public LocalDate retirementDate;
        public int baseAge;
        public int delayMonths;
        public int retirementAgeYears;
        public int retirementAgeMonths;
        public long days;
        public boolean retired;
        public long daysRemaining;
        public long daysRetired;
        public Period remaining;
    }
}
