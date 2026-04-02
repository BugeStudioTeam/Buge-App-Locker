package com.buge.locker;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Main activity — hosts AppsFragment and SettingsFragment via BottomNavigationView.
 */
public class MainActivity extends AppCompatActivity {

    private AppPreferences prefs;

    @Override
    protected void attachBaseContext(Context base) {
        // Safer applyLocale call
        Context ctx = LocaleHelper.applyLocale(base);
        super.attachBaseContext(ctx != null ? ctx : base);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = AppPreferences.getInstance(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) setSupportActionBar(toolbar);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        if (bottomNav != null) {
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_apps) {
                    showFragment(new AppsFragment(), "apps");
                    return true;
                } else if (id == R.id.nav_settings) {
                    showFragment(new SettingsFragment(), "settings");
                    return true;
                }
                return false;
            });
        }

        // Only show apps fragment if no previous state exists
        if (savedInstanceState == null) {
            showFragment(new AppsFragment(), "apps");
            if (bottomNav != null) bottomNav.setSelectedItemId(R.id.nav_apps);
        }

        // Defer battery optimization prompt
        new Handler(Looper.getMainLooper()).postDelayed(
                this::requestBatteryOptimizationIfNeeded, 2000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (prefs != null) prefs.clearUnlock();
    }

    private void showFragment(Fragment fragment, String tag) {
        if (isFinishing() || isDestroyed()) return;
        try {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, fragment, tag);
            ft.commitAllowingStateLoss();
        } catch (Exception ignored) {}
    }

    private void requestBatteryOptimizationIfNeeded() {
        if (isFinishing() || isDestroyed()) return;
        try {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (pm != null && !pm.isIgnoringBatteryOptimizations(getPackageName())) {
                Intent i = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                i.setData(Uri.parse("package:" + getPackageName()));
                startActivity(i);
            }
        } catch (Exception ignored) {}
    }
}
