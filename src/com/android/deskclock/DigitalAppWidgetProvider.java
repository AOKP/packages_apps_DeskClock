/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.deskclock;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Simple widget to show digital clock.
 */
public class DigitalAppWidgetProvider extends AppWidgetProvider {

    static final String TAG = "DigitalAppWidgetProvider";
    private static final boolean DEBUG = true;

    @Override
    public void onDisabled(Context context) {

        if (DEBUG)
            Log.d(TAG, "ACTION_APPWIDGET_DISABLED");

        // Build the intent to call the service
        Intent intent = new Intent(context.getApplicationContext(),
                DigitalAppWidgetService.class);

        // Update the widgets via the service
        context.stopService(intent);

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
        if (DEBUG)
            Log.d(TAG, "ACTION_APPWIDGET_UPDATE");

        RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.digital_appwidget);

        AppWidgetManager gm = AppWidgetManager.getInstance(context);
        gm.updateAppWidget(appWidgetIds, views);
        
        // Get all ids
        ComponentName thisWidget = new ComponentName(context,
                DigitalAppWidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        // Build the intent to call the service
        Intent intent = new Intent(context.getApplicationContext(),
                DigitalAppWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);

        // Update the widgets via the service
        context.startService(intent);

    }

}
