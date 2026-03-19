package com.bentoOS.assistant;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class OtaUpdater {

    private Context context;
    private SharedPreferences prefs;
    private UpdateListener listener;

    private static final String PREFS_NAME = "bento_ota_prefs";
    private static final String KEY_CURRENT_VERSION = "current_version";
    private static final String KEY_LAST_CHECK = "last_check";

    // GitHub releases API
    private static final String UPDATE_URL =
        "https://api.github.com/repos/bentoOS/bentoOS/releases/latest";
    private static final String CURRENT_VERSION = "0.1.0-alpha";

    public interface UpdateListener {
        void onUpdateAvailable(String version, String downloadUrl);
        void onNoUpdate();
        void onDownloadProgress(int percent);
        void onUpdateReady(String filePath);
        void onError(String error);
    }

    public OtaUpdater(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void checkForUpdates() {
        new Thread(() -> {
            try {
                URL url = new URL(UPDATE_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                JSONObject release = new JSONObject(sb.toString());
                String latestVersion = release.getString("tag_name");
                String downloadUrl = release
                    .getJSONArray("assets")
                    .getJSONObject(0)
                    .getString("browser_download_url");

                prefs.edit().putLong(KEY_LAST_CHECK,
                    System.currentTimeMillis()).apply();

                if (isNewerVersion(latestVersion, CURRENT_VERSION)) {
                    if (listener != null)
                        listener.onUpdateAvailable(latestVersion, downloadUrl);
                } else {
                    if (listener != null) listener.onNoUpdate();
                }

            } catch (Exception e) {
                if (listener != null) listener.onError(e.getMessage());
            }
        }).start();
    }

    public void downloadUpdate(String downloadUrl) {
        new Thread(() -> {
            try {
                URL url = new URL(downloadUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                int fileSize = conn.getContentLength();

                File outputFile = new File(
                    context.getExternalFilesDir(null), "bentoOS_update.zip");
                FileOutputStream fos = new FileOutputStream(outputFile);
                InputStream is = conn.getInputStream();

                byte[] buffer = new byte[4096];
                int downloaded = 0;
                int read;

                while ((read = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                    downloaded += read;
                    int progress = (int) ((downloaded / (float) fileSize) * 100);
                    if (listener != null) listener.onDownloadProgress(progress);
                }

                fos.close();
                is.close();

                if (listener != null)
                    listener.onUpdateReady(outputFile.getAbsolutePath());

            } catch (Exception e) {
                if (listener != null) listener.onError(e.getMessage());
            }
        }).start();
    }

    private boolean isNewerVersion(String latest, String current) {
        try {
            String[] l = latest.replace("v", "").split("\\.");
            String[] c = current.replace("v", "").split("\\.");
            for (int i = 0; i < Math.min(l.length, c.length); i++) {
                int lv = Integer.parseInt(l[i].replaceAll("[^0-9]", ""));
                int cv = Integer.parseInt(c[i].replaceAll("[^0-9]", ""));
                if (lv > cv) return true;
                if (lv < cv) return false;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public String getCurrentVersion() { return CURRENT_VERSION; }

    public long getLastCheckTime() {
        return prefs.getLong(KEY_LAST_CHECK, 0);
    }

    public void setUpdateListener(UpdateListener listener) {
        this.listener = listener;
    }
}