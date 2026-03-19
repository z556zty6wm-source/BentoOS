package com.bentoOS.launcher;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.animation.ObjectAnimator;
import android.animation.AnimatorSet;
import android.view.animation.OvershootInterpolator;

public class BentoWindow {

    private Context context;
    private FrameLayout windowView;
    private LinearLayout titleBar;
    private FrameLayout contentArea;
    private String title;

    // BentoOS Design
    private static final int BACKGROUND = 0xFF161616;
    private static final int TITLEBAR_BG = 0xFF1e1e1e;
    private static final int BORDER = 0xFF252525;
    private static final int TEXT_PRIMARY = 0xFFe0e0e0;

    // macOS traffic light colors
    private static final int BTN_CLOSE = 0xFFFF5F57;
    private static final int BTN_MINIMIZE = 0xFFFFBD2E;
    private static final int BTN_MAXIMIZE = 0xFF28C840;

    public BentoWindow(Context context, String title) {
        this.context = context;
        this.title = title;
        buildWindow();
    }

    private void buildWindow() {
        windowView = new FrameLayout(context);
        windowView.setBackgroundColor(BACKGROUND);

        // Title bar with traffic lights
        titleBar = new LinearLayout(context);
        titleBar.setOrientation(LinearLayout.HORIZONTAL);
        titleBar.setBackgroundColor(TITLEBAR_BG);

        // Traffic light buttons
        View closeBtn = makeTrafficLight(BTN_CLOSE);
        View minimizeBtn = makeTrafficLight(BTN_MINIMIZE);
        View maximizeBtn = makeTrafficLight(BTN_MAXIMIZE);

        titleBar.addView(closeBtn);
        titleBar.addView(minimizeBtn);
        titleBar.addView(maximizeBtn);

        // Window title
        TextView titleText = new TextView(context);
        titleText.setText(title);
        titleText.setTextColor(TEXT_PRIMARY);
        titleBar.addView(titleText);

        // Content area
        contentArea = new FrameLayout(context);
        contentArea.setBackgroundColor(BACKGROUND);

        windowView.addView(titleBar);
        windowView.addView(contentArea);
    }

    private View makeTrafficLight(int color) {
        View btn = new View(context);
        btn.setBackgroundColor(color);
        // 12dp circle
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(36, 36);
        params.setMargins(8, 0, 0, 0);
        btn.setLayoutParams(params);
        return btn;
    }

    public void animateOpen() {
        windowView.setAlpha(0f);
        windowView.setScaleX(0.95f);
        windowView.setScaleY(0.95f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(
            ObjectAnimator.ofFloat(windowView, "alpha", 0f, 1f),
            ObjectAnimator.ofFloat(windowView, "scaleX", 0.95f, 1f),
            ObjectAnimator.ofFloat(windowView, "scaleY", 0.95f, 1f)
        );
        set.setDuration(250);
        set.setInterpolator(new OvershootInterpolator(1.2f));
        set.start();
    }

    public void animateClose(Runnable onComplete) {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
            ObjectAnimator.ofFloat(windowView, "alpha", 1f, 0f),
            ObjectAnimator.ofFloat(windowView, "scaleX", 1f, 0.95f),
            ObjectAnimator.ofFloat(windowView, "scaleY", 1f, 0.95f)
        );
        set.setDuration(200);
        set.start();
        windowView.postDelayed(onComplete, 200);
    }

    public View getView() {
        return windowView;
    }

    public String getTitle() {
        return title;
    }
}