package com.bentoOS.privacy;

import android.content.Context;
import android.net.VpnService;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

public class DnsBlocker {

    private Context context;
    private Set<String> blocklist = new HashSet<>();
    private boolean isEnabled = false;

    // Default blocklist sources
    private static final String[] BLOCKLIST_URLS = {
        "https://raw.githubusercontent.com/StevenBlack/hosts/master/hosts",
        "https://adaway.org/hosts.txt"
    };

    public DnsBlocker(Context context) {
        this.context = context;
        loadDefaultBlocklist();
    }

    private void loadDefaultBlocklist() {
        // Built-in common ad/tracker domains
        blocklist.add("doubleclick.net");
        blocklist.add("googleadservices.com");
        blocklist.add("googlesyndication.com");
        blocklist.add("ads.facebook.com");
        blocklist.add("graph.facebook.com");
        blocklist.add("scorecardresearch.com");
        blocklist.add("quantserve.com");
        blocklist.add("outbrain.com");
        blocklist.add("taboola.com");
        blocklist.add("moatads.com");
        blocklist.add("amazon-adsystem.com");
        blocklist.add("advertising.com");
        blocklist.add("crashlytics.com");
        blocklist.add("branch.io");
        blocklist.add("appsflyer.com");
    }

    public boolean isDomainBlocked(String domain) {
        if (!isEnabled) return false;
        // Check exact match
        if (blocklist.contains(domain)) return true;
        // Check parent domain
        String[] parts = domain.split("\\.");
        if (parts.length > 2) {
            String parent = parts[parts.length - 2] + "." + parts[parts.length - 1];
            return blocklist.contains(parent);
        }
        return false;
    }

    public void enable() {
        isEnabled = true;
    }

    public void disable() {
        isEnabled = false;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void addDomain(String domain) {
        blocklist.add(domain);
    }

    public void removeDomain(String domain) {
        blocklist.remove(domain);
    }

    public Set<String> getBlocklist() {
        return blocklist;
    }

    public int getBlocklistSize() {
        return blocklist.size();
    }
}