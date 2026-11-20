package com.hzxs163.retirement;

import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LifeCalculatorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 20);

    private LifeCalculator.Result calculate(LocalDate birthday, int yearsTenths, LocalDate today) {
        return LifeCalculator.calculate(birthday, yearsTenths, today);
    }

    @Test
    public void sixtyIsCoercedToMinimum() {
        assertEquals(LifeCalculator.MIN_YEARS_TENTHS, LifeCalculator.clamp(60));
    }

    @Test
    public void absurdValueIsCoercedToMaximum() {
        assertEquals(LifeCalculator.MAX_YEARS_TENTHS, LifeCalculator.clamp(5000));
    }

    @Test
    public void fractionalYearsBecomeWholeMonths() {
        // 78.6 岁 = 78 年 + 7 个月（0.6 × 12 = 7.2，向下取整）
        LifeCalculator.Result result = calculate(LocalDate.of(1982, 3, 10), 786, TODAY);
        assertEquals(LocalDate.of(2060, 10, 10), result.endDate);
        assertEquals(78, result.yearsTenths / 10);
        assertEquals(12378L, result.days);
        assertEquals(12378L, result.daysRemaining);
    }

    @Test
    public void leapDayBirthdayIsClamped() {
        LifeCalculator.Result result = calculate(LocalDate.of(2000, 2, 29), 786, TODAY);
        assertEquals(LocalDate.of(2078, 9, 28), result.endDate);
    }

    @Test
    public void wholeYearsKeepTheSameDay() {
        LifeCalculator.Result result = calculate(LocalDate.of(1990, 6, 15), 800, TODAY);
        assertEquals(LocalDate.of(2070, 6, 15), result.endDate);
    }

    @Test
    public void pastEndDateHasNoRemainingDays() {
        LifeCalculator.Result result = calculate(LocalDate.of(1930, 1, 1), 800, TODAY);
        assertTrue(result.days < 0);
        assertEquals(0L, result.daysRemaining);
    }

    @Test
    public void yearsTextHasOneDecimal() {
        assertEquals("78.6 岁", LifeCalculator.yearsText(786));
        assertEquals("30.0 岁", LifeCalculator.yearsText(300));
        assertEquals("120.0 岁", LifeCalculator.yearsText(1200));
    }
}