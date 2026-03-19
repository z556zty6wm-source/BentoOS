package com.bentoOS.launcher;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

public class LauncherActivity extends Activity {

    private Dock dock;
    private Menubar menubar;
    private BentoWindowManager windowManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Full screen, no status bar
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        setContentView(R.layout.activity_launcher);

        // Initialize components
        dock = new Dock(this);
        menubar = new Menubar(this);
        windowManager = new BentoWindowManager(this);

        // Setup UI
        dock.init();
        menubar.init();
        windowManager.init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        dock.refresh();
        menubar.refresh();
    }
}
