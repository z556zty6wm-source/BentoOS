package com.bentoOS.systemui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import java.util.ArrayList;
import java.util.List;

public class Spotlight {

    private Context context;
    private LinearLayout spotlightView;
    private EditText searchBar;
    private ListView resultsList;
    private boolean isVisible = false;

    // BentoOS Design
    private static final int BG = 0xCC161616;
    private static final int SURFACE = 0xFF1e1e1e;
    private static final int ACCENT = 0xFF6c6cff;
    private static final int TEXT_PRIMARY = 0xFFe0e0e0;
    private static final int TEXT_HINT = 0xFF666666;

    public Spotlight(Context context) {
        this.context = context;
        buildUI();
    }

    private void buildUI() {
        spotlightView = new LinearLayout(context);
        spotlightView.setOrientation(LinearLayout.VERTICAL);
        spotlightView.setBackgroundColor(BG);
        spotlightView.setPadding(24, 24, 24, 24);

        // Search bar
        searchBar = new EditText(context);
        searchBar.setHint("Search apps, settings, files...");
        searchBar.setTextColor(TEXT_PRIMARY);
        searchBar.setHintTextColor(TEXT_HINT);
        searchBar.setBackgroundColor(SURFACE);
        searchBar.setPadding(16, 12, 16, 12);
        searchBar.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                search(s.toString());
            }
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Results list
        resultsList = new ListView(context);
        resultsList.setBackgroundColor(BG);

        spotlightView.addView(searchBar);
        spotlightView.addView(resultsList);
        spotlightView.setVisibility(View.GONE);
    }

    private void search(String query) {
        if (query.isEmpty()) {
            resultsList.setAdapter(null);
            return;
        }

        List<String> results = new ArrayList<>();

        // Search installed apps
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = pm.queryIntentActivities(intent, 0);

        for (ResolveInfo app : apps) {
            String appName = app.loadLabel(pm).toString();
            if (appName.toLowerCase().contains(query.toLowerCase())) {
                results.add(appName);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            context,
            android.R.layout.simple_list_item_1,
            results
        );
        resultsList.setAdapter(adapter);
    }

    public void toggle() {
        isVisible = !isVisible;
        spotlightView.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        if (isVisible) {
            animateIn();
            searchBar.requestFocus();
            InputMethodManager imm = (InputMethodManager)
                context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(searchBar, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void animateIn() {
        spotlightView.setAlpha(0f);
        spotlightView.setScaleX(0.96f);
        spotlightView.setScaleY(0.96f);
        spotlightView.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(200)
            .start();
    }

    public View getView() {
        return spotlightView;
    }
}