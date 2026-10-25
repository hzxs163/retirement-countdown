package com.hzxs163.retirement;

import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RetirementCalculatorTest {

    @Test
    public void maleBornBeforePolicyRetiresAtSixty() {
        RetirementCalculator.Result result = calculate(
                LocalDate.of(1964, 12, 31),
                RetirementCalculator.CATEGORY_MALE,
                60,
                LocalDate.of(2024, 1, 1));

        assertEquals(LocalDate.of(2024, 12, 31), result.retirementDate);
        assertEquals(0, result.delayMonths);
        assertEquals(60, result.retirementAgeYears);
        assertEquals(0, result.retirementAgeMonths);
    }

    @Test
    public void male1965JanuaryGetsOneMonthDelay() {
        RetirementCalculator.Result result = calculate(
                LocalDate.of(1965, 1, 15),
                RetirementCalculator.CATEGORY_MALE,
                60,
                LocalDate.of(2025, 1, 1));

        assertEquals(LocalDate.of(2025, 2, 15), result.retirementDate);
        assertEquals(1, result.delayMonths);
        assertEquals(60, result.retirementAgeYears);
        assertEquals(1, result.retirementAgeMonths);
    }

    @Test
    public void male1965AprilStillGetsOneMonthDelay() {
        RetirementCalculator.Result result = calculate(
                LocalDate.of(1965, 4, 30),
                RetirementCalculator.CATEGORY_MALE,
                60,
                LocalDate.of(2025, 1, 1));

        assertEquals(LocalDate.of(2025, 5, 30), result.retirementDate);
        assertEquals(1, result.delayMonths);
    }

    @Test
    public void male1965MayGetsTwoMonthsDelay() {
        RetirementCalculator.Result result = calculate(
                LocalDate.of(1965, 5, 15),
                RetirementCalculator.CATEGORY_MALE,
                60,
                LocalDate.of(2025, 1, 1));

        assertEquals(LocalDate.of(2025, 7, 15), result.retirementDate);
        assertEquals(2, result.delayMonths);
    }

    @Test
    public void male1976AugustIsTheLastNonSixtyThreeCohort() {
        RetirementCalculator.Result result = calculate(
                LocalDate.of(1976, 8, 15),
                RetirementCalculator.CATEGORY_MALE,
                60,
                LocalDate.of(2026, 10, 25));

        assertEquals(LocalDate.of(2039, 7, 15), result.retirementDate);
        assertEquals(35, result.delayMonths);
        assertEquals(62, result.retirementAgeYears);
        assertEquals(11, result.retirementAgeMonths);
    }

    @Test
    public void male1976SeptemberReachesSixtyThree() {
        RetirementCalculator.Result result = calculate(
                LocalDate.of(1976, 9, 15),
                RetirementCalculator.CATEGORY_MALE,
                60,
                LocalDate.of(2026, 10, 25));

        assertEquals(LocalDate.of(2039, 9, 15), result.retirementDate);
        assertEquals(36, result.delayMonths);
        assertEquals(63, result.retirementAgeYears);
        assertEquals(0, result.retirementAgeMonths);
    }

    @Test
    public void maleAfterTransitionStaysAtSixtyThree() {
        RetirementCalculator.Result result = calculate(
                LocalDate.of(1977, 1, 15),
                RetirementCalculator.CATEGORY_MALE,
                60,
                LocalDate.of(2026, 10, 25));

        assertEquals(LocalDate.of(2040, 1, 15), result.retirementDate);
        assertEquals(36, result.delayMonths);
        assertEquals(63, result.retirementAgeYears);
    }

    @Test
    public void female55January1970GetsOneMonthDelay() {
        RetirementCalculator.Result result = calculate(
                LocalDate.of(1970, 1, 15),
                RetirementCalculator.CATEGORY_FEMALE_55,
                55,
                LocalDate.of(2025, 1, 1));

        assertEquals(LocalDate.of(2025, 2, 15), result.retirementDate);
        assertEquals(1, result.delayMonths);
        assertEquals(55, result.retirementAgeYears);
        assertEquals(1, result.retirementAgeMonths);
    }

    @Test
    public void female55LastNonFiftyEightCohort() {
        RetirementCalculator.Result result = calculate(
                LocalDate.of(1981, 8, 15),
                RetirementCalculator.CATEGORY_FEMALE_55,
                55,
                LocalDate.of(2026, 10, 25));

        assertEquals(LocalDate.of(2039, 7, 15), result.retirementDate);
        assertEquals(35, result.delayMonths);
        assertEquals(57, result.retirementAgeYears);
        assertEquals(11, result.retirementAgeMonths);
    }

    @Test
    public void female55September1981ReachesFiftyEight() {
        RetirementCalculator.Result result = calculate(
                LocalDate.of(1981, 9, 15),
                RetirementCalculator.CATEGORY_FEMALE_55,
                55,
                LocalDate.of(2026, 10, 25));

        assertEquals(LocalDate.of(2039, 9, 15), result.retirementDate);
        assertEquals(58, result.retirementAgeYears);
    }

    @Test
    public void female55AfterTransitionStaysAtFiftyEight() {
        RetirementCalculator.Result result = calculate(
                LocalDate.of(1982, 1, 15),
                RetirementCalculator.CATEGORY_FEMALE_55,
                55,
                LocalDate.of(2026, 10, 25));

        assertEquals(LocalDate.of(2040, 1, 15), result.retirementDate);
        assertEquals(36, result.delayMonths);
        assertEquals(58, result.retirementAgeYears);
    }

    @Test
    public void female50January1975GetsOneMonthDelay() {
        RetirementCalculator.Result result = calculate(
                LocalDate.of(1975, 1, 15),
                RetirementCalculator.CATEGORY_FEMALE_50,
                50,
                LocalDate.of(2025, 1, 1));

        assertEquals(LocalDate.of(2025, 2, 15), result.retirementDate);
        assertEquals(1, result.delayMonths);
        assertEquals(50, result.retirementAgeYears);
        assertEquals(1, result.retirementAgeMonths);
    }

    @Test
    public void female50October1984IsTheLastUnderFiftyFive() {
        RetirementCalculator.Result result = calculate(
                LocalDate.of(1984, 10, 15),
                RetirementCalculator.CATEGORY_FEMALE_50,
                50,
                LocalDate.of(2026, 10, 25));

        assertEquals(LocalDate.of(2039, 9, 15), result.retirementDate);
        assertEquals(59, result.delayMonths);
        assertEquals(54, result.retirementAgeYears);
        assertEquals(11, result.retirementAgeMonths);
    }

    @Test
    public void female50November1984ReachesFiftyFive() {
        RetirementCalculator.Result result = calculate(
                LocalDate.of(1984, 11, 15),
                RetirementCalculator.CATEGORY_FEMALE_50,
                50,
                LocalDate.of(2026, 10, 25));

        assertEquals(LocalDate.of(2039, 11, 15), result.retirementDate);
        assertEquals(60, result.delayMonths);
        assertEquals(55, result.retirementAgeYears);
    }

    @Test
    public void female50AfterTransitionStaysAtFiftyFive() {
        RetirementCalculator.Result result = calculate(
                LocalDate.of(1985, 1, 15),
                RetirementCalculator.CATEGORY_FEMALE_50,
                50,
                LocalDate.of(2026, 10, 25));

        assertEquals(LocalDate.of(2040, 1, 15), result.retirementDate);
        assertEquals(60, result.delayMonths);
        assertEquals(55, result.retirementAgeYears);
    }

    @Test
    public void female50BeforePolicyHasNoDelay() {
        RetirementCalculator.Result result = calculate(
                LocalDate.of(1974, 12, 15),
                RetirementCalculator.CATEGORY_FEMALE_50,
                50,
                LocalDate.of(2024, 1, 1));

        assertEquals(LocalDate.of(2024, 12, 15), result.retirementDate);
        assertEquals(0, result.delayMonths);
    }

    @Test
    public void customAgeIgnoresPolicyDelay() {
        RetirementCalculator.Result result = calculate(
                LocalDate.of(1990, 6, 15),
                RetirementCalculator.CATEGORY_CUSTOM,
                60,
                LocalDate.of(2026, 10, 25));

        assertEquals(LocalDate.of(2050, 6, 15), result.retirementDate);
        assertEquals(0, result.delayMonths);
        assertEquals(60, result.retirementAgeYears);
    }

    @Test
    public void leapDayRetirementClampsToEndOfFebruary() {
        RetirementCalculator.Result leapYear = calculate(
                LocalDate.of(2000, 2, 29),
                RetirementCalculator.CATEGORY_CUSTOM,
                60,
                LocalDate.of(2060, 1, 1));
        assertEquals(LocalDate.of(2060, 2, 29), leapYear.retirementDate);

        RetirementCalculator.Result nonLeapYear = calculate(
                LocalDate.of(2000, 2, 29),
                RetirementCalculator.CATEGORY_CUSTOM,
                61,
                LocalDate.of(2061, 1, 1));
        assertEquals(LocalDate.of(2061, 2, 28), nonLeapYear.retirementDate);
    }

    @Test
    public void dayCountingHandlesBeforeOnAndAfterRetirement() {
        LocalDate birthday = LocalDate.of(1965, 1, 15);

        RetirementCalculator.Result before = calculate(
                birthday, RetirementCalculator.CATEGORY_MALE, 60, LocalDate.of(2025, 2, 14));
        assertFalse(before.retired);
        assertEquals(1L, before.days);
        assertEquals(1L, before.daysRemaining);
        assertEquals(0L, before.daysRetired);

        RetirementCalculator.Result onTheDay = calculate(
                birthday, RetirementCalculator.CATEGORY_MALE, 60, LocalDate.of(2025, 2, 15));
        assertFalse(onTheDay.retired);
        assertEquals(0L, onTheDay.days);
        assertEquals(0L, onTheDay.daysRemaining);

        RetirementCalculator.Result after = calculate(
                birthday, RetirementCalculator.CATEGORY_MALE, 60, LocalDate.of(2025, 2, 16));
        assertTrue(after.retired);
        assertEquals(-1L, after.days);
        assertEquals(1L, after.daysRetired);
    }

    @Test
    public void remainingPeriodIsMonthAccurate() {
        RetirementCalculator.Result result = calculate(
                LocalDate.of(1965, 1, 15),
                RetirementCalculator.CATEGORY_MALE,
                60,
                LocalDate.of(2025, 1, 15));

        assertEquals(1, result.remaining.getMonths());
        assertEquals(0, result.remaining.getDays());
    }

    @Test
    public void delayStepsUseBirthMonthBoundaries() {
        assertEquals(0, RetirementCalculator.progressiveDelayMonths(
                LocalDate.of(1964, 12, 31), 1965, 4, 36));
        assertEquals(1, RetirementCalculator.progressiveDelayMonths(
                LocalDate.of(1965, 1, 1), 1965, 4, 36));
        assertEquals(1, RetirementCalculator.progressiveDelayMonths(
                LocalDate.of(1965, 4, 30), 1965, 4, 36));
        assertEquals(2, RetirementCalculator.progressiveDelayMonths(
                LocalDate.of(1965, 5, 1), 1965, 4, 36));
        assertEquals(36, RetirementCalculator.progressiveDelayMonths(
                LocalDate.of(1976, 9, 1), 1965, 4, 36));
        assertEquals(36, RetirementCalculator.progressiveDelayMonths(
                LocalDate.of(1977, 1, 1), 1965, 4, 36));
        assertEquals(59, RetirementCalculator.progressiveDelayMonths(
                LocalDate.of(1984, 10, 15), 1975, 2, 60));
        assertEquals(60, RetirementCalculator.progressiveDelayMonths(
                LocalDate.of(1984, 11, 15), 1975, 2, 60));
        assertEquals(60, RetirementCalculator.progressiveDelayMonths(
                LocalDate.of(1985, 1, 1), 1975, 2, 60));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNullBirthday() {
        RetirementCalculator.calculate(null, RetirementCalculator.CATEGORY_MALE, 60, LocalDate.now());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsUnknownCategory() {
        RetirementCalculator.calculate(LocalDate.of(1990, 1, 1), 9, 60, LocalDate.now());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsOutOfRangeCustomAge() {
        RetirementCalculator.calculate(LocalDate.of(1990, 1, 1),
                RetirementCalculator.CATEGORY_CUSTOM, 101, LocalDate.now());
    }

    private static RetirementCalculator.Result calculate(LocalDate birthday, int category,
                                                         int customAge, LocalDate today) {
        return RetirementCalculator.calculate(birthday, category, customAge, today);
    }
}
