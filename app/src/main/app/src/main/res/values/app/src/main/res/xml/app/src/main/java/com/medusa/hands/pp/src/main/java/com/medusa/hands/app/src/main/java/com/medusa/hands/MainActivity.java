package com.medusa.hands;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalServer.start(getApplicationContext());

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        box.setPadding(pad, pad, pad, pad);

        status = new TextView(this);
        status.setTextSize(15);
        box.addView(status);

        Button openSettings = new Button(this);
        openSettings.setText("1) Accessibility সেটিংস খুলুন");
        openSettings.setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        box.addView(openSettings);

        Button refresh = new Button(this);
        refresh.setText("2) অবস্থা রিফ্রেশ করুন");
        refresh.setOnClickListener(v -> updateStatus());
        box.addView(refresh);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(box);
        setContentView(scroll);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }

    private void updateStatus() {
        boolean accOn = isAccessibilityOn();
        StringBuilder sb = new StringBuilder();
        sb.append("সার্ভার: 127.0.0.1:").append(LocalServer.PORT).append("\n");
        sb.append("টোকেন: ").append(LocalServer.TOKEN).append("\n");
        sb.append("Accessibility: ").append(accOn ? "চালু ✅" : "বন্ধ ❌").append("\n\n");
        sb.append("Termux-এ টেস্ট কমান্ড:\n");
        sb.append("curl \"http://127.0.0.1:").append(LocalServer.PORT)
          .append("/screen?token=").append(LocalServer.TOKEN).append("\"");
        status.setText(sb.toString());
    }

    private boolean isAccessibilityOn() {
        String enabled = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (enabled == null) return false;
        String me = getPackageName() + "/" + MedusaAccessibilityService.class.getName();
        for (String s : enabled.split(":")) {
            if (s.equalsIgnoreCase(me)) return true;
        }
        return false;
    }
                  }
