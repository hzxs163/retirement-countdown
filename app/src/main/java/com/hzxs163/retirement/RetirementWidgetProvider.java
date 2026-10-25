package com.hzxs163.retirement;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;

public class RetirementWidgetProvider extends AppWidgetProvider {

    public static void refresh(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName component = new ComponentName(context, RetirementWidgetProvider.class);
        int[] ids = manager.getAppWidgetIds(component);
        for (int id : ids) {
            manager.updateAppWidget(id, WidgetRenderer.build(context));
        }
        if (ids.length > 0) {
            RefreshScheduler.scheduleNext(context);
        } else {
            RefreshScheduler.cancel(context);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int id : appWidgetIds) {
            appWidgetManager.updateAppWidget(id, WidgetRenderer.build(context));
        }
        if (appWidgetIds.length > 0) {
            RefreshScheduler.scheduleNext(context);
        }
    }

    @Override
    public void onEnabled(Context context) {
        RefreshScheduler.scheduleNext(context);
    }

    @Override
    public void onDisabled(Context context) {
        RefreshScheduler.cancel(context);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        appWidgetManager.updateAppWidget(appWidgetId, WidgetRenderer.build(context));
    }
}
