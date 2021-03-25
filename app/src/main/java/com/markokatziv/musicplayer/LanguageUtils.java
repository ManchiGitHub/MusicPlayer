package com.markokatziv.musicplayer;

import android.os.Build;
import android.os.LocaleList;
import java.util.Locale;

/**
 * Created By marko
 */
public class LanguageUtils {

    public static String ENGLISH = "en";

    /* This function returns a string representation of the current language.
       Need this to mirror buttons in player layout according to current locale.*/
    public static String getCurrentLanguage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return LocaleList.getDefault().get(0).getLanguage();
        }
        else {
            return Locale.getDefault().getLanguage();
        }
    }
}
