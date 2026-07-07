package com.termux.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.termux.R;

/**
 * Developer Info / About screen untuk TermuxMod.
 * Menampilkan informasi developer, social links, dan daftar fitur MOD.
 */
public class DeveloperInfoActivity extends Activity {

    private static final String YOUTUBE_URL  = "https://www.youtube.com/@Kz.tutorial";
    private static final String FACEBOOK_URL = "https://www.facebook.com/pangkey.jul";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer_info);

        // Back button
        View backBtn = findViewById(R.id.dev_info_back);
        if (backBtn != null) backBtn.setOnClickListener(v -> finish());

        // Social links
        View youtube = findViewById(R.id.dev_link_youtube);
        if (youtube != null) {
            youtube.setOnClickListener(v -> openUrl(YOUTUBE_URL));
        }

        View facebook = findViewById(R.id.dev_link_facebook);
        if (facebook != null) {
            facebook.setOnClickListener(v -> openUrl(FACEBOOK_URL));
        }

    }

    private void openUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception ignored) {
        }
    }
}
