package com.markokatziv.musicplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.ArraySet;
import android.util.Log;

import java.util.HashSet;

/**
 * Created By marko
 */
public class PreferenceHandler {

    public static final String DEFAULT_PREF_NAME = "Continuity";

    // Use these tags when saving and getting back values.
    public static final String TAG_SONG_DURATION = "song_duration";
    public static final String TAG_IMAGE_INDEX = "image_index";
    public static final String TAG_LAST_SONG_INDEX = "image_index";
    public static final String TAG_LAST_SONG_INFO = "song_info";
    public static final String TAG_WAS_PLAYING = "was_playing";
    public static final String TAG_FIRST_TIME = "first_time";


    private static SharedPreferences getPreferences(Context context) {
        String name = DEFAULT_PREF_NAME; // Change name here
        return context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    private static SharedPreferences.Editor getEditor(Context context) {
        return getPreferences(context).edit();
    }

    // Save an integer
    public static void putInt(String tag, int value, Context context) {
        getEditor(context).putInt(tag, value).commit();
    }

    // Get an integer
    public static int getInt(String tag, Context context) {
        return getPreferences(context).getInt(tag,0);
    }

    // Get a boolean
    public static boolean getBoolean(String tag, Context context) {
        return getPreferences(context).getBoolean(tag, false);
    }

    public static void putBoolean(String tag,boolean value, Context context) {
        getEditor(context).putBoolean(TAG_FIRST_TIME, value).commit();
    }


    public static String getSongInfo(String tag, Context context){
        return getPreferences(context).getString(TAG_LAST_SONG_INFO, "");
    }




    public static void saveState(int lastSongIndex, boolean isPlaying, String songTitle, String artistTitle, Context context){

        SharedPreferences.Editor editor = getEditor(context);
        editor.putString(TAG_LAST_SONG_INFO, songTitle + "%" + artistTitle)
                .putInt(TAG_LAST_SONG_INDEX, lastSongIndex)
                .putBoolean(TAG_WAS_PLAYING, isPlaying).commit();

    }
    // Add more stuff here
}