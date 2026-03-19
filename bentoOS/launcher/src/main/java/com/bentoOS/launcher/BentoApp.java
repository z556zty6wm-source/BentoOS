package com.bentoOS.launcher;

import android.app.Application;

public class BentoApp extends Application {

    private static BentoApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        init();
    }

    private void init() {
        // Initialize global BentoOS services
        initTheme();
        initPreferences();
    }

    private void initTheme() {
        // Apply BentoOS dark theme system-wide
        // #0d0d0d background, #6c6cff accent
    }

    private void initPreferences() {
        // Load user preferences — accent color, wallpaper, dock apps
    }

    public static BentoApp getInstance() {
        return instance;
    }
}