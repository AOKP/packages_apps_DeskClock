package com.android.alarmclock;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;

import com.android.deskclock.R;

public class AnalogAOKPAppWidgetConfigure extends Activity {
    private static final boolean DEBUG = true;

    private static final String LOG_TAG = "AnalogAOKPAppWidgetConfigure";

    private static final String PREFS_NAME
            = "com.android.alarmclock.AnalogAOKPAppWidget";

    private static final String PREF_PREFIX_KEY = "clock_style_";

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    String[] clockStyleNames;
    String[] clockStyleDrawables;

    int selectedPos = 0;

    Spinner spinnerClockStyle;
    ImageView imagePreview;

    public AnalogAOKPAppWidgetConfigure() {
        super();
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.analog_aokp_appwidget_config);

        clockStyleNames = getResources().getStringArray(R.array.aokp_clock_backgrounds_name);
        clockStyleDrawables = getResources().getStringArray(R.array.aokp_clock_backgrounds_drawable);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // Bind the action for the ok button.
        findViewById(R.id.buttonOK).setOnClickListener(mOnClickListener);

        spinnerClockStyle = (Spinner) findViewById(R.id.spinnerSelectBackground);
        spinnerClockStyle.setOnItemSelectedListener(mOnItemSelectedListener);

        imagePreview = (ImageView) findViewById(R.id.imageViewPreview);

        // If they gave us an intent without the widget id, just bail.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        selectedPos = loadClockPref(this, mAppWidgetId);

        spinnerClockStyle.setSelection(selectedPos);

        int resID = getResources().getIdentifier(clockStyleDrawables[selectedPos], "drawable",
                getPackageName());
        if (resID != 0) {
            imagePreview.setImageDrawable(getResources().getDrawable(resID));
        }
    }

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = AnalogAOKPAppWidgetConfigure.this;

            saveClockPref(context, mAppWidgetId, selectedPos);

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            AnalogAOKPAppWidgetProvider.updateAppWidget(context, appWidgetManager,
                    mAppWidgetId, clockStyleDrawables[selectedPos]);

            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    AdapterView.OnItemSelectedListener mOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> parent,
            View view, int pos, long id) {
            if (DEBUG) Log.d(LOG_TAG,"selected item: " + pos);
            int resID = parent.getResources().getIdentifier(clockStyleDrawables[pos], "drawable",
                parent.getContext().getPackageName());
            if (resID != 0) {
                imagePreview.setImageDrawable(parent.getResources().getDrawable(resID));
            }
            selectedPos = pos;
        }

        public void onNothingSelected(AdapterView parent) {
          // Do nothing.
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        if (DEBUG) Log.d(LOG_TAG, "onPause");

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (DEBUG) Log.d(LOG_TAG, "onResume");

    }

    // Write the clock style to the SharedPreferences object for this widget
    static void saveClockPref(Context context, int appWidgetId, int pos) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putInt(PREF_PREFIX_KEY + appWidgetId, pos);
        prefs.commit();
    }

    // Read the clock style from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default
    static int loadClockPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getInt(PREF_PREFIX_KEY + appWidgetId, 0);
    }

    // delete the clock style from the SharedPreferences object for this widget
    static void deleteClockPref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.commit();
    }
}
