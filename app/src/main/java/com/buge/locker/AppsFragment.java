package com.buge.locker;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Fragment showing all installed apps with lock toggles and search functionality.
 */
public class AppsFragment extends Fragment {

    private AppPreferences prefs;
    private AppAdapter adapter;
    private RecyclerView recyclerView;
    private View layoutLoading;
    private View layoutEmpty;
    private Chip chipCount;
    private EditText editSearch;

    private List<AppInfo> allApps = new ArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_apps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context ctx = getContext();
        if (ctx == null) return;
        prefs = AppPreferences.getInstance(ctx);

        recyclerView = view.findViewById(R.id.recycler_view);
        layoutLoading = view.findViewById(R.id.layout_loading);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        chipCount = view.findViewById(R.id.chip_count);
        editSearch = view.findViewById(R.id.edit_search);

        recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
        adapter = new AppAdapter((app, locked) -> {
            if (locked) {
                prefs.lockApp(app.getPackageName());
            } else {
                prefs.unlockApp(app.getPackageName());
            }
            updateChip();
        });
        recyclerView.setAdapter(adapter);

        if (editSearch != null) {
            editSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterApps(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        loadApps();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateChip();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }

    private void loadApps() {
        showLoading(true);

        executor.execute(() -> {
            allApps = fetchInstalledApps();
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;
                adapter.setApps(allApps);
                showLoading(false);
                recyclerView.setVisibility(View.VISIBLE);
                updateChip();
                updateEmptyState();
            });
        });
    }

    private void filterApps(String query) {
        if (query == null || query.isEmpty()) {
            adapter.setApps(allApps);
        } else {
            List<AppInfo> filtered = new ArrayList<>();
            String q = query.toLowerCase();
            for (AppInfo app : allApps) {
                if (app.getAppName().toLowerCase().contains(q) || 
                    app.getPackageName().toLowerCase().contains(q)) {
                    filtered.add(app);
                }
            }
            adapter.setApps(filtered);
        }
        updateEmptyState();
    }

    private List<AppInfo> fetchInstalledApps() {
        Context ctx = getContext();
        if (ctx == null) return new ArrayList<>();
        
        PackageManager pm = ctx.getPackageManager();
        List<ApplicationInfo> installed;
        try {
            installed = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        } catch (Exception e) {
            return new ArrayList<>();
        }

        Set<String> lockedSet = prefs.getLockedApps();
        List<AppInfo> result = new ArrayList<>();
        String ownPkg = ctx.getPackageName();

        for (ApplicationInfo info : installed) {
            try {
                if (pm.getLaunchIntentForPackage(info.packageName) == null) continue;
                if (info.packageName.equals(ownPkg)) continue;

                String name = pm.getApplicationLabel(info).toString();
                android.graphics.drawable.Drawable icon = pm.getApplicationIcon(info.packageName);

                boolean locked = lockedSet.contains(info.packageName);
                result.add(new AppInfo(name, info.packageName, icon, locked));
            } catch (Exception ignored) {}
        }

        Collections.sort(result, (a, b) -> {
            if (a.isLocked() != b.isLocked()) return a.isLocked() ? -1 : 1;
            return a.getAppName().compareToIgnoreCase(b.getAppName());
        });

        return result;
    }

    private void showLoading(boolean loading) {
        if (layoutLoading != null) layoutLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (recyclerView != null) recyclerView.setVisibility(loading ? View.GONE : View.VISIBLE);
    }

    private void updateEmptyState() {
        if (adapter == null || layoutEmpty == null) return;
        layoutEmpty.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    private void updateChip() {
        if (chipCount == null || prefs == null || !isAdded()) return;
        int count = prefs.getLockedApps().size();
        chipCount.setText(getString(R.string.locked_count, count));
    }
}
