package com.medusa.hands;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class MedusaAccessibilityService extends AccessibilityService {

    public static volatile MedusaAccessibilityService INSTANCE = null;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        INSTANCE = this;
        LocalServer.start(getApplicationContext());
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public void onDestroy() {
        INSTANCE = null;
        super.onDestroy();
    }

    public String collectScreenText() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return "";
        StringBuilder sb = new StringBuilder();
        walk(root, sb);
        return sb.toString();
    }

    private void walk(AccessibilityNodeInfo node, StringBuilder sb) {
        if (node == null) return;
        CharSequence t = node.getText();
        if (t != null && t.length() > 0) sb.append(t).append('\n');
        CharSequence d = node.getContentDescription();
        if (d != null && d.length() > 0) sb.append(d).append('\n');
        int n = node.getChildCount();
        for (int i = 0; i < n; i++) {
            walk(node.getChild(i), sb);
        }
    }
          }
