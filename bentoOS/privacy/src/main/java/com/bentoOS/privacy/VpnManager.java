package com.bentoOS.privacy;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.VpnService;

public class VpnManager {

    private Context context;
    private SharedPreferences prefs;
    private boolean isConnected = false;
    private String currentServer = null;

    private static final String PREFS_NAME = "bento_vpn_prefs";
    private static final String KEY_SERVER = "last_server";
    private static final String KEY_PORT = "last_port";
    private static final String KEY_AUTO_CONNECT = "auto_connect";

    // WireGuard config
    private String privateKey;
    private String publicKey;
    private String serverEndpoint;
    private int serverPort = 51820;
    private String allowedIPs = "0.0.0.0/0";
    private String dns = "1.1.1.1";

    public VpnManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadConfig();
    }

    private void loadConfig() {
        currentServer = prefs.getString(KEY_SERVER, null);
        serverPort = prefs.getInt(KEY_PORT, 51820);
    }

    public void configure(String privateKey, String publicKey,
                          String endpoint, int port) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.serverEndpoint = endpoint;
        this.serverPort = port;

        prefs.edit()
            .putString(KEY_SERVER, endpoint)
            .putInt(KEY_PORT, port)
            .apply();
    }

    public boolean connect() {
        if (privateKey == null || serverEndpoint == null) {
            return false;
        }
        // WireGuard connection logic handled by native layer
        isConnected = true;
        currentServer = serverEndpoint;
        return true;
    }

    public void disconnect() {
        isConnected = false;
        currentServer = null;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public String getCurrentServer() {
        return currentServer;
    }

    public void setAutoConnect(boolean auto) {
        prefs.edit().putBoolean(KEY_AUTO_CONNECT, auto).apply();
    }

    public boolean isAutoConnect() {
        return prefs.getBoolean(KEY_AUTO_CONNECT, false);
    }

    public void setDns(String dns) {
        this.dns = dns;
    }

    public String getDns() {
        return dns;
    }

    public String getStatus() {
        if (isConnected) return "Connected to " + currentServer;
        return "Disconnected";
    }
}