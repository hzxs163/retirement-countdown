package com.hzxs163.retirement;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** 生命仪表盘的全部数字：已度过 / 剩余 / 还能做多少次。 */
public final class LifeStats {

    public static final int COLOR_DAILY = 0xFF3B82F6;
    public static final int COLOR_PHYSIO = 0xFFF0436B;
    public static final int COLOR_WORK = 0xFFF26B21;
    public static final int COLOR_SEASON = 0xFF22A06B;

    public static final class Stat {
        public final String emoji;
        public final String label;
        public final String value;
        public final String percent;
        public final int color;

        Stat(String emoji, String label, String value, String percent, int color) {
            this.emoji = emoji;
            this.label = label;
            this.value = value;
            this.percent = percent;
            this.color = color;
        }
    }

    public final LocalDate birth;
    public final LocalDate end;
    public final long totalDays;
    public final long elapsedDays;
    public final long remainDays;
    public final float usedRatio;
    public final float remainRatio;
    public final Period elapsedPeriod;
    public final Period remainPeriod;
    public final List<Stat> stats = new ArrayList<>();

    private LifeStats(LocalDate birth, LocalDate end, LocalDate today) {
        this.birth = birth;
        this.end = end;
        long total = Math.max(1, ChronoUnit.DAYS.between(birth, end));
        long elapsed = Math.max(0, Math.min(total, ChronoUnit.DAYS.between(birth, today)));
        totalDays = total;
        elapsedDays = elapsed;
        remainDays = Math.max(0, ChronoUnit.DAYS.between(today, end));
        usedRatio = (float) elapsed / (float) total;
        remainRatio = 1f - usedRatio;
        elapsedPeriod = Period.between(birth, today.isAfter(end) ? end : today);
        remainPeriod = today.isBefore(end) ? Period.between(today, end) : Period.ZERO;
        buildStats();
    }

    public static LifeStats compute(LocalDate birth, int yearsTenths, LocalDate today) {
        int clamped = LifeCalculator.clamp(yearsTenths);
        LocalDate end = birth.plusYears(clamped / 10).plusMonths((clamped % 10) * 12 / 10);
        return new LifeStats(birth, end, today);
    }

    private void buildStats() {
        String pct = Math.round(remainRatio * 100) + "%";
        double d = remainDays;

        add("🍜", "吃饭", d * 3, pct, COLOR_DAILY);
        add("😴", "睡觉", d, pct, COLOR_DAILY);
        add("🌅", "日落", d, pct, COLOR_DAILY);
        add("❤️", "心跳", d * 86400 * 72 / 60.0, pct, COLOR_PHYSIO);
        add("🚽", "上厕所", d * 8, pct, COLOR_PHYSIO);
        add("💼", "工作日", d * 261 / 365.0, pct, COLOR_WORK);
        add("🎉", "周末", d * 104 / 365.0, pct, COLOR_WORK);
        add("🗓", "假期", d * 11 / 365.0, pct, COLOR_WORK);
        add("🌸", "春节", d / 365.0, pct, COLOR_SEASON);
        add("☀️", "夏天", d * 3 / 365.0, pct, COLOR_SEASON);
        add("🎂", "生日", d / 365.0, pct, COLOR_SEASON);
        add("🌙", "满月", d / 29.53, pct, COLOR_SEASON);
    }

    private void add(String emoji, String label, double count, String percent, int color) {
        stats.add(new Stat(emoji, label, format(count), percent, color));
    }

    static String format(double value) {
        if (value >= 1e8) {
            return String.format(Locale.CHINA, "%.1f亿", value / 1e8);
        }
        if (value >= 1e4) {
            return String.format(Locale.CHINA, "%.1f万", value / 1e4);
        }
        return String.valueOf(Math.max(0, Math.round(value)));
    }
}
