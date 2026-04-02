package com.buge.locker;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Settings fragment with PIN management (with confirmation), accessibility, battery, and language settings.
 */
public class SettingsFragment extends Fragment {

    private AppPreferences prefs;

    private TextView txtPasswordStatus;
    private TextView txtAccessibilityStatus;
    private TextView txtBatteryStatus;
    private TextView txtLanguageCurrent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context ctx = getContext();
        if (ctx == null) return;
        prefs = AppPreferences.getInstance(ctx);

        txtPasswordStatus = view.findViewById(R.id.txt_password_status);
        txtAccessibilityStatus = view.findViewById(R.id.txt_accessibility_status);
        txtBatteryStatus = view.findViewById(R.id.txt_battery_status);
        txtLanguageCurrent = view.findViewById(R.id.txt_language_current);

        MaterialCardView cardPassword = view.findViewById(R.id.card_password);
        MaterialCardView cardAccessibility = view.findViewById(R.id.card_accessibility);
        MaterialCardView cardBattery = view.findViewById(R.id.card_battery);
        MaterialCardView cardLanguage = view.findViewById(R.id.card_language);

        if (cardPassword != null) cardPassword.setOnClickListener(v -> showPinDialog());
        if (cardAccessibility != null) cardAccessibility.setOnClickListener(v -> onAccessibilityClick());
        if (cardBattery != null) cardBattery.setOnClickListener(v -> onBatteryClick());
        if (cardLanguage != null) cardLanguage.setOnClickListener(v -> showLanguageDialog());
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshStatuses();
    }

    private void refreshStatuses() {
        if (!isAdded() || getContext() == null) return;
        Context ctx = getContext();

        if (txtPasswordStatus != null) {
            txtPasswordStatus.setText(prefs.hasPin()
                    ? R.string.settings_password_set
                    : R.string.settings_password_not_set);
        }

        boolean accessEnabled = isAccessibilityEnabled(ctx);
        if (txtAccessibilityStatus != null) {
            txtAccessibilityStatus.setText(accessEnabled
                    ? R.string.status_enabled : R.string.status_disabled);
            txtAccessibilityStatus.setTextColor(ctx.getColor(
                    accessEnabled ? R.color.md_theme_light_primary : R.color.md_theme_light_error));
        }

        boolean batteryOk = isBatteryUnrestricted(ctx);
        if (txtBatteryStatus != null) {
            txtBatteryStatus.setText(batteryOk
                    ? R.string.status_unrestricted : R.string.status_restricted);
            txtBatteryStatus.setTextColor(ctx.getColor(
                    batteryOk ? R.color.md_theme_light_primary : R.color.md_theme_light_error));
        }

        if (txtLanguageCurrent != null) {
            String lang = prefs.getLanguage();
            if ("zh".equals(lang)) txtLanguageCurrent.setText(R.string.lang_chinese);
            else if ("es".equals(lang)) txtLanguageCurrent.setText(R.string.lang_spanish);
            else txtLanguageCurrent.setText(R.string.lang_english);
        }
    }

    public static boolean isAccessibilityEnabled(Context ctx) {
        if (ctx == null) return false;
        String serviceName = ctx.getPackageName() + "/" + AppLockService.class.getName();
        try {
            String enabledServices = Settings.Secure.getString(
                    ctx.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (!TextUtils.isEmpty(enabledServices)) {
                for (String s : enabledServices.split(":")) {
                    if (s.trim().equalsIgnoreCase(serviceName)) {
                        return true;
                    }
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    private boolean isBatteryUnrestricted(Context ctx) {
        if (ctx == null) return false;
        try {
            PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
            return pm != null && pm.isIgnoringBatteryOptimizations(ctx.getPackageName());
        } catch (Exception e) {
            return false;
        }
    }

    private void onAccessibilityClick() {
        if (!isAdded() || getContext() == null) return;
        Context ctx = getContext();
        if (isAccessibilityEnabled(ctx)) {
            Toast.makeText(ctx, R.string.status_enabled, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            new AlertDialog.Builder(ctx)
                    .setTitle(R.string.dlg_accessibility_title)
                    .setMessage(R.string.dlg_accessibility_msg)
                    .setPositiveButton(R.string.dlg_go_settings, (d, w) -> {
                        try {
                            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(ctx, "Cannot open settings", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(R.string.dlg_cancel, null)
                    .show();
        } catch (Exception ignored) {}
    }

    private void onBatteryClick() {
        if (!isAdded() || getContext() == null) return;
        Context ctx = getContext();
        if (isBatteryUnrestricted(ctx)) {
            Toast.makeText(ctx, R.string.status_unrestricted, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Intent i = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            i.setData(Uri.parse("package:" + ctx.getPackageName()));
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            try {
                startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
            } catch (Exception ex) {
                Toast.makeText(ctx, "Cannot open battery settings", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showPinDialog() {
        if (!isAdded() || getContext() == null) return;
        Context ctx = getContext();

        View dialogView = LayoutInflater.from(ctx).inflate(R.layout.dialog_set_pin, null);
        TextInputEditText editNew = dialogView.findViewById(R.id.edit_new_pin);
        TextInputEditText editConfirm = dialogView.findViewById(R.id.edit_confirm_pin);
        MaterialButton btnSave = dialogView.findViewById(R.id.btn_save);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);

        String existing = prefs.getPin();
        if (existing != null) {
            editNew.setText(existing);
            editConfirm.setText(existing);
        }

        AlertDialog dialog = new AlertDialog.Builder(ctx)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnSave.setOnClickListener(v -> {
            String newPin = editNew.getText() != null ? editNew.getText().toString().trim() : "";
            String confirm = editConfirm.getText() != null ? editConfirm.getText().toString().trim() : "";

            if (newPin.length() < 4) {
                Toast.makeText(ctx, R.string.pwd_too_short, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPin.equals(confirm)) {
                Toast.makeText(ctx, R.string.pwd_mismatch, Toast.LENGTH_SHORT).show();
                return;
            }
            prefs.setPin(newPin);
            Toast.makeText(ctx, R.string.pwd_saved, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            refreshStatuses();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showLanguageDialog() {
        if (!isAdded() || getContext() == null) return;
        Context ctx = getContext();

        View dialogView = LayoutInflater.from(ctx).inflate(R.layout.dialog_language_selection, null);
        RadioGroup group = dialogView.findViewById(R.id.radio_group_language);
        
        String current = prefs.getLanguage();
        if ("zh".equals(current)) group.check(R.id.radio_zh);
        else if ("es".equals(current)) group.check(R.id.radio_es);
        else group.check(R.id.radio_en);

        AlertDialog dialog = new AlertDialog.Builder(ctx)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        group.setOnCheckedChangeListener((g, checkedId) -> {
            String newLang = "en";
            if (checkedId == R.id.radio_zh) newLang = "zh";
            else if (checkedId == R.id.radio_es) newLang = "es";

            if (!newLang.equals(current)) {
                prefs.setLanguage(newLang);
                Toast.makeText(ctx, R.string.lang_changed, Toast.LENGTH_SHORT).show();
                if (getActivity() != null) {
                    getActivity().recreate();
                }
            }
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.btn_cancel_lang).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
