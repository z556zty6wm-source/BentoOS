package com.bentoOS.systemui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StatusBar {

    private Context context;
    private LinearLayout statusBarView;
    private TextView clockView;
    private TextView batteryView;
    private TextView wifiView;
    private TextView activeAppView;

    // BentoOS Design
    private static final int BG = 0x99000000;
    private static final int TEXT_PRIMARY = 0xFFe0e0e0;
    private static final int TEXT_SECONDARY = 0xFF666666;
    private static final int ACCENT = 0xFF6c6cff;

    private BroadcastReceiver batteryReceiver;
    private BroadcastReceiver timeReceiver;

    public StatusBar(Context context) {
        this.context = context;
        buildUI();
        registerReceivers();
    }

    private void buildUI() {
        statusBarView = new LinearLayout(context);
        statusBarView.setOrientation(LinearLayout.HORIZONTAL);
        statusBarView.setBackgroundColor(BG);
        statusBarView.setPadding(16, 0, 16, 0);

        // Left — active app name
        activeAppView = new TextView(context);
        activeAppView.setText("BentoOS");
        activeAppView.setTextColor(TEXT_PRIMARY);
        activeAppView.setTextSize(13);
        LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        activeAppView.setLayoutParams(leftParams);

        // Center — clock
        clockView = new TextView(context);
        clockView.setTextColor(TEXT_PRIMARY);
        clockView.setTextSize(13);
        clockView.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams centerParams = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        clockView.setLayoutParams(centerParams);
        updateClock();

        // Right — wifi + battery
        LinearLayout rightGroup = new LinearLayout(context);
        rightGroup.setOrientation(LinearLayout.HORIZONTAL);
        rightGroup.setGravity(android.view.Gravity.END | android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rightParams = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        rightGroup.setLayoutParams(rightParams);

        wifiView = new TextView(context);
        wifiView.setTextColor(TEXT_PRIMARY);
        wifiView.setTextSize(13);
        wifiView.setPadding(0, 0, 12, 0);

        batteryView = new TextView(context);
        batteryView.setTextColor(TEXT_PRIMARY);
        batteryView.setTextSize(13);

        rightGroup.addView(wifiView);
        rightGroup.addView(batteryView);

        statusBarView.addView(activeAppView);
        statusBarView.addView(clockView);
        statusBarView.addView(rightGroup);
    }

    private void registerReceivers() {
        // Battery
        batteryReceiver = new BroadcastReceiver() {
            public void onReceive(Context ctx, Intent intent) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                int battery = (int) ((level / (float) scale) * 100);
                batteryView.setText(battery + "%");
            }
        };
        context.registerReceiver(batteryReceiver,
            new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        // Clock — updates every minute
        timeReceiver = new BroadcastReceiver() {
            public void onReceive(Context ctx, Intent intent) {
                updateClock();
            }
        };
        context.registerReceiver(timeReceiver,
            new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    private void updateClock() {
        String time = new SimpleDateFormat("EEE d MMM  HH:mm",
            Locale.getDefault()).format(new Date());
        clockView.setText(time);
    }

    public void setActiveApp(String appName) {
        activeAppView.setText(appName);
    }

    public void updateWifi(boolean connected) {
        wifiView.setText(connected ? "WiFi" : "");
        wifiView.setTextColor(connected ? TEXT_PRIMARY : TEXT_SECONDARY);
    }

    public void destroy() {
        context.unregisterReceiver(batteryReceiver);
        context.unregisterReceiver(timeReceiver);
    }

    public View getView() {
        return statusBarView;
    }
}