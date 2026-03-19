package com.bentoOS.assistant;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import java.util.ArrayList;
import java.util.List;

public class VoiceControl {

    private Context context;
    private SpeechRecognizer speechRecognizer;
    private boolean isListening = false;
    private VoiceCommandListener commandListener;

    public interface VoiceCommandListener {
        void onCommand(String command);
        void onListeningStarted();
        void onListeningStopped();
        void onError(String error);
    }

    // Built-in commands
    private static final String CMD_OPEN = "open";
    private static final String CMD_CLOSE = "close";
    private static final String CMD_SEARCH = "search";
    private static final String CMD_VOLUME_UP = "volume up";
    private static final String CMD_VOLUME_DOWN = "volume down";
    private static final String CMD_SCREENSHOT = "screenshot";
    private static final String CMD_GAMING_MODE = "gaming mode";
    private static final String CMD_CONTROL_CENTER = "control center";
    private static final String CMD_SPOTLIGHT = "spotlight";
    private static final String CMD_VPN = "vpn";

    public VoiceControl(Context context) {
        this.context = context;
        initSpeechRecognizer();
    }

    private void initSpeechRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) return;

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            public void onReadyForSpeech(Bundle params) {
                isListening = true;
                if (commandListener != null)
                    commandListener.onListeningStarted();
            }
            public void onResults(Bundle results) {
                isListening = false;
                ArrayList<String> matches = results.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    processCommand(matches.get(0).toLowerCase());
                }
                if (commandListener != null)
                    commandListener.onListeningStopped();
            }
            public void onError(int error) {
                isListening = false;
                if (commandListener != null)
                    commandListener.onError("Speech error: " + error);
            }
            public void onBeginningOfSpeech() {}
            public void onRmsChanged(float rmsdB) {}
            public void onBufferReceived(byte[] buffer) {}
            public void onEndOfSpeech() {}
            public void onPartialResults(Bundle partialResults) {}
            public void onEvent(int eventType, Bundle params) {}
        });
    }

    private void processCommand(String command) {
        if (commandListener == null) return;

        if (command.startsWith(CMD_OPEN)) {
            String appName = command.replace(CMD_OPEN, "").trim();
            commandListener.onCommand("OPEN:" + appName);
        } else if (command.startsWith(CMD_SEARCH)) {
            String query = command.replace(CMD_SEARCH, "").trim();
            commandListener.onCommand("SEARCH:" + query);
        } else if (command.contains(CMD_VOLUME_UP)) {
            commandListener.onCommand("VOLUME_UP");
        } else if (command.contains(CMD_VOLUME_DOWN)) {
            commandListener.onCommand("VOLUME_DOWN");
        } else if (command.contains(CMD_SCREENSHOT)) {
            commandListener.onCommand("SCREENSHOT");
        } else if (command.contains(CMD_GAMING_MODE)) {
            commandListener.onCommand("GAMING_MODE");
        } else if (command.contains(CMD_CONTROL_CENTER)) {
            commandListener.onCommand("CONTROL_CENTER");
        } else if (command.contains(CMD_SPOTLIGHT)) {
            commandListener.onCommand("SPOTLIGHT");
        } else if (command.contains(CMD_VPN)) {
            commandListener.onCommand("VPN");
        } else {
            // Send unknown commands to Gemini
            commandListener.onCommand("GEMINI:" + command);
        }
    }

    public void startListening() {
        if (speechRecognizer == null || isListening) return;

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        speechRecognizer.startListening(intent);
    }

    public void stopListening() {
        if (speechRecognizer != null && isListening) {
            speechRecognizer.stopListening();
            isListening = false;
        }
    }

    public void setCommandListener(VoiceCommandListener listener) {
        this.commandListener = listener;
    }

    public boolean isListening() { return isListening; }

    public void destroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}