package com.hzxs163.retirement;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy年M月d日", Locale.CHINA);

    private TextView heroLabel;
    private TextView heroDays;
    private TextView heroBadge;
    private TextView heroDate;
    private TextView heroSub;
    private TextView valueBirthday;
    private TextView valueCategory;
    private TextView valueCustomAge;
    private View customAgeBlock;
    private TextView detailAge;
    private TextView detailDelay;
    private TextView detailRetireDate;
    private TextView valueNextAlarm;
    private TextView valueExactAlarm;
    private TextView valueBattery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
        bindActions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        RetirementWidgetProvider.refresh(this);
        render();
    }

    private void bindViews() {
        heroLabel = findViewById(R.id.hero_label);
        heroDays = findViewById(R.id.hero_days);
        heroBadge = findViewById(R.id.hero_badge);
        heroDate = findViewById(R.id.hero_date);
        heroSub = findViewById(R.id.hero_sub);
        valueBirthday = findViewById(R.id.value_birthday);
        valueCategory = findViewById(R.id.value_category);
        valueCustomAge = findViewById(R.id.value_custom_age);
        customAgeBlock = findViewById(R.id.custom_age_block);
        detailAge = findViewById(R.id.detail_age);
        detailDelay = findViewById(R.id.detail_delay);
        detailRetireDate = findViewById(R.id.detail_retire_date);
        valueNextAlarm = findViewById(R.id.value_next_alarm);
        valueExactAlarm = findViewById(R.id.value_exact_alarm);
        valueBattery = findViewById(R.id.value_battery);
    }

    private void bindActions() {
        findViewById(R.id.row_birthday).setOnClickListener(v -> pickBirthday());
        findViewById(R.id.row_category).setOnClickListener(v -> pickCategory());
        findViewById(R.id.row_custom_age).setOnClickListener(v -> pickCustomAge());
        findViewById(R.id.row_exact_alarm).setOnClickListener(v -> requestExactAlarm());
        findViewById(R.id.row_battery).setOnClickListener(v -> openBatterySettings());
        findViewById(R.id.row_autostart).setOnClickListener(v -> openAutostartSettings());
        findViewById(R.id.btn_pin).setOnClickListener(v -> pinWidget());
        findViewById(R.id.btn_refresh).setOnClickListener(v -> refreshWidgets());
        findViewById(R.id.tv_help).setOnClickListener(v -> showHelp());
    }

    private void render() {
        LocalDate birthday = ProfileStore.getBirthday(this);
        int category = ProfileStore.getCategory(this);
        int customAge = ProfileStore.getCustomAge(this);

        valueCategory.setText(categoryTitle(category, customAge));
        valueCustomAge.setText(customAge + " 岁");
        customAgeBlock.setVisibility(
                category == RetirementCalculator.CATEGORY_CUSTOM ? View.VISIBLE : View.GONE);

        if (birthday == null) {
            heroLabel.setText("先设置生日");
            heroDays.setText("--");
            heroBadge.setText("未设置");
            heroDate.setText("点下方“出生日期”开始计算");
            heroSub.setText("数据只保存在本机");
            valueBirthday.setText(R.string.value_not_set);
            detailAge.setText("—");
            detailDelay.setText("—");
            detailRetireDate.setText("—");
        } else {
            RetirementCalculator.Result result = RetirementCalculator.calculate(
                    birthday, category, customAge, LocalDate.now());
            long shownDays = result.retired ? result.daysRetired : result.daysRemaining;

            heroDays.setText(NumberFormat.getIntegerInstance(Locale.CHINA).format(shownDays));
            heroBadge.setText(WidgetRenderer.categoryBadge(category, customAge));
            valueBirthday.setText(birthday.format(DATE_FORMAT));
            detailAge.setText(WidgetRenderer.ageText(result));
            if (category == RetirementCalculator.CATEGORY_CUSTOM) {
                detailDelay.setText("—");
            } else if (result.delayMonths == 0) {
                detailDelay.setText("不涉及");
            } else {
                detailDelay.setText(result.delayMonths + " 个月");
            }
            detailRetireDate.setText(result.retirementDate.format(DATE_FORMAT));

            if (result.retired) {
                heroLabel.setText("已经退休");
                heroDate.setText("退休日 " + result.retirementDate.format(DATE_FORMAT));
                heroSub.setText("祝退休生活愉快");
            } else if (result.days == 0) {
                heroLabel.setText("今天退休");
                heroDate.setText("退休日 " + result.retirementDate.format(DATE_FORMAT));
                heroSub.setText("从今天起，时间归你");
            } else {
                heroLabel.setText("距离退休还有");
                heroDate.setText("预计退休日 " + result.retirementDate.format(DATE_FORMAT));
                heroSub.setText("还需 " + WidgetRenderer.remainingText(result.remaining)
                        + " · 法定 " + WidgetRenderer.ageText(result));
            }
        }

        String next = RefreshScheduler.formatNext(this);
        valueNextAlarm.setText("未安排".equals(next) ? "添加小组件后自动安排" : next);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            boolean exact = RefreshScheduler.exactAlarmsAllowed(this);
            valueExactAlarm.setText(exact ? "已授权" : "未授权 · 点此开启");
            valueExactAlarm.setTextColor(getColor(exact ? R.color.ok : R.color.accent));
        } else {
            valueExactAlarm.setText("系统已支持");
            valueExactAlarm.setTextColor(getColor(R.color.ok));
        }

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        boolean ignoring = powerManager != null
                && powerManager.isIgnoringBatteryOptimizations(getPackageName());
        valueBattery.setText(ignoring ? "无限制" : "可能延迟 · 点此设置");
        valueBattery.setTextColor(getColor(ignoring ? R.color.ok : R.color.accent));
    }

    private String categoryTitle(int category, int customAge) {
        switch (category) {
            case RetirementCalculator.CATEGORY_MALE:
                return "男职工（原 60 岁）";
            case RetirementCalculator.CATEGORY_FEMALE_55:
                return "女职工（原 55 岁）";
            case RetirementCalculator.CATEGORY_FEMALE_50:
                return "女职工（原 50 岁）";
            case RetirementCalculator.CATEGORY_CUSTOM:
                return "自定义 " + customAge + " 岁";
            default:
                return "男职工（原 60 岁）";
        }
    }

    private void pickBirthday() {
        LocalDate current = ProfileStore.getBirthday(this);
        if (current == null) {
            current = LocalDate.of(1990, 1, 1);
        }
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            LocalDate picked = LocalDate.of(year, month + 1, dayOfMonth);
            if (picked.isAfter(LocalDate.now())) {
                toast("生日不能晚于今天");
                return;
            }
            ProfileStore.setBirthday(this, picked);
            render();
            RetirementWidgetProvider.refresh(this);
        }, current.getYear(), current.getMonthValue() - 1, current.getDayOfMonth());

        Calendar minDate = Calendar.getInstance();
        minDate.clear();
        minDate.set(1900, Calendar.JANUARY, 1);
        dialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private void pickCategory() {
        final String[] names = {
                "男职工（原法定 60 岁）",
                "女职工（原法定 55 岁）",
                "女职工（原法定 50 岁）",
                "自定义退休年龄"
        };
        int current = ProfileStore.getCategory(this);
        new AlertDialog.Builder(this)
                .setTitle("选择退休身份")
                .setSingleChoiceItems(names, current, (dialog, which) -> {
                    dialog.dismiss();
                    if (which == RetirementCalculator.CATEGORY_CUSTOM) {
                        pickCustomAge();
                    } else {
                        ProfileStore.setCategory(this, which);
                        render();
                        RetirementWidgetProvider.refresh(this);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void pickCustomAge() {
        NumberPicker picker = new NumberPicker(this);
        picker.setMinValue(40);
        picker.setMaxValue(70);
        picker.setWrapSelectorWheel(false);
        picker.setValue(Math.max(40, Math.min(70, ProfileStore.getCustomAge(this))));

        FrameLayout holder = new FrameLayout(this);
        int horizontalPadding = dp(24);
        holder.setPadding(horizontalPadding, dp(8), horizontalPadding, 0);
        holder.addView(picker, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        new AlertDialog.Builder(this)
                .setTitle("自定义退休年龄")
                .setMessage("按出生日期加上这个年龄，作为退休日。")
                .setView(holder)
                .setPositiveButton("确定", (dialog, which) -> {
                    ProfileStore.setCustomAge(this, picker.getValue());
                    ProfileStore.setCategory(this, RetirementCalculator.CATEGORY_CUSTOM);
                    render();
                    RetirementWidgetProvider.refresh(this);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void requestExactAlarm() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            toast("当前系统无需额外授权");
            render();
            return;
        }
        if (RefreshScheduler.exactAlarmsAllowed(this)) {
            toast("精确闹钟已授权");
            RetirementWidgetProvider.refresh(this);
            render();
            return;
        }
        try {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception exception) {
            openAppDetails();
        }
    }

    private void openBatterySettings() {
        if (startSafely(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))) {
            return;
        }
        openAppDetails();
    }

    private void openAutostartSettings() {
        Intent miui = new Intent();
        miui.setComponent(new ComponentName(
                "com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartManagementActivity"));
        if (startSafely(miui)) {
            return;
        }
        if (startSafely(new Intent("miui.intent.action.OP_AUTO_START"))) {
            return;
        }
        openAppDetails();
    }

    private void openAppDetails() {
        if (!startSafely(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + getPackageName())))) {
            toast("请到系统设置里找到“退休倒计时”");
        }
    }

    private boolean startSafely(Intent intent) {
        try {
            startActivity(intent);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private void pinWidget() {
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        if (!manager.isRequestPinAppWidgetSupported()) {
            showPinFallback();
            return;
        }
        ComponentName provider = new ComponentName(this, RetirementWidgetProvider.class);
        Intent successIntent = new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent success = PendingIntent.getActivity(
                this,
                3001,
                successIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        try {
            boolean requested = manager.requestPinAppWidget(provider, null, success);
            if (requested) {
                toast("在系统弹窗里确认添加到桌面或负一屏");
            } else {
                showPinFallback();
            }
        } catch (RuntimeException exception) {
            showPinFallback();
        }
    }

    private void showPinFallback() {
        new AlertDialog.Builder(this)
                .setTitle("手动添加小组件")
                .setMessage("长按桌面空白处 → 添加小部件 / 添加工具 → 搜索“退休倒计时”，拖到桌面。\n\n"
                        + "想放负一屏：滑到最左侧负一屏，点右上角“+”，在小部件中心搜索。\n\n"
                        + "不同 HyperOS / MIUI 版本对第三方“安卓小部件”的开放程度不同；如果负一屏找不到它，只能放主屏，任何第三方 App 都无法绕过。")
                .setPositiveButton("知道了", null)
                .show();
    }

    private void refreshWidgets() {
        int count = AppWidgetManager.getInstance(this)
                .getAppWidgetIds(new ComponentName(this, RetirementWidgetProvider.class)).length;
        RetirementWidgetProvider.refresh(this);
        render();
        toast(count == 0 ? "还没有添加小组件" : "小组件已刷新");
    }

    private void showHelp() {
        String help = "小米 / 红米（HyperOS、MIUI）添加：\n"
                + "1. 滑到最左侧的负一屏，点右上角“+”，进入小部件中心。\n"
                + "2. 搜索“退休倒计时”；如果推荐里没有，进入“全部应用 / 安卓小部件”分类。\n"
                + "3. 选中 4×2 卡片 → 添加到负一屏。\n\n"
                + "如果负一屏里找不到：\n"
                + "部分 HyperOS / MIUI 版本只允许系统卡片放负一屏，不开放第三方“安卓小部件”。这不是 App 权限问题，第三方应用无法绕过。可以改放主屏：\n"
                + "长按桌面空白处 → 添加小部件 → 全部应用 / 安卓小部件 → 退休倒计时。\n\n"
                + "每天自动刷新：\n"
                + "• 小组件每天 00:01 自动更新，不需要打开 App。\n"
                + "• 精确闹钟未授权时，系统可能推迟几分钟执行。\n"
                + "• 小米还需要允许“自启动”，并把省电策略设为“无限制”，否则后台刷新可能被系统冻结。";
        new AlertDialog.Builder(this)
                .setTitle("负一屏添加说明")
                .setMessage(help)
                .setPositiveButton("知道了", null)
                .show();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
