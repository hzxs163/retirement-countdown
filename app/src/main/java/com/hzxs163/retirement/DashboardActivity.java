package com.hzxs163.retirement;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.GridLayout;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DashboardActivity extends Activity {

    private static final DateTimeFormatter HEAD_FORMAT =
            DateTimeFormatter.ofPattern("yyyy年M月d日 EEEE", Locale.CHINA);
    private static final DateTimeFormatter MONTH_FORMAT =
            DateTimeFormatter.ofPattern("yyyy年M月", Locale.CHINA);

    private static final int SEEK_MIN_TENTHS = 300;
    private static final int SEEK_MAX_TENTHS = 1200;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            renderClock();
            long delay = 1000L - (System.currentTimeMillis() % 1000L);
            handler.postDelayed(this, delay);
        }
    };

    private TextView dashDate;
    private TextView dashClock;
    private TextView dashBirth;
    private TextView dashEnd;
    private TextView dashBirthValue;
    private TextView dashLifeLabel;
    private TextView dashLifeValue;
    private TextView dashUsedPercent;
    private TextView dashUsedDetail;
    private TextView dashRemainDetail;
    private SeekBar dashLifeSeek;
    private TrackBarView dashTrack;
    private TrackBarView dashRingTrack;
    private DonutView dashDonut;
    private GridLayout dashGrid;

    private LocalDate birthday;
    private int yearsTenths;
    private boolean seekReady;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        bindViews();

        birthday = ProfileStore.getBirthday(this);
        if (birthday == null) {
            openSettings();
            return;
        }

        findViewById(R.id.dash_settings).setOnClickListener(v -> openSettings());
        findViewById(R.id.row_dash_birth).setOnClickListener(v -> pickBirthYearMonth());

        dashTrack.setShowKnob(true);
        dashRingTrack.setShowKnob(false);
        dashRingTrack.setBarHeight(dp(6));
        dashDonut.setRingWidth(dp(11));
        dashDonut.setColors(getColor(R.color.dash_orange), getColor(R.color.dash_blue));
        dashTrack.setColors(getColor(R.color.dash_orange), getColor(R.color.dash_track));
        dashRingTrack.setColors(getColor(R.color.dash_orange), getColor(R.color.dash_track));

        dashLifeSeek.setMax(SEEK_MAX_TENTHS - SEEK_MIN_TENTHS);
        dashLifeSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!seekReady || !fromUser) {
                    return;
                }
                int tenths = tenthsOf(progress);
                dashLifeLabel.setText("预期寿命：" + LifeCalculator.yearsText(tenths));
                dashLifeValue.setText(formatYears(tenths));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (!seekReady) {
                    return;
                }
                yearsTenths = LifeCalculator.clamp(tenthsOf(seekBar.getProgress()));
                ProfileStore.setLifeYearsTenths(DashboardActivity.this, yearsTenths);
                renderAll();
                Widgets.refreshAll(DashboardActivity.this);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        birthday = ProfileStore.getBirthday(this);
        if (birthday == null) {
            return;
        }
        renderAll();
        handler.removeCallbacks(ticker);
        handler.post(ticker);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(ticker);
    }

    private void bindViews() {
        dashDate = findViewById(R.id.dash_date);
        dashClock = findViewById(R.id.dash_clock);
        dashBirth = findViewById(R.id.dash_birth);
        dashEnd = findViewById(R.id.dash_end);
        dashBirthValue = findViewById(R.id.dash_birth_value);
        dashLifeLabel = findViewById(R.id.dash_life_label);
        dashLifeValue = findViewById(R.id.dash_life_value);
        dashUsedPercent = findViewById(R.id.dash_used_percent);
        dashUsedDetail = findViewById(R.id.dash_used_detail);
        dashRemainDetail = findViewById(R.id.dash_remain_detail);
        dashLifeSeek = findViewById(R.id.dash_life_seek);
        dashTrack = findViewById(R.id.dash_track);
        dashRingTrack = findViewById(R.id.dash_ring_track);
        dashDonut = findViewById(R.id.dash_donut);
        dashGrid = findViewById(R.id.dash_grid);
    }

    private void openSettings() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void renderAll() {
        LocalDate today = LocalDate.now();
        yearsTenths = LifeCalculator.clamp(ProfileStore.getLifeYearsTenths(this));
        LifeStats stats = LifeStats.compute(birthday, yearsTenths, today);

        dashDate.setText(today.format(HEAD_FORMAT));
        dashBirth.setText(birthday.format(MONTH_FORMAT));
        dashEnd.setText(stats.end.format(MONTH_FORMAT));
        dashBirthValue.setText(birthday.format(MONTH_FORMAT));
        dashLifeLabel.setText("预期寿命：" + LifeCalculator.yearsText(yearsTenths));
        dashLifeValue.setText(formatYears(yearsTenths));

        dashUsedPercent.setText(String.format(Locale.CHINA, "%.1f%%", stats.usedRatio * 100f));
        dashUsedDetail.setText(periodText(stats.elapsedPeriod) + "（" + group(stats.elapsedDays) + " 天）");
        dashRemainDetail.setText(periodText(stats.remainPeriod) + "（" + group(stats.remainDays) + " 天）");

        dashDonut.setUsedRatio(stats.usedRatio);
        dashTrack.setRatio(stats.usedRatio);
        dashRingTrack.setRatio(stats.usedRatio);

        seekReady = false;
        dashLifeSeek.setProgress(yearsTenths - SEEK_MIN_TENTHS);
        seekReady = true;

        fillStats(stats);
        renderClock();
    }

    private void fillStats(LifeStats stats) {
        LayoutInflater inflater = LayoutInflater.from(this);
        dashGrid.removeAllViews();
        int margin = dp(4);
        for (LifeStats.Stat stat : stats.stats) {
            View card = inflater.inflate(R.layout.item_stat, dashGrid, false);
            ((TextView) card.findViewById(R.id.stat_emoji)).setText(stat.emoji);
            ((TextView) card.findViewById(R.id.stat_value)).setText(stat.value);
            ((TextView) card.findViewById(R.id.stat_label)).setText(stat.label);
            TextView percent = card.findViewById(R.id.stat_percent);
            percent.setText(stat.percent);
            percent.setTextColor(stat.color);

            DonutView ring = card.findViewById(R.id.stat_ring);
            ring.setColors(stat.color, getColor(R.color.dash_track));
            ring.setUsedRatio(stats.remainRatio);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(margin, margin, margin, margin);
            dashGrid.addView(card, params);
        }
    }

    private void renderClock() {
        LocalTime now = LocalTime.now();
        int remain = 24 * 3600 - (now.getHour() * 3600 + now.getMinute() * 60 + now.getSecond());
        int hours = remain / 3600;
        int minutes = (remain % 3600) / 60;
        int seconds = remain % 60;
        dashClock.setText(String.format(Locale.CHINA, "%02d:%02d:%02d", hours, minutes, seconds));
    }

    private void pickBirthYearMonth() {
        int currentYear = birthday.getYear();
        int currentMonth = birthday.getMonthValue();

        NumberPicker yearPicker = new NumberPicker(this);
        yearPicker.setMinValue(1900);
        yearPicker.setMaxValue(LocalDate.now().getYear());
        yearPicker.setWrapSelectorWheel(false);
        yearPicker.setValue(currentYear);

        NumberPicker monthPicker = new NumberPicker(this);
        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setWrapSelectorWheel(false);
        monthPicker.setValue(currentMonth);
        monthPicker.setFormatter(value -> value + " 月");

        LinearLayout holder = new LinearLayout(this);
        holder.setGravity(Gravity.CENTER);
        int side = dp(28);
        holder.setPadding(side, dp(8), side, 0);
        holder.addView(yearPicker, new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        holder.addView(monthPicker, new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        new AlertDialog.Builder(this)
                .setTitle("出生年月")
                .setMessage("出生当天以之前设置的日期为准，月份变短时自动取当月最后一天。")
                .setView(holder)
                .setPositiveButton("确定", (dialog, which) -> {
                    LocalDate picked = LocalDate.of(
                            yearPicker.getValue(), monthPicker.getValue(),
                            Math.min(birthday.getDayOfMonth(),
                                    LocalDate.of(yearPicker.getValue(), monthPicker.getValue(), 1)
                                            .lengthOfMonth()));
                    if (picked.isAfter(LocalDate.now())) {
                        Toast.makeText(this, "出生年月不能晚于今天", Toast.LENGTH_LONG).show();
                        return;
                    }
                    ProfileStore.setBirthday(this, picked);
                    birthday = picked;
                    renderAll();
                    Widgets.refreshAll(this);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private int tenthsOf(int progress) {
        return SEEK_MIN_TENTHS + progress;
    }

    private String formatYears(int tenths) {
        return String.format(Locale.CHINA, "%.1f", tenths / 10.0);
    }

    private String periodText(Period period) {
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

    private String group(long value) {
        return NumberFormat.getIntegerInstance(Locale.CHINA).format(value);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
