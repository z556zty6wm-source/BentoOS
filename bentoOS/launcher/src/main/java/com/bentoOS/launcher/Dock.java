package com.bentoOS.launcher;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

public class Dock {

    private Context context;
    private LinearLayout dockView;

    // BentoOS Design: #161616 surface, 20dp pill radius, #6c6cff accent
    private static final int DOCK_BACKGROUND = 0xFF161616;
    private static final int DOCK_RADIUS = 20;
    private static final int ACCENT_COLOR = 0xFF6c6cff;

    public Dock(Context context) {
        this.context = context;
    }

    public void init() {
        dockView = new LinearLayout(context);
        dockView.setOrientation(LinearLayout.HORIZONTAL);
        dockView.setBackgroundColor(DOCK_BACKGROUND);
        // Liquid Glass effect only on dock
        applyLiquidGlass();
    }

    public void refresh() {
        // Reload installed apps into dock
        loadDockApps();
    }

    private void applyLiquidGlass() {
        // Blur + transparency effect
        dockView.setAlpha(0.92f);
    }

    private void loadDockApps() {
        // Load pinned apps from preferences
    }
}