package com.bentoOS.privacy;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.view.WindowManager;

public class GamingMode {

    private Context context;
    private SharedPreferences prefs;
    private boolean isActive = false;

    private static final String PREFS_NAME = "bento_gaming_prefs";

    // Gaming mode settings
    private boolean fpsOverlayEnabled = true;
    private boolean notificationsBlocked = true;
    private boolean autoRotateEnabled = false;
    private boolean touchBoostEnabled = true;
    private boolean recordingEnabled = false;

    // CPU governor
    private static final String GOVERNOR_PERFORMANCE = "performance";
    private static final String GOVERNOR_BALANCED = "schedutil";

    public GamingMode(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadSettings();
    }

    private void loadSettings() {
        fpsOverlayEnabled = prefs.getBoolean("fps_overlay", true);
        notificationsBlocked = prefs.getBoolean("block_notifs", true);
        touchBoostEnabled = prefs.getBoolean("touch_boost", true);
    }

    public void enable() {
        isActive = true;
        applyGamingSettings();
    }

    public void disable() {
        isActive = false;
        restoreNormalSettings();
    }

    private void applyGamingSettings() {
        // Block notifications
        if (notificationsBlocked) {
            // Set DND mode
        }

        // Boost CPU to performance governor
        setCpuGovernor(GOVERNOR_PERFORMANCE);

        // Keep screen on
        // Force GPU rendering
        // Show FPS overlay if enabled
        if (fpsOverlayEnabled) {
            showFpsOverlay();
        }
    }

    private void restoreNormalSettings() {
        setCpuGovernor(GOVERNOR_BALANCED);
        hideFpsOverlay();
    }

    private void setCpuGovernor(String governor) {
        // Written to /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor
        // Requires root on kernel level
        try {
            Runtime.getRuntime().exec(
                "echo " + governor +
                " > /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showFpsOverlay() {
        // FPS overlay drawn via WindowManager SYSTEM_ALERT_WINDOW
    }

    private void hideFpsOverlay() {
        // Remove FPS overlay
    }

    public void startRecording() {
        recordingEnabled = true;
        // MediaProjection API for screen recording
    }

    public void stopRecording() {
        recordingEnabled = false;
    }

    public boolean isActive() { return isActive; }
    public boolean isFpsOverlayEnabled() { return fpsOverlayEnabled; }
    public boolean isRecording() { return recordingEnabled; }

    public void setFpsOverlay(boolean enabled) {
        fpsOverlayEnabled = enabled;
        prefs.edit().putBoolean("fps_overlay", enabled).apply();
    }

    public void setNotificationsBlocked(boolean blocked) {
        notificationsBlocked = blocked;
        prefs.edit().putBoolean("block_notifs", blocked).apply();
    }
}