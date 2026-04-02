package com.buge.locker;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Full-screen lock screen activity.
 */
public class LockScreenActivity extends AppCompatActivity {

    public static final String EXTRA_PKG = "pkg";
    public static final String EXTRA_APP_NAME = "app_name";

    private AppPreferences prefs;
    private String lockedPkg;

    private TextInputEditText editPin;
    private TextInputLayout layoutPin;
    private TextView txtError;
    private TextView txtAppName;
    private ImageView imgAppIcon;

    @Override
    protected void attachBaseContext(Context base) {
        Context ctx = LocaleHelper.applyLocale(base);
        super.attachBaseContext(ctx != null ? ctx : base);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Show over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_lock_screen);

        prefs = AppPreferences.getInstance(this);

        lockedPkg = getIntent().getStringExtra(EXTRA_PKG);
        String appName = getIntent().getStringExtra(EXTRA_APP_NAME);
        if (appName == null || appName.isEmpty()) appName = lockedPkg;

        txtAppName = findViewById(R.id.txt_app_name);
        imgAppIcon = findViewById(R.id.img_app_icon);
        editPin = findViewById(R.id.edit_pin);
        layoutPin = findViewById(R.id.input_layout_pin);
        txtError = findViewById(R.id.txt_error);

        MaterialButton btnUnlock = findViewById(R.id.btn_unlock);
        MaterialButton btnGoHome = findViewById(R.id.btn_go_home);

        if (txtAppName != null) txtAppName.setText(appName);
        loadAppIcon(lockedPkg);

        if (btnUnlock != null) btnUnlock.setOnClickListener(v -> attemptUnlock());
        if (btnGoHome != null) btnGoHome.setOnClickListener(v -> goHome());

        if (editPin != null) {
            editPin.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    attemptUnlock();
                    return true;
                }
                return false;
            });
            editPin.requestFocus();
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                goHome();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        lockedPkg = intent.getStringExtra(EXTRA_PKG);
        String appName = intent.getStringExtra(EXTRA_APP_NAME);
        if (appName != null && txtAppName != null) txtAppName.setText(appName);
        loadAppIcon(lockedPkg);
        clearError();
        if (editPin != null) editPin.setText("");
    }

    private void loadAppIcon(String pkg) {
        if (imgAppIcon == null || pkg == null) return;
        try {
            Drawable icon = getPackageManager().getApplicationIcon(pkg);
            imgAppIcon.setImageDrawable(icon);
        } catch (PackageManager.NameNotFoundException e) {
            imgAppIcon.setImageResource(android.R.drawable.ic_lock_lock);
        }
    }

    private void attemptUnlock() {
        if (editPin == null) return;
        String input = editPin.getText() != null ? editPin.getText().toString().trim() : "";

        if (input.isEmpty()) {
            showError();
            return;
        }

        if (prefs.checkPin(input)) {
            prefs.grantUnlock(lockedPkg);
            clearError();
            finish();
        } else {
            showError();
            editPin.setText("");
        }
    }

    private void showError() {
        if (txtError != null) txtError.setVisibility(android.view.View.VISIBLE);
        if (layoutPin != null) layoutPin.setError(" ");
    }

    private void clearError() {
        if (txtError != null) txtError.setVisibility(android.view.View.GONE);
        if (layoutPin != null) layoutPin.setError(null);
    }

    private void goHome() {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.addCategory(Intent.CATEGORY_HOME);
        home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(home);
    }
}
