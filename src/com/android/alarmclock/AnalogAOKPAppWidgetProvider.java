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

package com.android.alarmclock;

import com.android.deskclock.AlarmClock;
import com.android.deskclock.R;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RemoteViews;

/**
 * Simple widget to show analog clock.
 */
public class AnalogAOKPAppWidgetProvider extends AppWidgetProvider {
    private static final boolean DEBUG = false;

    static final String TAG = "AnalogAOKPAppWidgetProvider";

    static String[] clockStyleDrawables;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if (DEBUG) Log.d(TAG, "onUpdate");

        final int N = appWidgetIds.length;

        clockStyleDrawables = context.getResources().getStringArray(R.array.aokp_clock_backgrounds_drawable);

        for (int i=0; i<N; i++) {
            int selectedPos = AnalogAOKPAppWidgetConfigure.loadClockPref(context, appWidgetIds[i]);

            int resID = context.getResources().getIdentifier(clockStyleDrawables[selectedPos], "drawable",
                context.getPackageName());

            updateAppWidget(context, appWidgetManager, appWidgetIds[i], resID);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        if (DEBUG) Log.d(TAG, "onDeleted");

        final int N = appWidgetIds.length;
        for (int i=0; i<N; i++) {
            AnalogAOKPAppWidgetConfigure.deleteClockPref(context, appWidgetIds[i]);
        }
    }

    @Override
    public void onEnabled(Context context) {
        if (DEBUG) Log.d(TAG, "onEnabled");
    }

    @Override
    public void onDisabled(Context context) {
        if (DEBUG) Log.d(TAG, "onDisabled");
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId, int resID) {
        if (DEBUG) Log.d(TAG, "updateAppWidget appWidgetId=" + appWidgetId);

        RemoteViews views = new RemoteViews(context.getPackageName(),
            R.layout.analog_aokp_appwidget);

        views.setOnClickPendingIntent(R.id.analog_aokp_appwidget,
                PendingIntent.getActivity(context, 0,
                    new Intent(context, AlarmClock.class), 0));

        if (resID != 0) {
            views.setInt(R.id.imageViewClockDial, "setImageResource", resID);
        }

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }


}

