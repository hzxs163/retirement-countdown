package com.hzxs163.retirement;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;

public class DashboardWidgetProvider extends AppWidgetProvider {

    public static void refresh(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName component = new ComponentName(context, DashboardWidgetProvider.class);
        int[] ids = manager.getAppWidgetIds(component);
        for (int id : ids) {
            manager.updateAppWidget(id, DashboardWidgetRenderer.build(context, widthOf(manager, id)));
        }
        RefreshScheduler.sync(context);
    }

    static int widthOf(AppWidgetManager manager, int widgetId) {
        Bundle options = manager.getAppWidgetOptions(widgetId);
        int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0);
        if (width <= 0) {
            width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 0);
        }
        return width > 0 ? width : 340;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int id : appWidgetIds) {
            appWidgetManager.updateAppWidget(id, DashboardWidgetRenderer.build(context, widthOf(appWidgetManager, id)));
        }
        RefreshScheduler.sync(context);
    }

    @Override
    public void onEnabled(Context context) {
        RefreshScheduler.sync(context);
    }

    @Override
    public void onDisabled(Context context) {
        RefreshScheduler.sync(context);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        appWidgetManager.updateAppWidget(appWidgetId,
                DashboardWidgetRenderer.build(context, widthOf(appWidgetManager, appWidgetId)));
    }
}
