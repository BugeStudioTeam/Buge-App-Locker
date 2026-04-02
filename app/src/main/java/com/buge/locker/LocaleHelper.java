package com.buge.locker;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import java.util.Locale;

/**
 * Robust utility for applying locale to a Context.
 */
public class LocaleHelper {

    public static Context applyLocale(Context context) {
        if (context == null) return null;
        AppPreferences prefs = AppPreferences.getInstance(context);
        String lang = prefs.getLanguage();
        return applyLocale(context, lang);
    }

    public static Context applyLocale(Context context, String lang) {
        if (context == null) return null;
        Locale locale = "zh".equals(lang) ? Locale.SIMPLIFIED_CHINESE : Locale.ENGLISH;
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
            LocaleList localeList = new LocaleList(locale);
            LocaleList.setDefault(localeList);
            config.setLocales(localeList);
            return context.createConfigurationContext(config);
        } else {
            config.setLocale(locale);
            res.updateConfiguration(config, res.getDisplayMetrics());
            return context;
        }
    }
}
