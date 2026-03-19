package com.bentoOS.systemui;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class NotificationCenter {

    private Context context;
    private LinearLayout notificationView;
    private LinearLayout notificationList;
    private boolean isVisible = false;
    private List<BentoNotification> notifications = new ArrayList<>();

    // BentoOS Design
    private static final int BG = 0xCC161616;
    private static final int SURFACE = 0xFF1e1e1e;
    private static final int BORDER = 0xFF1e1e1e;
    private static final int TEXT_PRIMARY = 0xFFe0e0e0;
    private static final int TEXT_SECONDARY = 0xFF666666;
    private static final int ACCENT = 0xFF6c6cff;

    public NotificationCenter(Context context) {
        this.context = context;
        buildUI();
    }

    private void buildUI() {
        notificationView = new LinearLayout(context);
        notificationView.setOrientation(LinearLayout.VERTICAL);
        notificationView.setBackgroundColor(BG);
        notificationView.setPadding(24, 24, 24, 24);

        // Header
        TextView header = new TextView(context);
        header.setText("Notifications");
        header.setTextColor(TEXT_PRIMARY);
        header.setTextSize(16);
        notificationView.addView(header);

        // Clear all button
        TextView clearAll = new TextView(context);
        clearAll.setText("Clear All");
        clearAll.setTextColor(ACCENT);
        clearAll.setTextSize(13);
        clearAll.setOnClickListener(v -> clearAll());
        notificationView.addView(clearAll);

        // Notification list
        notificationList = new LinearLayout(context);
        notificationList.setOrientation(LinearLayout.VERTICAL);
        notificationView.addView(notificationList);

        notificationView.setVisibility(View.GONE);
    }

    public void addNotification(String appName, String title, String body) {
        BentoNotification notif = new BentoNotification(appName, title, body);
        notifications.add(notif);
        renderNotification(notif);
    }

    private void renderNotification(BentoNotification notif) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(SURFACE);
        card.setPadding(16, 12, 16, 12);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 8);
        card.setLayoutParams(params);

        TextView appLabel = new TextView(context);
        appLabel.setText(notif.appName);
        appLabel.setTextColor(TEXT_SECONDARY);
        appLabel.setTextSize(11);

        TextView titleView = new TextView(context);
        titleView.setText(notif.title);
        titleView.setTextColor(TEXT_PRIMARY);
        titleView.setTextSize(14);

        TextView bodyView = new TextView(context);
        bodyView.setText(notif.body);
        bodyView.setTextColor(TEXT_SECONDARY);
        bodyView.setTextSize(12);

        card.addView(appLabel);
        card.addView(titleView);
        card.addView(bodyView);

        // Swipe to dismiss
        card.setOnLongClickListener(v -> {
            notifications.remove(notif);
            notificationList.removeView(card);
            return true;
        });

        notificationList.addView(card);
    }

    private void clearAll() {
        notifications.clear();
        notificationList.removeAllViews();
    }

    public void toggle() {
        isVisible = !isVisible;
        notificationView.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        if (isVisible) animateIn();
    }

    private void animateIn() {
        notificationView.setAlpha(0f);
        notificationView.setTranslationX(20f);
        notificationView.animate()
            .alpha(1f)
            .translationX(0f)
            .setDuration(250)
            .start();
    }

    public View getView() {
        return notificationView;
    }

    // Inner class
    static class BentoNotification {
        String appName, title, body;
        BentoNotification(String appName, String title, String body) {
            this.appName = appName;
            this.title = title;
            this.body = body;
        }
    }
}