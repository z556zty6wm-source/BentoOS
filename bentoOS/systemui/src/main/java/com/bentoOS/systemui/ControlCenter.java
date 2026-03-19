package com.bentoOS.systemui;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.net.wifi.WifiManager;
import android.bluetooth.BluetoothAdapter;
import android.media.AudioManager;

public class ControlCenter {

    private Context context;
    private LinearLayout controlCenterView;
    private boolean isVisible = false;

    // BentoOS Design
    private static final int BG = 0xCC161616;
    private static final int SURFACE = 0xFF1e1e1e;
    private static final int ACCENT = 0xFF6c6cff;
    private static final int TEXT_PRIMARY = 0xFFe0e0e0;
    private static final int TEXT_SECONDARY = 0xFF666666;

    // System services
    private WifiManager wifiManager;
    private BluetoothAdapter bluetoothAdapter;
    private AudioManager audioManager;

    public ControlCenter(Context context) {
        this.context = context;
        initServices();
        buildUI();
    }

    private void initServices() {
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    private void buildUI() {
        controlCenterView = new LinearLayout(context);
        controlCenterView.setOrientation(LinearLayout.VERTICAL);
        controlCenterView.setBackgroundColor(BG);
        controlCenterView.setPadding(24, 24, 24, 24);

        // Row 1 — WiFi + Bluetooth + Airplane
        LinearLayout row1 = buildToggleRow();
        controlCenterView.addView(row1);

        // Row 2 — Brightness slider
        controlCenterView.addView(buildSlider("Brightness"));

        // Row 3 — Volume slider
        controlCenterView.addView(buildSlider("Volume"));

        // Row 4 — Do Not Disturb + Dark Mode + Gaming Mode
        LinearLayout row2 = buildToggleRow();
        controlCenterView.addView(row2);

        controlCenterView.setVisibility(View.GONE);
    }

    private LinearLayout buildToggleRow() {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        return row;
    }

    private View buildSlider(String label) {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);

        TextView labelView = new TextView(context);
        labelView.setText(label);
        labelView.setTextColor(TEXT_SECONDARY);
        labelView.setTextSize(12);

        SeekBar seekBar = new SeekBar(context);

        if (label.equals("Volume")) {
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            seekBar.setMax(maxVolume);
            seekBar.setProgress(currentVolume);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                }
                public void onStartTrackingTouch(SeekBar sb) {}
                public void onStopTrackingTouch(SeekBar sb) {}
            });
        }

        container.addView(labelView);
        container.addView(seekBar);
        return container;
    }

    public void toggle() {
        isVisible = !isVisible;
        controlCenterView.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        if (isVisible) animateIn();
    }

    private void animateIn() {
        controlCenterView.setAlpha(0f);
        controlCenterView.setTranslationY(-20f);
        controlCenterView.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(250)
            .start();
    }

    public View getView() {
        return controlCenterView;
    }
}