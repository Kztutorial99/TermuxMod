package com.termux.app.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.termux.R;
import com.termux.shared.termux.TermuxConstants;

/** Basic embedded browser for viewing help pages. */
public final class HelpActivity extends AppCompatActivity {

    WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#0D0D1A"));

        MaterialToolbar toolbar = new MaterialToolbar(this);
        toolbar.setTitle(getString(R.string.help_activity_title));
        toolbar.setTitleTextColor(Color.parseColor("#E8E8F0"));
        toolbar.setBackgroundColor(Color.parseColor("#131320"));
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationIconTint(Color.parseColor("#E8E8F0"));
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        LinearLayout.LayoutParams toolbarParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            (int) (getResources().getDisplayMetrics().density * 56));
        root.addView(toolbar, toolbarParams);

        final LinearProgressIndicator progressIndicator = new LinearProgressIndicator(this);
        progressIndicator.setIndeterminate(true);
        progressIndicator.setIndicatorColor(Color.parseColor("#00E5CC"));
        progressIndicator.setTrackColor(Color.parseColor("#1E1E30"));
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            (int) (getResources().getDisplayMetrics().density * 3));
        root.addView(progressIndicator, progressParams);

        mWebView = new WebView(this);
        mWebView.setBackgroundColor(Color.parseColor("#0D0D1A"));
        WebSettings settings = mWebView.getSettings();
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setJavaScriptEnabled(false);
        LinearLayout.LayoutParams webParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT);
        root.addView(mWebView, webParams);

        setContentView(root);
        setSupportActionBar(toolbar);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.equals(TermuxConstants.TERMUX_WIKI_URL) || url.startsWith(TermuxConstants.TERMUX_WIKI_URL + "/")) {
                    progressIndicator.setVisibility(View.VISIBLE);
                    return false;
                }

                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                } catch (ActivityNotFoundException e) {
                    progressIndicator.setVisibility(View.VISIBLE);
                    return false;
                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressIndicator.setVisibility(View.GONE);
                String darkCss =
                    "javascript:(function(){" +
                    "var style=document.createElement('style');" +
                    "style.innerHTML='body{background-color:#0D0D1A!important;color:#B8B8CC!important;}"+
                    "a{color:#00E5CC!important;}"+
                    "pre,code{background-color:#131320!important;color:#00E5CC!important;border-radius:8px;}"+
                    "h1,h2,h3,h4,h5,h6{color:#E8E8F0!important;}'+';';" +
                    "document.head.appendChild(style);" +
                    "})()";
                view.loadUrl(darkCss);
            }
        });
        mWebView.loadUrl(TermuxConstants.TERMUX_WIKI_URL);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onBackPressed() {
        if (mWebView != null && mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

}
