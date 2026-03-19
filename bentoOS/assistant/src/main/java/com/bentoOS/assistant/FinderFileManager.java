package com.bentoOS.assistant;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FinderFileManager {

    private Context context;
    private LinearLayout finderView;
    private LinearLayout fileList;
    private TextView currentPathView;
    private File currentDirectory;

    // BentoOS Design
    private static final int BG = 0xFF0d0d0d;
    private static final int SURFACE = 0xFF161616;
    private static final int SIDEBAR_BG = 0xFF111111;
    private static final int ACCENT = 0xFF6c6cff;
    private static final int TEXT_PRIMARY = 0xFFe0e0e0;
    private static final int TEXT_SECONDARY = 0xFF666666;
    private static final int BORDER = 0xFF1e1e1e;

    // Sidebar locations
    private static final String[] SIDEBAR_ITEMS = {
        "Home", "Documents", "Downloads",
        "Pictures", "Music", "Videos"
    };

    public FinderFileManager(Context context) {
        this.context = context;
        this.currentDirectory = android.os.Environment
            .getExternalStorageDirectory();
        buildUI();
    }

    private void buildUI() {
        finderView = new LinearLayout(context);
        finderView.setOrientation(LinearLayout.VERTICAL);
        finderView.setBackgroundColor(BG);

        // Toolbar
        LinearLayout toolbar = new LinearLayout(context);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setBackgroundColor(SURFACE);
        toolbar.setPadding(16, 10, 16, 10);

        // Back button
        TextView backBtn = new TextView(context);
        backBtn.setText("←");
        backBtn.setTextColor(TEXT_PRIMARY);
        backBtn.setTextSize(18);
        backBtn.setOnClickListener(v -> navigateUp());

        // Current path
        currentPathView = new TextView(context);
        currentPathView.setText(currentDirectory.getAbsolutePath());
        currentPathView.setTextColor(TEXT_SECONDARY);
        currentPathView.setTextSize(12);
        LinearLayout.LayoutParams pathParams = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        currentPathView.setLayoutParams(pathParams);

        // View toggle
        TextView viewToggle = new TextView(context);
        viewToggle.setText("⊞");
        viewToggle.setTextColor(TEXT_PRIMARY);
        viewToggle.setTextSize(18);

        toolbar.addView(backBtn);
        toolbar.addView(currentPathView);
        toolbar.addView(viewToggle);

        // Main area — sidebar + file list
        LinearLayout mainArea = new LinearLayout(context);
        mainArea.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams mainParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        mainArea.setLayoutParams(mainParams);

        // Sidebar
        LinearLayout sidebar = buildSidebar();
        LinearLayout.LayoutParams sidebarParams = new LinearLayout.LayoutParams(
            200, LinearLayout.LayoutParams.MATCH_PARENT);
        sidebar.setLayoutParams(sidebarParams);

        // File list
        fileList = new LinearLayout(context);
        fileList.setOrientation(LinearLayout.VERTICAL);
        fileList.setBackgroundColor(BG);

        mainArea.addView(sidebar);
        mainArea.addView(fileList);

        finderView.addView(toolbar);
        finderView.addView(mainArea);

        loadDirectory(currentDirectory);
    }

    private LinearLayout buildSidebar() {
        LinearLayout sidebar = new LinearLayout(context);
        sidebar.setOrientation(LinearLayout.VERTICAL);
        sidebar.setBackgroundColor(SIDEBAR_BG);
        sidebar.setPadding(0, 8, 0, 8);

        for (String item : SIDEBAR_ITEMS) {
            TextView sidebarItem = new TextView(context);
            sidebarItem.setText(item);
            sidebarItem.setTextColor(TEXT_PRIMARY);
            sidebarItem.setTextSize(13);
            sidebarItem.setPadding(16, 10, 16, 10);
            sidebarItem.setOnClickListener(v -> navigateTo(item));
            sidebar.addView(sidebarItem);
        }

        return sidebar;
    }

    private void loadDirectory(File directory) {
        fileList.removeAllViews();
        currentDirectory = directory;
        currentPathView.setText(directory.getAbsolutePath());

        File[] files = directory.listFiles();
        if (files == null) return;

        Arrays.sort(files, (a, b) -> {
            if (a.isDirectory() && !b.isDirectory()) return -1;
            if (!a.isDirectory() && b.isDirectory()) return 1;
            return a.getName().compareToIgnoreCase(b.getName());
        });

        for (File file : files) {
            if (file.getName().startsWith(".")) continue;
            fileList.addView(buildFileRow(file));
        }
    }

    private View buildFileRow(File file) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(16, 10, 16, 10);
        row.setBackgroundColor(BG);

        // Icon
        TextView icon = new TextView(context);
        icon.setText(file.isDirectory() ? "📁" : getFileIcon(file.getName()));
        icon.setTextSize(16);
        icon.setPadding(0, 0, 12, 0);

        // Name
        TextView name = new TextView(context);
        name.setText(file.getName());
        name.setTextColor(TEXT_PRIMARY);
        name.setTextSize(13);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        name.setLayoutParams(nameParams);

        // Size / date
        TextView info = new TextView(context);
        info.setText(file.isDirectory() ? "" : formatSize(file.length()));
        info.setTextColor(TEXT_SECONDARY);
        info.setTextSize(11);

        row.addView(icon);
        row.addView(name);
        row.addView(info);

        row.setOnClickListener(v -> {
            if (file.isDirectory()) loadDirectory(file);
            else openFile(file);
        });

        return row;
    }

    private String getFileIcon(String name) {
        if (name.endsWith(".jpg") || name.endsWith(".png")) return "🖼";
        if (name.endsWith(".mp3") || name.endsWith(".wav")) return "🎵";
        if (name.endsWith(".mp4") || name.endsWith(".mkv")) return "🎬";
        if (name.endsWith(".pdf")) return "📄";
        if (name.endsWith(".zip") || name.endsWith(".tar")) return "🗜";
        if (name.endsWith(".apk")) return "📦";
        return "📄";
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + " KB";
        return (bytes / (1024 * 1024)) + " MB";
    }

    private void navigateUp() {
        File parent = currentDirectory.getParentFile();
        if (parent != null) loadDirectory(parent);
    }

    private void navigateTo(String location) {
        String base = android.os.Environment
            .getExternalStorageDirectory().getAbsolutePath();
        switch (location) {
            case "Home": loadDirectory(new File(base)); break;
            case "Documents": loadDirectory(new File(base + "/Documents")); break;
            case "Downloads": loadDirectory(new File(base + "/Download")); break;
            case "Pictures": loadDirectory(new File(base + "/Pictures")); break;
            case "Music": loadDirectory(new File(base + "/Music")); break;
            case "Videos": loadDirectory(new File(base + "/Movies")); break;
        }
    }

    private void openFile(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        context.startActivity(intent);
    }

    public View getView() { return finderView; }
}