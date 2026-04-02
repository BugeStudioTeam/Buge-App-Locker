package com.buge.locker;

import android.graphics.drawable.Drawable;

/**
 * Data model representing an installed application.
 */
public class AppInfo {

    private final String appName;
    private final String packageName;
    private final Drawable icon;
    private boolean locked;

    public AppInfo(String appName, String packageName, Drawable icon, boolean locked) {
        this.appName = appName;
        this.packageName = packageName;
        this.icon = icon;
        this.locked = locked;
    }

    public String getAppName() { return appName; }
    public String getPackageName() { return packageName; }
    public Drawable getIcon() { return icon; }
    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }
}
