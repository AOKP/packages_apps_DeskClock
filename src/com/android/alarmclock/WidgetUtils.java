/*
 * Copyright (C) 2012 The Android Open Source Project
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

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.android.deskclock.R;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class WidgetUtils {
    static final String TAG = "WidgetUtils";

    public static void setClockSize(Context context, RemoteViews clock, float scale) {
        float fontSize = context.getResources().getDimension(R.dimen.widget_big_font_size);
        clock.setTextViewTextSize(
                R.id.the_clock1, TypedValue.COMPLEX_UNIT_PX, fontSize * scale);
        clock.setTextViewTextSize(
                R.id.the_clock2, TypedValue.COMPLEX_UNIT_PX, fontSize * scale);
    }

    public static void setClockColor(Context context, RemoteViews clock, SharedPreferences mPrefs) {
        int colorTime = mPrefs.getInt("digital_clock_time_color",
            context.getResources().getColor(R.color.clock_white));
        int colorDate = mPrefs.getInt("digital_clock_date_color",
            context.getResources().getColor(R.color.clock_white));
        int colorAlarm = mPrefs.getInt("digital_clock_alarm_color",
            context.getResources().getColor(R.color.clock_gray));

        clock.setTextColor(R.id.the_clock1, colorTime);
        clock.setTextColor(R.id.the_clock2, colorTime);
        clock.setTextColor(R.id.date, colorDate);
        clock.setTextColor(R.id.nextAlarm, colorAlarm);
    }

    // Calculate the scale factor of the fonts in the widget
    public static float getScaleRatio(Context context, Bundle options, int id) {
        if (options == null) {
            AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
            if (widgetManager == null) {
                // no manager , do no scaling
                return 1f;
            }
            options = widgetManager.getAppWidgetOptions(id);
        }
        if (options != null) {
            int minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            if (minWidth == 0) {
                // No data , do no scaling
                return 1f;
            }
            Resources res = context.getResources();
            float density = res.getDisplayMetrics().density;
            float ratio = (density * minWidth) / res.getDimension(R.dimen.min_digital_widget_width);
            // Check if the height could introduce a font size constraint
            int minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
            if (minHeight > 0 && (density * minHeight)
                    < res.getDimension(R.dimen.min_digital_widget_height)) {
                ratio = Math.min(ratio, getHeightScaleRatio(context, options, id));
            }
            return (ratio > 1) ? 1 : ratio;
        }
        return 1;
    }

    // Calculate the scale factor of the fonts in the list of  the widget using the widget height
    private static float getHeightScaleRatio(Context context, Bundle options, int id) {
        if (options == null) {
            AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
            if (widgetManager == null) {
                // no manager , do no scaling
                return 1f;
            }
            options = widgetManager.getAppWidgetOptions(id);
        }
        if (options != null) {
            int minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
            if (minHeight == 0) {
                // No data , do no scaling
                return 1f;
            }
            Resources res = context.getResources();
            float density = res.getDisplayMetrics().density;
            // Estimate height of date text box - 1.35 roughly approximates the text box padding
            float lblBox = 1.35f * res.getDimension(R.dimen.label_font_size);
            // Ensure divisor for ratio is positive number
            if (res.getDimension(R.dimen.min_digital_widget_height) - lblBox > 0) {
                float ratio = ((density * minHeight) - lblBox)
                        / (res.getDimension(R.dimen.min_digital_widget_height) - lblBox);
                return (ratio > 1) ? 1 : ratio;
            }
        }
        return 1;
    }


    // Decide if to show the list of world clock.
    // Check to see if the widget size is big enough, if it is return true.
    public static boolean showList(Context context, int id, float scale) {
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        if (widgetManager == null) {
            // no manager to make the calculation, show the list anyway
            return true;
        }
        Bundle options = widgetManager.getAppWidgetOptions(id);
        if (options == null) {
            // no data to make the calculation, show the list anyway
            return true;
        }
        Resources res = context.getResources();
        String whichHeight = res.getConfiguration().orientation ==
                Configuration.ORIENTATION_PORTRAIT
                ? AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT
                : AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT;
        int height = options.getInt(whichHeight);
        if (height == 0) {
            // no data to make the calculation, show the list anyway
            return true;
        }
        float density = res.getDisplayMetrics().density;
        // Estimate height of date text box
        float lblBox = 1.35f * res.getDimension(R.dimen.label_font_size);
        float neededSize = res.getDimension(R.dimen.digital_widget_list_min_fixed_height) +
                2 * lblBox +
                scale * res.getDimension(R.dimen.digital_widget_list_min_scaled_height);
        return ((density * height) > neededSize);
    }
}

