package com.buge.locker;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.accessibility.AccessibilityEvent;

/**
 * Core accessibility service that monitors app launches.
 */
public class AppLockService extends AccessibilityService {

    private AppPreferences prefs;
    private String lastForegroundPkg = "";

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = AppPreferences.getInstance(this);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        prefs = AppPreferences.getInstance(this);

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.DEFAULT;
        info.notificationTimeout = 100;
        setServiceInfo(info);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return;
        }

        CharSequence pkgCS = event.getPackageName();
        if (pkgCS == null) return;

        String pkg = pkgCS.toString();
        String ownPkg = getPackageName();

        // Don't lock our own app or the lock screen
        if (pkg.equals(ownPkg) || pkg.equals("android") || pkg.equals("com.android.systemui")) return;
        
        // Avoid processing the same package repeatedly
        if (pkg.equals(lastForegroundPkg)) return;
        lastForegroundPkg = pkg;

        // Check if app is locked and not recently unlocked
        if (prefs.isLocked(pkg) && !prefs.isRecentlyUnlocked(pkg)) {
            // Exit the locked app immediately by going home
            performGlobalAction(GLOBAL_ACTION_HOME);

            // Launch the lock screen after a brief delay
            new Thread(() -> {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ignored) {}

                String appName = pkg;
                try {
                    PackageManager pm = getPackageManager();
                    android.content.pm.ApplicationInfo info = pm.getApplicationInfo(pkg, 0);
                    appName = pm.getApplicationLabel(info).toString();
                } catch (Exception ignored) {}

                try {
                    Intent lockIntent = new Intent(AppLockService.this, LockScreenActivity.class);
                    lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK 
                            | Intent.FLAG_ACTIVITY_CLEAR_TOP 
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    lockIntent.putExtra(LockScreenActivity.EXTRA_PKG, pkg);
                    lockIntent.putExtra(LockScreenActivity.EXTRA_APP_NAME, appName);
                    startActivity(lockIntent);
                } catch (Exception ignored) {}
            }).start();
        }
    }

    @Override
    public void onInterrupt() {}
}
