package com.hzxs163.retirement;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String ACTION_DAILY_ALARM = "com.hzxs163.retirement.action.DAILY_ALARM";

    @Override
    public void onReceive(Context context, Intent intent) {
        RetirementWidgetProvider.refresh(context);
    }
}
