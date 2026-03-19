package com.bentoOS.assistant;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GeminiSidebar {

    private Context context;
    private LinearLayout sidebarView;
    private LinearLayout chatContainer;
    private ScrollView scrollView;
    private EditText inputField;
    private boolean isVisible = false;

    // BentoOS Design
    private static final int BG = 0xCC161616;
    private static final int SURFACE = 0xFF1e1e1e;
    private static final int ACCENT = 0xFF6c6cff;
    private static final int TEXT_PRIMARY = 0xFFe0e0e0;
    private static final int TEXT_SECONDARY = 0xFF666666;
    private static final int USER_BUBBLE = 0xFF1e1e1e;
    private static final int AI_BUBBLE = 0xFF252525;

    // Gemini API
    private static final String API_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent";
    private String apiKey = "";
    private List<String> conversationHistory = new ArrayList<>();

    public GeminiSidebar(Context context, String apiKey) {
        this.context = context;
        this.apiKey = apiKey;
        buildUI();
    }

    private void buildUI() {
        sidebarView = new LinearLayout(context);
        sidebarView.setOrientation(LinearLayout.VERTICAL);
        sidebarView.setBackgroundColor(BG);

        // Header
        LinearLayout header = new LinearLayout(context);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setBackgroundColor(SURFACE);
        header.setPadding(16, 12, 16, 12);

        TextView title = new TextView(context);
        title.setText("Gemini");
        title.setTextColor(TEXT_PRIMARY);
        title.setTextSize(15);

        TextView closeBtn = new TextView(context);
        closeBtn.setText("✕");
        closeBtn.setTextColor(TEXT_SECONDARY);
        closeBtn.setOnClickListener(v -> hide());

        header.addView(title);
        header.addView(closeBtn);

        // Chat area
        scrollView = new ScrollView(context);
        chatContainer = new LinearLayout(context);
        chatContainer.setOrientation(LinearLayout.VERTICAL);
        chatContainer.setPadding(16, 16, 16, 16);
        scrollView.addView(chatContainer);

        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        scrollView.setLayoutParams(scrollParams);

        // Input area
        LinearLayout inputArea = new LinearLayout(context);
        inputArea.setOrientation(LinearLayout.HORIZONTAL);
        inputArea.setBackgroundColor(SURFACE);
        inputArea.setPadding(12, 8, 12, 8);

        inputField = new EditText(context);
        inputField.setHint("Ask Gemini...");
        inputField.setTextColor(TEXT_PRIMARY);
        inputField.setHintTextColor(TEXT_SECONDARY);
        inputField.setBackgroundColor(0xFF0d0d0d);
        inputField.setPadding(12, 10, 12, 10);
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        inputField.setLayoutParams(inputParams);

        TextView sendBtn = new TextView(context);
        sendBtn.setText("→");
        sendBtn.setTextColor(ACCENT);
        sendBtn.setTextSize(20);
        sendBtn.setPadding(12, 0, 0, 0);
        sendBtn.setOnClickListener(v -> sendMessage());

        inputArea.addView(inputField);
        inputArea.addView(sendBtn);

        sidebarView.addView(header);
        sidebarView.addView(scrollView);
        sidebarView.addView(inputArea);

        sidebarView.setVisibility(View.GONE);
    }

    private void sendMessage() {
        String userMessage = inputField.getText().toString().trim();
        if (userMessage.isEmpty()) return;

        addBubble(userMessage, true);
        inputField.setText("");
        conversationHistory.add(userMessage);

        // Call Gemini API on background thread
        new Thread(() -> {
            String response = callGeminiApi(userMessage);
            if (context instanceof android.app.Activity) {
                ((android.app.Activity) context).runOnUiThread(() -> {
                    addBubble(response, false);
                    scrollView.fullScroll(View.FOCUS_DOWN);
                });
            }
        }).start();
    }

    private String callGeminiApi(String prompt) {
        try {
            URL url = new URL(API_URL + "?key=" + apiKey);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject body = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            JSONArray parts = new JSONArray();
            JSONObject part = new JSONObject();
            part.put("text", prompt);
            parts.put(part);
            content.put("parts", parts);
            contents.put(content);
            body.put("contents", contents);

            OutputStream os = conn.getOutputStream();
            os.write(body.toString().getBytes());
            os.close();

            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();

            JSONObject response = new JSONObject(sb.toString());
            return response
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text");

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private void addBubble(String message, boolean isUser) {
        TextView bubble = new TextView(context);
        bubble.setText(message);
        bubble.setTextColor(TEXT_PRIMARY);
        bubble.setTextSize(13);
        bubble.setBackgroundColor(isUser ? USER_BUBBLE : AI_BUBBLE);
        bubble.setPadding(12, 10, 12, 10);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 8);
        params.gravity = isUser ?
            android.view.Gravity.END : android.view.Gravity.START;
        bubble.setLayoutParams(params);

        chatContainer.addView(bubble);
    }

    public void toggle() {
        isVisible = !isVisible;
        if (isVisible) show(); else hide();
    }

    private void show() {
        sidebarView.setVisibility(View.VISIBLE);
        sidebarView.setTranslationX(sidebarView.getWidth());
        sidebarView.animate().translationX(0).setDuration(250).start();
    }

    private void hide() {
        sidebarView.animate()
            .translationX(sidebarView.getWidth())
            .setDuration(200)
            .withEndAction(() -> sidebarView.setVisibility(View.GONE))
            .start();
        isVisible = false;
    }

    public View getView() { return sidebarView; }
}