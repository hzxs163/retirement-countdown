package com.hzxs163.retirement;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.view.View;
import android.widget.RemoteViews;

import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class WidgetRenderer {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy年M月d日", Locale.CHINA);

    /** 金句：300 条（240 松弛风 + 60 舒服过日子，每 5 条插 1 条暖心），按日期轮换起点 */
    private static final String[] QUOTES = {
            "今天的我，暂无上进心。",
            "明天再说，明天也这么说。",
            "万事开头难，那就别开。",
            "计划赶不上变化，干脆不计划。",
            "日子是过以后，不是过从前。",
            "我的信条：能拖就拖。",
            "即将开始，永远即将。",
            "别问进度，问就是快了。",
            "不着急，天塌不下来。",
            "心宽一寸，路宽一丈。",
            "事情做不完，地球照样转。",
            "万事俱备，只欠不想干。",
            "等一下再说，等一下再说。",
            "进度条卡在 99%。",
            "少一点计较，多一点坦然。",
            "今日待办：待定。",
            "不是不做，是还没到点。",
            "先放一放，放个三天。",
            "计划写在便签上就够了。",
            "不为难自己，是最大的清醒。",
            "我做事讲究一个随缘。",
            "截止日期也是一种建议。",
            "拖延症已进化为慢性从容。",
            "这件事明天会更简单吗？",
            "慢慢来，比较快。",
            "我先想想，想到明年。",
            "别催了，越催越慢。",
            "起步晚，但退得早。",
            "我只负责想，不负责做。",
            "一日三餐，四季安稳。",
            "今日状态：蓄力中。",
            "缓慢是一种策略。",
            "我的截止日期是随缘。",
            "先喝口水，再想想。",
            "有茶有饭，便是好日。",
            "待办清单已经泛黄了。",
            "任务都在，人不在。",
            "明天是更好的开始。",
            "人生很长，不差今天。",
            "饭要趁热吃，觉要按时睡。",
            "我认真考虑过，决定再拖拖。",
            "一切都还来得及——大概。",
            "先坐一会儿。",
            "缓一缓，天不会塌。",
            "今天也辛苦了，歇会儿吧。",
            "我把急事都留给了明天。",
            "今日 KPI：准时下班。",
            "完成度：构思完毕。",
            "我的速度，叫刚刚好。",
            "把日子过成喜欢的样子。",
            "事情不急，是我急不起来。",
            "慢慢来，反正来不及了。",
            "开工前先热身，热身一下午。",
            "明天的事，明天的人做。",
            "好好睡觉，好好吃饭。",
            "我是个讲究时机的人。",
            "人生苦短，先睡一觉。",
            "世界那么大，床那么舒服。",
            "与其内耗，不如睡觉。",
            "天亮了，又是新的一天。",
            "闹钟叫不醒我，床可以。",
            "睡吧，天大的事明早说。",
            "中午这一个觉，不能少。",
            "我的充电口在床上。",
            "该来的会来，该走的会走。",
            "睡眠是最高效的努力。",
            "早睡是明天的事。",
            "一觉醒来再说。",
            "补个觉，比补什么都有用。",
            "心里有光，日子就不暗。",
            "枕头比会议重要。",
            "梦里什么都有。",
            "困了就是身体在请假。",
            "小睡一刻，元气半天。",
            "慢下来，才尝得出味道。",
            "被子是我的安全区。",
            "起床需要五分钟预热。",
            "今天不宜早起。",
            "闭眼五分钟，清醒两小时。",
            "少熬夜，多晒太阳。",
            "睡觉不丢人。",
            "我的枕头有治愈功能。",
            "眯一会儿，世界会更友好。",
            "睡了就不焦虑了。",
            "身体健康是一切的本钱。",
            "熬夜伤身，还是睡吧。",
            "床是家里最好的家具。",
            "睡到自然醒是种福气。",
            "午睡是成年人的充电宝。",
            "不慌不忙，来得及。",
            "天塌了也先睡完这觉。",
            "醒着就要干活，所以睡。",
            "睡眠公平对待每个人。",
            "我在养精蓄锐，别打扰。",
            "允许自己慢一点。",
            "睡觉不影响世界运转。",
            "一觉睡到日上三竿。",
            "闭眼就是休息。",
            "不困也想歇着。",
            "每天都值得被认真过。",
            "睡眠时间不算浪费。",
            "醒来再看，也许就没事了。",
            "我的爱好：睡觉。",
            "梦里不用打卡。",
            "生活是过给自己看的。",
            "睡饱了才有力气发愁。",
            "我不是懒，我是省电模式。",
            "电量只剩 5%，请勿打扰。",
            "我已经很努力了，努力地歇着。",
            "累了就歇，不用硬撑。",
            "力气省下来，留给吃饭。",
            "认真你就输了，我不认真。",
            "逃避虽可耻，但真的有用。",
            "走慢点，风景才看得清。",
            "该休息时就休息吧。",
            "停下来，也是一种前进。",
            "松弛是我的天赋。",
            "我很好，就是不想动。",
            "摸鱼也需要天赋，我天生会。",
            "善待自己，比什么都重要。",
            "我的核心竞争力：不着急。",
            "我的理想：今天到此为止。",
            "上班只是为了下班。",
            "少想一点，多睡一点。",
            "有盼头，就不算难。",
            "万事不急，先喝口茶。",
            "屋里比办公室舒服。",
            "跑不动了，我改成散步。",
            "工作会等人，觉不会。",
            "一步一步，总会到。",
            "先照顾好自己，别的再说。",
            "今日目标：活着就行。",
            "该吃吃该睡睡，明天说。",
            "我不赶时间，时间别赶我。",
            "云淡风轻，也是一种活法。",
            "任务清单更像愿望清单。",
            "今天没力气，明天再说。",
            "慢点没关系，反正都在排队。",
            "别拿奋斗绑架我，我抱紧枕头。",
            "抬头看看天，缓一缓。",
            "心态放平，工资照领。",
            "今天也成功地什么都没做。",
            "不争不抢，饭点准时。",
            "我擅长把事情变小。",
            "一切都在慢慢变好。",
            "我的天赋是低功耗运行。",
            "低配人生，高配睡眠。",
            "我不焦虑，我只是慢。",
            "世界很吵，我选择静音。",
            "给自己留一点空。",
            "我选择在今天放过自己。",
            "省下的力气都是自己的。",
            "我对效率过敏。",
            "忙是别人的事。",
            "睡个好觉，明天再说。",
            "我的人生是慢放版。",
            "别忙了，头发会掉。",
            "与其焦虑，不如眯一会儿。",
            "不着急，慢慢来。",
            "心静了，事就小了。",
            "争不过就绕道走。",
            "别人的赛道，我不参加。",
            "我不是慢，我是不赶。",
            "赢了又如何，输了又怎样。",
            "日子还长，不必着急。",
            "让子弹飞，让我歇着。",
            "我不比，我只看自己。",
            "钱少点，气顺点。",
            "事事看淡，饭饭吃饱。",
            "平安就好，其他随缘。",
            "心里没事，就是好日子。",
            "看开点，天都宽了。",
            "我不争第一，我争心安。",
            "退一步，海阔天空。",
            "把压力放一放。",
            "只跟自己比，也懒得比。",
            "不计较的人生比较轻。",
            "输赢之外还有天气。",
            "我的野心不大，够用就行。",
            "好好活着，就是赢。",
            "慢慢走，也算在走。",
            "不攀比，日子就轻了。",
            "少生气，多吃饭。",
            "我脾气好，因为懒得气。",
            "谁的生活都不容易，慢慢来。",
            "天大地大，睡觉最大。",
            "一切都会过去，包括这事。",
            "我不卷，我散步。",
            "平和是种高级懒。",
            "今天也值得一杯热茶。",
            "我不争，我让。",
            "想开一点，饭香一点。",
            "日子是自己的，不用演。",
            "心宽，路自然宽。",
            "生活不必满分，及格就好。",
            "不较劲，省力。",
            "输了就输了，饭还是要吃。",
            "别跟自己过不去。",
            "我不在意，所以我不累。",
            "别把日子过成任务。",
            "打卡是一种仪式。",
            "周一离周五最远。",
            "工位坐久了会生根。",
            "会议是时间的黑洞。",
            "允许一切发生。",
            "我的效率巅峰在周五下午。",
            "摸鱼要摸得心安理得。",
            "上班路上最自由。",
            "咖啡续命，摸鱼续魂。",
            "笑一笑，没什么大不了。",
            "我热爱工位——坐着的部分。",
            "消息回得慢，是种态度。",
            "我在思考，别打扰。",
            "加班是别人的浪漫。",
            "好天气要记得出门。",
            "到点就走，天经地义。",
            "我的桌面比我还清闲。",
            "工作是做不完的，先吃饭。",
            "领导看不见时，我很自由。",
            "有些事，明天就好了。",
            "我的午休神圣不可侵犯。",
            "今天不忙，明天再忙。",
            "键盘敲得响不代表在忙。",
            "我上班是为了那杯免费的水。",
            "少一点期待，多一点自在。",
            "下班铃是最好听的音乐。",
            "把工作留给明天的人。",
            "我不影响进度，因为没进度。",
            "一开会我就困。",
            "过得舒服，比过得体面重要。",
            "我的工位是第二个床。",
            "上班像开盲盒，每天一样。",
            "我在等下班，很专注。",
            "邮件明天再看。",
            "心不累，日子就不累。",
            "工资到账那天最努力。",
            "我对加班免疫。",
            "办公室的空气不养人。",
            "我的下班时间是信仰。",
            "歇够了再出发。",
            "摸鱼时我在思考人生。",
            "工位和床之间，我选后者。",
            "我上班只是路过。",
            "今天的会，明天的愁。",
            "平常心，是难得的福气。",
            "准时下班是一种能力。",
            "我做事讲究节奏——慢的那种。",
            "请勿在下班前找我。",
            "我请假时最精神。",
            "过好今天，就是全部。",
            "吃饭是一天里最正经的事。",
            "早中晚都要吃好。",
            "睡前别生气。",
            "辛苦了，先吃点东西。",
            "慢一点，也到了。",
            "吃好一顿，胜过努力一天。",
            "汤要热，人要松。",
            "一杯茶的时间，不还。",
            "吃饱了才有力气想别的。",
            "想开点，天很大。",
            "外卖到了先吃再说。",
            "菜要下饭，心要放宽。",
            "晚饭吃什么，是今天的大事。",
            "我的幸福很简单：热汤。",
            "生活总会给你一点甜。",
            "饭点没人能拦我。",
            "一荤一素，也很满足。",
            "碗里的事最重要。",
            "泡面也要摆个盘。",
            "该吃吃该喝喝，别想太多。",
            "慢慢吃，容易饱。",
            "一杯奶茶，天下太平。",
            "吃水果的时候最像样。",
            "锅是热的，心是软的。",
            "心情好比什么都强。",
            "电饭煲响的那一刻很治愈。",
            "饭桌上没有烦心事。",
            "今天的菜，咸淡正好。",
            "做饭慢，吃饭更慢。",
            "别急，好日子都在路上。",
            "零食柜是我的军火库。",
            "先吃饭，再烦恼。",
            "一顿热的，胜过十句鼓励。",
            "我的快乐按顿计算。",
            "时间会解决大部分事。",
            "冰箱满了，心就稳了。",
            "好好吃饭就是好好生活。",
            "买菜也是种运动。",
            "早饭吃点好的。",
            "允许难过，也允许放下。",
            "下午三点，茶歇时间。",
            "我不需要意义，我需要面。",
            "慢火煲出来的才香。",
            "一碗面解决所有情绪。",
            "今天也要好好过。",
            "饭后散步，天下第一。",
            "吃是自己对自己的好。",
            "今天也有好好吃饭。",
            "活着就是一起吃很多顿饭。",
            "活着，就还有机会。",
    };

    private static final int[] QUOTE_VIEWS = {
            R.id.widget_quote_1, R.id.widget_quote_2, R.id.widget_quote_3,
            R.id.widget_quote_4, R.id.widget_quote_5, R.id.widget_quote_6,
    };

    private WidgetRenderer() {
    }

    private static void applyQuotes(RemoteViews views) {
        applyQuotes(views, QUOTE_VIEWS);
    }

    public static void applyQuotes(RemoteViews views, int[] quoteViewIds) {
        int offset = LocalDate.now().getDayOfYear() % QUOTES.length;
        for (int i = 0; i < quoteViewIds.length; i++) {
            views.setTextViewText(quoteViewIds[i], QUOTES[(offset + i) % QUOTES.length]);
        }
    }

    public static RemoteViews build(Context context) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_retirement);
        PendingIntent openApp = openAppIntent(context);
        views.setOnClickPendingIntent(R.id.widget_root, openApp);
        applyQuotes(views);

        LocalDate birthday = ProfileStore.getBirthday(context);
        if (birthday == null) {
            views.setTextViewText(R.id.widget_label, "退休倒计时");
            views.setTextViewText(R.id.widget_badge, "未设置");
            views.setTextViewText(R.id.widget_days, "--");
            views.setTextViewText(R.id.widget_unit, "天");
            views.setTextViewText(R.id.widget_line1, "点一下，设置生日");
            views.setTextViewText(R.id.widget_line2, "自动计算退休日期");
            views.setViewVisibility(R.id.life_column, View.GONE);
            views.setViewVisibility(R.id.widget_divider, View.GONE);
            views.setTextViewText(R.id.widget_life_label, "");
            views.setTextViewText(R.id.widget_life_prefix, "");
            views.setContentDescription(R.id.widget_root, "退休倒计时，还没有设置生日。点击打开应用设置。");
            return views;
        }

        int category = ProfileStore.getCategory(context);
        int customAge = ProfileStore.getCustomAge(context);
        RetirementCalculator.Result result =
                RetirementCalculator.calculate(birthday, category, customAge, LocalDate.now());

        long shownDays = result.retired ? result.daysRetired : result.daysRemaining;
        views.setTextViewText(R.id.widget_days, formatNumber(shownDays));
        views.setTextViewText(R.id.widget_unit, "天");
        views.setTextViewText(R.id.widget_badge, categoryBadge(category, customAge));

        String label;
        if (result.retired) {
            label = "已经退休";
        } else if (result.days == 0) {
            label = "今天退休";
        } else {
            label = "距退休还有";
        }
        views.setTextViewText(R.id.widget_label, label);

        String dateLine;
        if (result.days <= 0) {
            dateLine = "退休日 " + result.retirementDate.format(DATE_FORMAT);
        } else {
            dateLine = "预计 " + result.retirementDate.format(DATE_FORMAT) + " 退休";
        }
        views.setTextViewText(R.id.widget_line1, dateLine);

        String subLine;
        if (result.retired) {
            subLine = "祝退休生活愉快";
        } else if (result.days == 0) {
            subLine = "从今天起，时间归你";
        } else {
            subLine = "还需 " + remainingText(result.remaining)
                    + " · " + ageText(result) + "退休";
        }
        views.setTextViewText(R.id.widget_line2, subLine);

        LifeCalculator.Result life = null;
        if (ProfileStore.isLifeOnWidget(context)) {
            life = LifeCalculator.calculate(
                    birthday, ProfileStore.getLifeYearsTenths(context), LocalDate.now());
            if (life.days > 0) {
                views.setTextViewText(R.id.widget_life_label, "余生还有");
                views.setTextViewText(R.id.widget_life_prefix, "剩余");
                views.setTextViewText(R.id.widget_life_days, formatNumber(life.daysRemaining));
                views.setTextViewText(R.id.widget_life_unit, "天");
                views.setViewVisibility(R.id.widget_life_chrono, View.VISIBLE);
                // 秒数交给桌面的 Chronometer 自己走，App 不常驻。
                // 给的是「剩余总时长」，所以显示的是总小时数 h:mm:ss
                long totalMillis = Duration.between(
                        LocalDateTime.now(), life.endDate.atStartOfDay()).toMillis();
                if (totalMillis < 0) {
                    totalMillis = 0;
                }
                views.setChronometer(R.id.widget_life_chrono,
                        SystemClock.elapsedRealtime() + totalMillis, "%s", true);
                views.setChronometerCountDown(R.id.widget_life_chrono, true);
            } else {
                views.setTextViewText(R.id.widget_life_label, "已过终点");
                views.setTextViewText(R.id.widget_life_prefix, "");
                views.setTextViewText(R.id.widget_life_days, "");
                views.setTextViewText(R.id.widget_life_unit, "");
                views.setViewVisibility(R.id.widget_life_chrono, View.GONE);
            }
            views.setViewVisibility(R.id.life_column, View.VISIBLE);
            views.setViewVisibility(R.id.widget_divider, View.VISIBLE);
        } else {
            views.setViewVisibility(R.id.life_column, View.GONE);
            views.setViewVisibility(R.id.widget_divider, View.GONE);
            views.setTextViewText(R.id.widget_life_label, "");
            views.setTextViewText(R.id.widget_life_prefix, "");
        }

        String description;
        if (result.retired) {
            description = "退休倒计时，已经退休 " + formatNumber(result.daysRetired)
                    + " 天。点击打开生命仪表盘。";
        } else {
            description = "退休倒计时，距离退休还有 " + formatNumber(result.daysRemaining)
                    + " 天，预计 " + result.retirementDate.format(DATE_FORMAT) + " 退休。点击打开生命仪表盘。";
        }
        if (life != null && life.days > 0) {
            description += "按设定预期寿命，还剩 " + formatNumber(life.daysRemaining) + " 天。";
        }
        views.setContentDescription(R.id.widget_root, description);
        return views;
    }

    public static String categoryBadge(int category, int customAge) {
        switch (category) {
            case RetirementCalculator.CATEGORY_MALE:
                return "男职工";
            case RetirementCalculator.CATEGORY_FEMALE_55:
                return "女职工 · 55岁档";
            case RetirementCalculator.CATEGORY_FEMALE_50:
                return "女职工 · 50岁档";
            case RetirementCalculator.CATEGORY_CUSTOM:
                return "自定义 " + customAge + " 岁";
            default:
                return "男职工";
        }
    }

    public static String ageText(RetirementCalculator.Result result) {
        if (result.retirementAgeMonths == 0) {
            return result.retirementAgeYears + " 岁";
        }
        return result.retirementAgeYears + " 岁 " + result.retirementAgeMonths + " 个月";
    }

    public static String remainingText(Period period) {
        if (period.getYears() > 0) {
            if (period.getMonths() == 0) {
                return period.getYears() + " 年";
            }
            return period.getYears() + " 年 " + period.getMonths() + " 个月";
        }
        if (period.getMonths() > 0) {
            if (period.getDays() == 0) {
                return period.getMonths() + " 个月";
            }
            return period.getMonths() + " 个月 " + period.getDays() + " 天";
        }
        return period.getDays() + " 天";
    }

    private static String formatNumber(long value) {
        return NumberFormat.getIntegerInstance(Locale.CHINA).format(value);
    }

    private static PendingIntent openAppIntent(Context context) {
        Intent intent = new Intent(context, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(
                context,
                2001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}
