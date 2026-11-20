package com.hzxs163.retirement;

import android.content.Context;

public final class Widgets {

    private Widgets() {
    }

    public static void refreshAll(Context context) {
        RetirementWidgetProvider.refresh(context);
        DashboardWidgetProvider.refresh(context);
    }
}
