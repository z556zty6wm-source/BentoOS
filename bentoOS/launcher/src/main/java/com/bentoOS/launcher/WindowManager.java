package com.bentoOS.launcher;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import java.util.ArrayList;
import java.util.List;

public class BentoWindowManager {

    private Context context;
    private FrameLayout windowContainer;
    private List<BentoWindow> openWindows;

    // BentoOS Design: 12dp window radius, #161616 surface
    private static final int WINDOW_RADIUS = 12;
    private static final int WINDOW_BACKGROUND = 0xFF161616;
    private static final int WINDOW_BORDER = 0xFF1e1e1e;

    public BentoWindowManager(Context context) {
        this.context = context;
        this.openWindows = new ArrayList<>();
    }

    public void init() {
        windowContainer = new FrameLayout(context);
        windowContainer.setBackgroundColor(0xFF0d0d0d);
    }

    public void openWindow(BentoWindow window) {
        openWindows.add(window);
        windowContainer.addView(window.getView());
        window.animateOpen();
    }

    public void closeWindow(BentoWindow window) {
        window.animateClose(() -> {
            openWindows.remove(window);
            windowContainer.removeView(window.getView());
        });
    }

    public void focusWindow(BentoWindow window) {
        // Bring to front
        window.getView().bringToFront();
    }

    public List<BentoWindow> getOpenWindows() {
        return openWindows;
    }
}