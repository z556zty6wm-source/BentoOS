package com.bentoOS.launcher;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Menubar {

    private Context context;
    private LinearLayout menubarView;
    private TextView clockView;
    private TextView activeAppView;

    // BentoOS Design: transparent menubar
    private static final int MENUBAR_BACKGROUND = 0x80000000;
    private static final int TEXT_PRIMARY = 0xFFe0e0e0;
    private static final int TEXT_SECONDARY = 0xFF666666;

    public Menubar(Context context) {
        this.context = context;
    }

    public void init() {
        menubarView = new LinearLayout(context);
        menubarView.setOrientation(LinearLayout.HORIZONTAL);
        menubarView.setBackgroundColor(MENUBAR_BACKGROUND);

        // Left side — active app name
        activeAppView = new TextView(context);
        activeAppView.setTextColor(TEXT_PRIMARY);
        activeAppView.setText("BentoOS");

        // Right side — clock
        clockView = new TextView(context);
        clockView.setTextColor(TEXT_PRIMARY);
        updateClock();

        menubarView.addView(activeAppView);
        menubarView.addView(clockView);
    }

    public void refresh() {
        updateClock();
    }

    private void updateClock() {
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault())
            .format(new Date());
        if (clockView != null) {
            clockView.setText(time);
        }
    }

    public void setActiveApp(String appName) {
        if (activeAppView != null) {
            activeAppView.setText(appName);
        }
    }
}