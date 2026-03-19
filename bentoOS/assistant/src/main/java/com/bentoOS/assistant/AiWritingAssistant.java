package com.bentoOS.assistant;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AiWritingAssistant {

    private Context context;
    private LinearLayout assistantView;
    private TextView suggestionView;
    private boolean isVisible = false;

    // BentoOS Design
    private static final int BG = 0xCC161616;
    private static final int SURFACE = 0xFF1e1e1e;
    private static final int ACCENT = 0xFF6c6cff;
    private static final int TEXT_PRIMARY = 0xFFe0e0e0;
    private static final int TEXT_SECONDARY = 0xFF666666;

    // Gemini API
    private static final String API_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent";
    private String apiKey = "";

    public AiWritingAssistant(Context context, String apiKey) {
        this.context = context;
        this.apiKey = apiKey;
        buildUI();
    }

    private void buildUI() {
        assistantView = new LinearLayout(context);
        assistantView.setOrientation(LinearLayout.VERTICAL);
        assistantView.setBackgroundColor(BG);
        assistantView.setPadding(16, 12, 16, 12);

        // Action buttons row
        LinearLayout actions = new LinearLayout(context);
        actions.setOrientation(LinearLayout.HORIZONTAL);

        String[] actions_list = {"Improve", "Summarize", "Translate", "Fix Grammar"};
        for (String action : actions_list) {
            TextView btn = new TextView(context);
            btn.setText(action);
            btn.setTextColor(ACCENT);
            btn.setTextSize(12);
            btn.setBackgroundColor(SURFACE);
            btn.setPadding(12, 8, 12, 8);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 8, 0);
            btn.setLayoutParams(params);
            btn.setOnClickListener(v -> processText(action, ""));
            actions.addView(btn);
        }

        // Suggestion output
        suggestionView = new TextView(context);
        suggestionView.setTextColor(TEXT_PRIMARY);
        suggestionView.setTextSize(13);
        suggestionView.setBackgroundColor(SURFACE);
        suggestionView.setPadding(12, 10, 12, 10);
        suggestionView.setText("Select text and choose an action...");

        LinearLayout.LayoutParams suggParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        suggParams.setMargins(0, 8, 0, 0);
        suggestionView.setLayoutParams(suggParams);

        assistantView.addView(actions);
        assistantView.addView(suggestionView);
        assistantView.setVisibility(View.GONE);
    }

    public void processText(String action, String selectedText) {
        if (selectedText.isEmpty()) return;

        String prompt = buildPrompt(action, selectedText);
        suggestionView.setText("Thinking...");

        new Thread(() -> {
            String result = callGeminiApi(prompt);
            if (context instanceof android.app.Activity) {
                ((android.app.Activity) context).runOnUiThread(() ->
                    suggestionView.setText(result));
            }
        }).start();
    }

    private String buildPrompt(String action, String text) {
        switch (action) {
            case "Improve":
                return "Improve this text, keep the same meaning: " + text;
            case "Summarize":
                return "Summarize this in 2-3 sentences: " + text;
            case "Translate":
                return "Translate this to English: " + text;
            case "Fix Grammar":
                return "Fix grammar and spelling errors: " + text;
            default:
                return text;
        }
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

    public void showForSelectedText(String text) {
        isVisible = true;
        assistantView.setVisibility(View.VISIBLE);
        processText("Improve", text);
    }

    public void hide() {
        isVisible = false;
        assistantView.setVisibility(View.GONE);
    }

    public View getView() { return assistantView; }
}