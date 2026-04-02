package com.buge.locker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for the app list with lock toggle.
 */
public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {

    public interface OnToggleListener {
        void onToggle(AppInfo app, boolean locked);
    }

    private final List<AppInfo> allApps = new ArrayList<>();
    private final List<AppInfo> displayed = new ArrayList<>();
    private final OnToggleListener listener;
    private String currentQuery = "";

    public AppAdapter(OnToggleListener listener) {
        this.listener = listener;
    }

    public void setApps(List<AppInfo> apps) {
        allApps.clear();
        allApps.addAll(apps);
        applyFilter(currentQuery);
    }

    public void filter(String query) {
        currentQuery = query == null ? "" : query.trim().toLowerCase();
        applyFilter(currentQuery);
    }

    private void applyFilter(String q) {
        displayed.clear();
        if (q.isEmpty()) {
            displayed.addAll(allApps);
        } else {
            for (AppInfo a : allApps) {
                if (a.getAppName().toLowerCase().contains(q)
                        || a.getPackageName().toLowerCase().contains(q)) {
                    displayed.add(a);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        AppInfo app = displayed.get(position);

        h.imgIcon.setImageDrawable(app.getIcon());
        h.txtName.setText(app.getAppName());
        h.txtPkg.setText(app.getPackageName());

        // Detach listener before setting state to avoid spurious callbacks
        h.switchLock.setOnCheckedChangeListener(null);
        h.switchLock.setChecked(app.isLocked());

        CompoundButton.OnCheckedChangeListener toggleListener = (btn, checked) -> {
            app.setLocked(checked);
            if (listener != null) listener.onToggle(app, checked);
        };
        h.switchLock.setOnCheckedChangeListener(toggleListener);

        // Clicking the whole row toggles the switch
        h.itemView.setOnClickListener(v -> h.switchLock.toggle());
    }

    @Override
    public int getItemCount() {
        return displayed.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgIcon;
        final TextView txtName;
        final TextView txtPkg;
        final MaterialSwitch switchLock;

        ViewHolder(@NonNull View v) {
            super(v);
            imgIcon = v.findViewById(R.id.img_icon);
            txtName = v.findViewById(R.id.txt_app_name);
            txtPkg = v.findViewById(R.id.txt_package);
            switchLock = v.findViewById(R.id.switch_lock);
        }
    }
}
