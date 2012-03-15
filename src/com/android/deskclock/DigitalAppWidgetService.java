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

import java.io.File;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;

import com.android.deskclock.R;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextPaint;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

/**
 * Simple widget to show digital clock.
 */
public class DigitalAppWidgetService extends Service {

    static final String TAG = "DigitalAppWidgetService";

    private static final String SYSTEM = "/system/fonts/";
    private static final String SYSTEM_FONT_TIME_BACKGROUND = SYSTEM
            + "AndroidClock.ttf";

    private static final boolean DEBUG = false;
    private final static String M12 = "h:mm";
    private Calendar mCalendar;
    private String mDateFormat;
    private String mFormat;
    private String mAmString, mPmString;
    private float densityMultiplier;
    private int density;
    private final Handler mHandler = new Handler();
    Typeface clock;
    TextPaint paint;

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (DEBUG)
                Log.d(TAG, "Intent received: " + intent);

            if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                mCalendar = Calendar.getInstance();
            }
            // Post a runnable to avoid blocking the broadcast.
            mHandler.post(new Runnable() {
                public void run() {
                    updateWidget();
                }
            });
        }
    };

    @Override
    public void onCreate() {
        mCalendar = Calendar.getInstance();
        densityMultiplier = this.getResources().getDisplayMetrics().density;

        density = this.getResources().getDisplayMetrics().densityDpi;

        paint = new TextPaint();

        File f = new File(SYSTEM_FONT_TIME_BACKGROUND);
        if (f.exists()) {
            clock = Typeface.createFromFile(SYSTEM_FONT_TIME_BACKGROUND);
            paint.setTypeface(clock);
        }

        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
        paint.setFilterBitmap(true);
        paint.setLinearText(true);
        paint.density = densityMultiplier;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(getResources().getColor(R.color.time_text_color));
        paint.setTextSize((int) (92 * densityMultiplier));
        paint.setTextAlign(Align.RIGHT);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (DEBUG)
            Log.d(TAG, "Service Started");

        String[] ampm = new DateFormatSymbols().getAmPmStrings();
        mAmString = ampm[0];
        mPmString = ampm[1];

        mDateFormat = getString(R.string.full_wday_month_day_no_year);

        updateWidget();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);

        this.registerReceiver(mIntentReceiver, filter);

        return START_STICKY;

    }

    @Override
    public void onDestroy() {

        if (DEBUG)
            Log.d(TAG, "Service Destroyed");

        this.unregisterReceiver(mIntentReceiver);

        this.stopSelf();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    private void updateWidget() {
        RemoteViews views = new RemoteViews(this.getApplicationContext()
                .getPackageName(), R.layout.digital_appwidget);

        mFormat = Alarms.get24HourMode(this.getApplicationContext()) ? Alarms.M24
                : M12;

        mCalendar.setTimeInMillis(System.currentTimeMillis());

        final CharSequence newTime = DateFormat.format(mFormat, mCalendar);

        if (DEBUG)
            Log.d(TAG, "new time: " + newTime);

        final Date now = new Date();

        views.setTextViewText(R.id.date, DateFormat.format(mDateFormat, now));

        views.setImageViewBitmap(R.id.timeDisplayImage,
                createTimeBitmap(newTime));

        views.setTextViewText(R.id.am_pm,
                mCalendar.get(Calendar.AM_PM) == 0 ? mAmString : mPmString);

        views.setViewVisibility(R.id.am_pm, mFormat == M12 ? View.VISIBLE
                : View.GONE);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
                .getApplicationContext());

        ComponentName thisWidget = new ComponentName(getApplicationContext(),
                DigitalAppWidgetProvider.class);

        appWidgetManager.updateAppWidget(thisWidget, views);
    }

    public Bitmap createTimeBitmap(CharSequence time) {

        Rect bounds = new Rect();
        paint.getTextBounds("00:00", 0, 5, bounds);

        Bitmap myBitmap = Bitmap.createBitmap(bounds.width() + (int) (2 * densityMultiplier),
                bounds.height() + (int) (2 * densityMultiplier), Bitmap.Config.ARGB_8888);

        // Bitmap myBitmap = Bitmap.createBitmap((int) (240 *
        // densityMultiplier),
        // (int) (54 * densityMultiplier), Bitmap.Config.ARGB_8888);

        if (DEBUG)
            myBitmap.eraseColor(Color.DKGRAY);

        if (DEBUG) {
            Log.d(TAG, "density: " + density);
            Log.d(TAG, "densityMultiplier: " + densityMultiplier);
            Log.d(TAG, "myBitmap.getDensity: " + myBitmap.getDensity());
            Log.d(TAG, "myBitmap.getHeight: " + myBitmap.getHeight());
            Log.d(TAG, "myBitmap.getWidth: " + myBitmap.getWidth());
            Log.d(TAG,
                    "myBitmap.getScaledHeight: "
                            + myBitmap.getScaledHeight(this.getResources().getDisplayMetrics()));
            Log.d(TAG,
                    "myBitmap.getScaledWidth: "
                            + myBitmap.getScaledWidth(this.getResources().getDisplayMetrics()));
        }

        Canvas myCanvas = new Canvas(myBitmap);

        myCanvas.drawText(time, 0, time.length(), bounds.width(),
                bounds.height(), paint);

        return myBitmap;
    }

}
