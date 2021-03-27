package com.markokatziv.musicplayer;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceHandler {

    public static final String DEFAULT_STRING_VALUE = "default_value";
    public static final String TAG_SONG_DURATION = "song_duration";

    private static SharedPreferences.Editor getEditor(Context context) {
        return getPreferences(context).edit();
    }

    /**
     * Returns SharedPreferences object.
     * @param context Application context
     * @return shared preferences instance
     */
    private static SharedPreferences getPreferences(Context context) {
        String name = "Continuity";
        return context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    /**
     * Save an Integer on SharedPreferences.
     * @param tag     tag
     * @param value   value
     * @param context Application context
     */
    public static void putInt(String tag, int value, Context context) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putInt(tag, value);
        editor.commit();
    }

    /**
     * Get an Integer from SharedPreferences.
     *
     * @param tag     tag
     * @param context Application context
     * @return String value
     */
    public static int getInt(String tag, Context context) {
        SharedPreferences sharedPreferences = getPreferences(context);
        return sharedPreferences.getInt(tag, 0);
    }

}