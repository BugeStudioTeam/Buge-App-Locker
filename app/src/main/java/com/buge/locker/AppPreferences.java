package com.buge.locker;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * Robust SharedPreferences wrapper using Singleton pattern to avoid multiple instances.
 */
public class AppPreferences {

    private static final String PREFS_NAME = "buge_locker_prefs";
    private static final String KEY_PIN = "pin";
    private static final String KEY_LOCKED_APPS = "locked_apps";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_UNLOCKED_PKG = "unlocked_pkg";
    private static final String KEY_UNLOCK_TIME = "unlock_time";

    private static final long UNLOCK_GRACE_MS = 5000;

    private static AppPreferences instance;
    private final SharedPreferences prefs;

    private AppPreferences(Context context) {
        // Always use application context
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized AppPreferences getInstance(Context context) {
        if (instance == null) {
            instance = new AppPreferences(context);
        }
        return instance;
    }

    // ─── PIN ────────────────────────────────────────────────────────────────

    public void setPin(String pin) {
        prefs.edit().putString(KEY_PIN, pin).apply();
    }

    public String getPin() {
        return prefs.getString(KEY_PIN, null);
    }

    public boolean hasPin() {
        String pin = getPin();
        return pin != null && pin.length() >= 4;
    }

    public boolean checkPin(String input) {
        if (input == null) return false;
        String stored = getPin();
        return stored != null && stored.equals(input.trim());
    }

    // ─── LOCKED APPS ────────────────────────────────────────────────────────

    public Set<String> getLockedApps() {
        Set<String> saved = prefs.getStringSet(KEY_LOCKED_APPS, null);
        return saved != null ? new HashSet<>(saved) : new HashSet<>();
    }

    public boolean isLocked(String pkg) {
        if (pkg == null) return false;
        return getLockedApps().contains(pkg);
    }

    public void lockApp(String pkg) {
        if (pkg == null) return;
        Set<String> apps = getLockedApps();
        apps.add(pkg);
        prefs.edit().putStringSet(KEY_LOCKED_APPS, apps).apply();
    }

    public void unlockApp(String pkg) {
        if (pkg == null) return;
        Set<String> apps = getLockedApps();
        apps.remove(pkg);
        prefs.edit().putStringSet(KEY_LOCKED_APPS, apps).apply();
    }

    // ─── LANGUAGE ───────────────────────────────────────────────────────────

    public void setLanguage(String lang) {
        prefs.edit().putString(KEY_LANGUAGE, lang).apply();
    }

    public String getLanguage() {
        return prefs.getString(KEY_LANGUAGE, "en");
    }

    // ─── UNLOCK STATE ───────────────────────────────────────────────────────

    public void grantUnlock(String pkg) {
        prefs.edit()
                .putString(KEY_UNLOCKED_PKG, pkg)
                .putLong(KEY_UNLOCK_TIME, System.currentTimeMillis())
                .apply();
    }

    public boolean isRecentlyUnlocked(String pkg) {
        if (pkg == null) return false;
        String saved = prefs.getString(KEY_UNLOCKED_PKG, null);
        if (!pkg.equals(saved)) return false;
        long unlockTime = prefs.getLong(KEY_UNLOCK_TIME, 0);
        return (System.currentTimeMillis() - unlockTime) < UNLOCK_GRACE_MS;
    }

    public void clearUnlock() {
        prefs.edit()
                .remove(KEY_UNLOCKED_PKG)
                .remove(KEY_UNLOCK_TIME)
                .apply();
    }
}
