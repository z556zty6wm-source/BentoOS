package com.bentoOS.privacy;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PrivacyIndicator {

    private Context context;
    private WindowManager windowManager;
    private LinearLayout indicatorView;

    // Indicator states
    private boolean micActive = false;
    private boolean cameraActive = false;

    // BentoOS Design
    private static final int MIC_COLOR = 0xFFFF5F57;
    private static final int CAM_COLOR = 0xFFFF5F57;
    private static final int BG = 0xFF161616;
    private static final int TEXT_COLOR = 0xFFe0e0e0;

    public PrivacyIndicator(Context context) {
        this.context = context;
        this.windowManager = (WindowManager)
            context.getSystemService(Context.WINDOW_SERVICE);
        buildUI();
    }

    private void buildUI() {
        indicatorView = new LinearLayout(context);
        indicatorView.setOrientation(LinearLayout.HORIZONTAL);
        indicatorView.setBackgroundColor(BG);
        indicatorView.setPadding(12, 6, 12, 6);

        // Mic indicator dot
        View micDot = new View(context);
        micDot.setBackgroundColor(MIC_COLOR);
        LinearLayout.LayoutParams dotParams =
            new LinearLayout.LayoutParams(8, 8);
        dotParams.setMargins(0, 0, 6, 0);
        micDot.setLayoutParams(dotParams);

        TextView micLabel = new TextView(context);
        micLabel.setText("MIC");
        micLabel.setTextColor(TEXT_COLOR);
        micLabel.setTextSize(10);

        // Camera indicator dot
        View camDot = new View(context);
        camDot.setBackgroundColor(CAM_COLOR);
        camDot.setLayoutParams(dotParams);

        TextView camLabel = new TextView(context);
        camLabel.setText("CAM");
        camLabel.setTextColor(TEXT_COLOR);
        camLabel.setTextSize(10);

        indicatorView.addView(micDot);
        indicatorView.addView(micLabel);
        indicatorView.addView(camDot);
        indicatorView.addView(camLabel);
    }

    public void showMicIndicator() {
        micActive = true;
        updateVisibility();
    }

    public void hideMicIndicator() {
        micActive = false;
        updateVisibility();
    }

    public void showCameraIndicator() {
        cameraActive = true;
        updateVisibility();
    }

    public void hideCameraIndicator() {
        cameraActive = false;
        updateVisibility();
    }

    private void updateVisibility() {
        if (micActive || cameraActive) {
            indicatorView.setVisibility(View.VISIBLE);
        } else {
            indicatorView.setVisibility(View.GONE);
        }
    }

    public boolean isMicActive() { return micActive; }
    public boolean isCameraActive() { return cameraActive; }

    public View getView() { return indicatorView; }
}