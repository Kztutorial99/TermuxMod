package com.termux.app.extrakeys;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.termux.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Layar pengaturan urutan Extra Keys.
 * User bisa menyalakan/mematikan tombol dan memindah posisinya naik/turun
 * (posisi paling atas = paling kiri di bar extra keys).
 */
public class ExtraKeysOrderActivity extends AppCompatActivity {

    private LinearLayout mContainer;
    private final List<String> mEnabled = new ArrayList<>();
    private final List<String> mDisabled = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extrakeys_order);

        mContainer = findViewById(R.id.extrakeys_order_container);

        View back = findViewById(R.id.btn_extrakeys_back);
        if (back != null) back.setOnClickListener(v -> finish());

        View reset = findViewById(R.id.btn_extrakeys_reset);
        if (reset != null) reset.setOnClickListener(v -> {
            ExtraKeysPrefs.resetOrder(this);
            loadKeys();
            render();
            Toast.makeText(this, R.string.extrakeys_order_reset_done, Toast.LENGTH_SHORT).show();
        });

        loadKeys();
        render();
    }

    private void loadKeys() {
        mEnabled.clear();
        mDisabled.clear();
        mEnabled.addAll(ExtraKeysPrefs.getOrder(this));
        for (String key : ExtraKeysPrefs.AVAILABLE_KEYS) {
            if (!mEnabled.contains(key)) mDisabled.add(key);
        }
    }

    private void persist() {
        ExtraKeysPrefs.saveOrder(this, mEnabled);
    }

    private void render() {
        mContainer.removeAllViews();

        addHeader(getString(R.string.extrakeys_order_active));
        for (int i = 0; i < mEnabled.size(); i++) {
            mContainer.addView(buildRow(mEnabled.get(i), true, i));
        }

        addHeader(getString(R.string.extrakeys_order_inactive));
        for (String key : mDisabled) {
            mContainer.addView(buildRow(key, false, -1));
        }
    }

    private void addHeader(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(getResources().getColor(R.color.color_text_secondary));
        tv.setTextSize(13);
        tv.setPadding(dp(4), dp(18), dp(4), dp(6));
        mContainer.addView(tv);
    }

    private View buildRow(final String key, final boolean enabled, final int index) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12), dp(10), dp(12), dp(10));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(getResources().getColor(R.color.color_surface_high));
        bg.setCornerRadius(dp(10));
        row.setBackground(bg);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, dp(6), 0, 0);
        row.setLayoutParams(rowParams);

        CheckBox check = new CheckBox(this);
        check.setChecked(enabled);
        check.setText(key);
        check.setTextColor(getResources().getColor(R.color.color_text_primary));
        check.setTextSize(15);
        LinearLayout.LayoutParams checkParams = new LinearLayout.LayoutParams(0,
            LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        check.setLayoutParams(checkParams);
        check.setOnClickListener(v -> {
            if (enabled) {
                mEnabled.remove(key);
                if (!mDisabled.contains(key)) mDisabled.add(key);
            } else {
                mDisabled.remove(key);
                mEnabled.add(key);
            }
            persist();
            render();
        });
        row.addView(check);

        if (enabled) {
            row.addView(buildMoveButton("↑", index > 0, () -> move(index, index - 1)));
            row.addView(buildMoveButton("↓", index < mEnabled.size() - 1, () -> move(index, index + 1)));
        }

        return row;
    }

    private TextView buildMoveButton(String label, boolean active, final Runnable action) {
        TextView tv = new TextView(this);
        tv.setText(label);
        tv.setTextSize(18);
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(active
            ? getResources().getColor(R.color.color_accent_primary)
            : Color.parseColor("#55FFFFFF"));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(44), dp(38));
        params.setMargins(dp(4), 0, 0, 0);
        tv.setLayoutParams(params);
        tv.setClickable(active);
        if (active) tv.setOnClickListener(v -> action.run());
        return tv;
    }

    private void move(int from, int to) {
        if (from < 0 || to < 0 || from >= mEnabled.size() || to >= mEnabled.size()) return;
        String key = mEnabled.remove(from);
        mEnabled.add(to, key);
        persist();
        render();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
