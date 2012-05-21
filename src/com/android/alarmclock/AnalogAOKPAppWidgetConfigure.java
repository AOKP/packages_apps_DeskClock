package com.android.alarmclock;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Gallery;

import com.android.deskclock.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class AnalogAOKPAppWidgetConfigure extends Activity implements AdapterView.OnItemSelectedListener,
        OnClickListener {
    private static final boolean DEBUG = false;

    private static final String LOG_TAG = "AnalogAOKPAppWidgetConfigure";

    private static final String PREFS_NAME
            = "com.android.alarmclock.AnalogAOKPAppWidget";

    private static final String PREF_PREFIX_KEY = "clock_style_";

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    int selectedPos = 0;

    Gallery galleryClockStyle;
    ImageView imagePreview;

    private ArrayList<Integer> mImages;

    public AnalogAOKPAppWidgetConfigure() {
        super();
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setTitle(R.string.clock_dial_instructions);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);

        findClockDials();

        setContentView(R.layout.analog_aokp_appwidget_config);

        galleryClockStyle = (Gallery) findViewById(R.id.gallerySelectBackground);
        galleryClockStyle.setAdapter(new ImageAdapter(this));
        galleryClockStyle.setOnItemSelectedListener(this);
        galleryClockStyle.setCallbackDuringFling(false);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // Bind the action for the ok button.
        findViewById(R.id.buttonOK).setOnClickListener(this);

        imagePreview = (ImageView) findViewById(R.id.imageViewPreview);

        // If they gave us an intent without the widget id, just bail.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        selectedPos = loadClockPref(this, mAppWidgetId);

        galleryClockStyle.setSelection(selectedPos);

        imagePreview.setImageDrawable(getResources().getDrawable(mImages.get(selectedPos)));
    }

    private void findClockDials() {
        mImages = new ArrayList<Integer>();

        final Resources resources = getResources();
        final String packageName = getApplication().getPackageName();

        addClockDials(resources, packageName, R.array.aokp_clock_backgrounds_drawable);
    }

    private void addClockDials(Resources resources, String packageName, int list) {
        final String[] extras = resources.getStringArray(list);
        for (String extra : extras) {
            int res = resources.getIdentifier(extra, "drawable", packageName);
            if (res != 0) {
                mImages.add(res);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (DEBUG) Log.d(LOG_TAG, "onPause");

    }

    @Override
    public void onStop() {
        super.onStop();
        if (DEBUG) Log.d(LOG_TAG, "onStop");

    }

    @Override
    public void onResume() {
        super.onResume();
        if (DEBUG) Log.d(LOG_TAG, "onResume");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (DEBUG) Log.d(LOG_TAG, "onDestroy");

    }

    public void onItemSelected(AdapterView parent, View v, int position, long id) {
        selectedPos = position;
        imagePreview.setImageDrawable(parent.getResources().getDrawable(mImages.get(selectedPos)));
    }

    public void onNothingSelected(AdapterView parent) {
    }

    private class ImageAdapter extends BaseAdapter {
        private LayoutInflater mLayoutInflater;

        ImageAdapter(AnalogAOKPAppWidgetConfigure context) {
            mLayoutInflater = context.getLayoutInflater();
        }

        public int getCount() {
            return mImages.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView image;

            if (convertView == null) {
                image = (ImageView) mLayoutInflater.inflate(R.layout.analog_aokp_appwidget_config_dial_item, parent, false);
            } else {
                image = (ImageView) convertView;
            }

            int imageRes = mImages.get(position);
            image.setImageResource(imageRes);
            Drawable imageDrawable = image.getDrawable();
            if (imageDrawable != null) {
                imageDrawable.setDither(true);
            } else {
                Log.e(LOG_TAG, String.format(
                    "Error decoding image resId=%d for wallpaper #%d",
                    imageRes, position));
            }
            return image;
        }
    }

    public void onClick(View v) {
        saveClockPref(this, mAppWidgetId, selectedPos);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

        AnalogAOKPAppWidgetProvider.updateAppWidget(this, appWidgetManager,
            mAppWidgetId, mImages.get(selectedPos));

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
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
