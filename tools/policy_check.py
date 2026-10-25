"""Independent mirror of RetirementCalculator for cross-checking on the host.

This is not shipped in the APK. It mirrors java.time's plusYears/plusMonths
clamping so the boundary dates can be verified without a local JDK.
"""

import calendar
from datetime import date


MALE = "male"
FEMALE_55 = "female55"
FEMALE_50 = "female50"
CUSTOM = "custom"


def add_months(value, months):
    total = value.year * 12 + (value.month - 1) + months
    year = total // 12
    month = total % 12 + 1
    day = min(value.day, calendar.monthrange(year, month)[1])
    return date(year, month, day)


def add_years(value, years):
    return add_months(value, years * 12)


def progressive_delay(birthday, first_year, months_per_step, max_delay):
    if birthday.year < first_year:
        return 0
    elapsed = (birthday.year - first_year) * 12 + (birthday.month - 1)
    return min(max_delay, elapsed // months_per_step + 1)


def calculate(birthday, category, custom_age):
    if category == MALE:
        base_age, delay = 60, progressive_delay(birthday, 1965, 4, 36)
    elif category == FEMALE_55:
        base_age, delay = 55, progressive_delay(birthday, 1970, 4, 36)
    elif category == FEMALE_50:
        base_age, delay = 50, progressive_delay(birthday, 1975, 2, 60)
    elif category == CUSTOM:
        base_age, delay = custom_age, 0
    else:
        raise ValueError(category)

    retirement = add_months(add_years(birthday, base_age), delay)
    return retirement, base_age + delay // 12, delay % 12, delay


CASES = [
    ((1964, 12, 31), MALE, 60, (2024, 12, 31), 60, 0, 0),
    ((1965, 1, 15), MALE, 60, (2025, 2, 15), 60, 1, 1),
    ((1965, 4, 30), MALE, 60, (2025, 5, 30), 60, 1, 1),
    ((1965, 5, 15), MALE, 60, (2025, 7, 15), 60, 2, 2),
    ((1976, 8, 15), MALE, 60, (2039, 7, 15), 62, 11, 35),
    ((1976, 9, 15), MALE, 60, (2039, 9, 15), 63, 0, 36),
    ((1977, 1, 15), MALE, 60, (2040, 1, 15), 63, 0, 36),
    ((1970, 1, 15), FEMALE_55, 55, (2025, 2, 15), 55, 1, 1),
    ((1981, 8, 15), FEMALE_55, 55, (2039, 7, 15), 57, 11, 35),
    ((1981, 9, 15), FEMALE_55, 55, (2039, 9, 15), 58, 0, 36),
    ((1982, 1, 15), FEMALE_55, 55, (2040, 1, 15), 58, 0, 36),
    ((1975, 1, 15), FEMALE_50, 50, (2025, 2, 15), 50, 1, 1),
    ((1984, 10, 15), FEMALE_50, 50, (2039, 9, 15), 54, 11, 59),
    ((1984, 11, 15), FEMALE_50, 50, (2039, 11, 15), 55, 0, 60),
    ((1985, 1, 15), FEMALE_50, 50, (2040, 1, 15), 55, 0, 60),
    ((1990, 6, 15), CUSTOM, 60, (2050, 6, 15), 60, 0, 0),
    ((2000, 2, 29), CUSTOM, 60, (2060, 2, 29), 60, 0, 0),
    ((2000, 2, 29), CUSTOM, 61, (2061, 2, 28), 61, 0, 0),
]


def main():
    failures = 0
    for birthday, category, custom_age, expected_date, years, months, delay in CASES:
        actual = calculate(date(*birthday), category, custom_age)
        expected = (date(*expected_date), years, months, delay)
        label = f"{birthday} {category} age={custom_age}"
        if actual != expected:
            failures += 1
            print(f"FAIL {label}: expected {expected}, got {actual}")
        else:
            print(f"PASS {label}: {actual[0]} age={actual[1]}y{actual[2]}m delay={actual[3]}m")

    print(f"\n{len(CASES) - failures}/{len(CASES)} cases passed")
    raise SystemExit(1 if failures else 0)


if __name__ == "__main__":
    main()
