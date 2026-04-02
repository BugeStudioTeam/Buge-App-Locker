package com.buge.locker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Receives BOOT_COMPLETED to ensure the accessibility service is reminded to start.
 * (The actual service is started by the OS when accessibility is enabled.)
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Accessibility services are managed by the OS.
        // This receiver exists so the app is kept alive after boot.
    }
}
