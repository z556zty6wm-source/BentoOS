package com.bentoOS.privacy;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashMap;
import java.util.Map;

public class PerAppNetwork {

    private Context context;
    private SharedPreferences prefs;
    private Map<String, NetworkPolicy> appPolicies = new HashMap<>();

    private static final String PREFS_NAME = "bento_network_policies";

    public enum NetworkPolicy {
        ALLOW_ALL,
        WIFI_ONLY,
        VPN_ONLY,
        BLOCK_ALL
    }

    public PerAppNetwork(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadPolicies();
    }

    private void loadPolicies() {
        Map<String, ?> saved = prefs.getAll();
        for (Map.Entry<String, ?> entry : saved.entrySet()) {
            try {
                NetworkPolicy policy = NetworkPolicy.valueOf((String) entry.getValue());
                appPolicies.put(entry.getKey(), policy);
            } catch (Exception e) {
                // Skip invalid entries
            }
        }
    }

    public void setPolicy(String packageName, NetworkPolicy policy) {
        appPolicies.put(packageName, policy);
        prefs.edit().putString(packageName, policy.name()).apply();
    }

    public NetworkPolicy getPolicy(String packageName) {
        return appPolicies.getOrDefault(packageName, NetworkPolicy.ALLOW_ALL);
    }

    public boolean isAllowed(String packageName, boolean isWifi, boolean isVpn) {
        NetworkPolicy policy = getPolicy(packageName);
        switch (policy) {
            case ALLOW_ALL: return true;
            case WIFI_ONLY: return isWifi;
            case VPN_ONLY: return isVpn;
            case BLOCK_ALL: return false;
            default: return true;
        }
    }

    public void resetPolicy(String packageName) {
        appPolicies.remove(packageName);
        prefs.edit().remove(packageName).apply();
    }

    public Map<String, NetworkPolicy> getAllPolicies() {
        return appPolicies;
    }
}