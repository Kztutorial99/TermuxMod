package com.termux.app.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.card.MaterialCardView;
import com.termux.R;
import com.termux.shared.settings.preferences.TermuxAppSharedPreferences;

/**
 * Terminal Appearance screen — background, BG animation, font, cursor, display settings.
 * Semua preference disimpan ke SharedPreferences "termuxmod_appearance".
 */
public class TerminalAppearanceActivity extends Activity {

    private static final String PREFS = "termuxmod_appearance";
    private static final String KEY_BG_ANIM      = "bg_animation"; // "off"|"matrix"|"cyber"|"neon"
    private static final String KEY_ANIM_OPACITY  = "anim_opacity";
    private static final String KEY_IMAGE_OPACITY = "image_opacity";
    private static final String KEY_BG_COLOR      = "bg_color";
    private static final String KEY_FONT_SIZE      = "font_size";
    private static final String KEY_FONT_COLOR     = "font_color";
    private static final String KEY_CURSOR_STYLE   = "cursor_style"; // "block"|"underline"|"bar"
    private static final String KEY_KEEP_SCREEN_ON = "keep_screen_on";
    private static final String KEY_CURSOR_BLINK   = "cursor_blink";

    private SharedPreferences mPrefs;
    private String mSelectedAnim = "off";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal_appearance);

        mPrefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        mSelectedAnim = mPrefs.getString(KEY_BG_ANIM, "off");

        // Back
        View back = findViewById(R.id.appearance_back);
        if (back != null) back.setOnClickListener(v -> finish());

        // Apply button
        View apply = findViewById(R.id.appearance_apply);
        if (apply != null) apply.setOnClickListener(v -> applyAndFinish());

        setupOpacitySlider();
        setupAnimSelector();
        setupAnimOpacitySlider();
        setupFontSizeSlider();
        setupFontPresets();
        setupCursorStyle();
        setupDisplayToggles();
    }

    private void setupOpacitySlider() {
        SeekBar seekBar = findViewById(R.id.appearance_opacity_seekbar);
        TextView valueLabel = findViewById(R.id.appearance_opacity_value);
        if (seekBar == null || valueLabel == null) return;

        int progress = mPrefs.getInt(KEY_IMAGE_OPACITY, 50);
        seekBar.setProgress(progress);
        valueLabel.setText(progress + "%");

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean f) {
                valueLabel.setText(p + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });
    }

    private void setupAnimSelector() {
        int[] animCardIds    = {R.id.anim_off, R.id.anim_matrix, R.id.anim_cyber_rain, R.id.anim_neon_pulse};
        String[] animKeys    = {"off", "matrix", "cyber", "neon"};
        TextView badge       = findViewById(R.id.appearance_anim_badge);

        updateAnimBadge(badge, mSelectedAnim);
        highlightSelectedAnim(animCardIds, animKeys, mSelectedAnim);

        for (int i = 0; i < animCardIds.length; i++) {
            final String key = animKeys[i];
            final int[] cards = animCardIds;
            final String[] keys = animKeys;
            View card = findViewById(animCardIds[i]);
            if (card != null) {
                card.setOnClickListener(v -> {
                    mSelectedAnim = key;
                    updateAnimBadge(badge, key);
                    highlightSelectedAnim(cards, keys, key);
                });
            }
        }
    }

    private void updateAnimBadge(TextView badge, String key) {
        if (badge == null) return;
        switch (key) {
            case "matrix": badge.setText("MATRIX"); break;
            case "cyber":  badge.setText("CYBER RAIN"); break;
            case "neon":   badge.setText("NEON PULSE"); break;
            default:       badge.setText(getString(R.string.appearance_anim_off)); break;
        }
    }

    private void highlightSelectedAnim(int[] cardIds, String[] keys, String selected) {
        for (int i = 0; i < cardIds.length; i++) {
            MaterialCardView card = (MaterialCardView) findViewById(cardIds[i]);
            if (card == null) continue;
            boolean isSelected = keys[i].equals(selected);
            card.setStrokeWidth(isSelected ? (int)(2 * getResources().getDisplayMetrics().density) : (int)(getResources().getDisplayMetrics().density));
            card.setStrokeColor(isSelected
                ? getResources().getColor(R.color.color_accent_primary)
                : getResources().getColor(R.color.color_border));
        }
    }

    private void setupAnimOpacitySlider() {
        SeekBar seekBar = findViewById(R.id.appearance_anim_opacity_seekbar);
        TextView valueLabel = findViewById(R.id.appearance_anim_opacity_value);
        if (seekBar == null || valueLabel == null) return;

        int progress = mPrefs.getInt(KEY_ANIM_OPACITY, 80);
        seekBar.setProgress(progress);
        valueLabel.setText(progress + "%");

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean f) {
                valueLabel.setText(p + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });
    }

    private void setupFontSizeSlider() {
        SeekBar seekBar = findViewById(R.id.appearance_font_seekbar);
        TextView valueLabel = findViewById(R.id.appearance_font_size_value);
        TextView minusBtn = null; // handled separately
        if (seekBar == null || valueLabel == null) return;

        // Font size range: 6-36sp, seekbar max=30 → size = progress + 6
        TermuxAppSharedPreferences prefs = TermuxAppSharedPreferences.build(this, false);
        int currentSize = (prefs != null) ? prefs.getFontSize() : 14;
        int progress = Math.max(0, Math.min(30, currentSize - 6));
        seekBar.setProgress(progress);
        valueLabel.setText(String.valueOf(currentSize));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean f) {
                valueLabel.setText(String.valueOf(p + 6));
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });

        // - and + buttons
        View minus = findViewById(R.id.appearance_font_minus);
        View plus  = findViewById(R.id.appearance_font_plus);
        if (minus != null) minus.setOnClickListener(v -> {
            int p = Math.max(0, seekBar.getProgress() - 1);
            seekBar.setProgress(p);
            valueLabel.setText(String.valueOf(p + 6));
        });
        if (plus != null) plus.setOnClickListener(v -> {
            int p = Math.min(30, seekBar.getProgress() + 1);
            seekBar.setProgress(p);
            valueLabel.setText(String.valueOf(p + 6));
        });
    }

    private void setupFontPresets() {
        int[] presetIds    = {R.id.font_preset_s, R.id.font_preset_m, R.id.font_preset_l, R.id.font_preset_xl};
        int[] presetSizes  = {10, 14, 18, 36};

        for (int i = 0; i < presetIds.length; i++) {
            final int size = presetSizes[i];
            View v = findViewById(presetIds[i]);
            if (v != null) {
                v.setOnClickListener(btn -> {
                    SeekBar sb = findViewById(R.id.appearance_font_seekbar);
                    TextView lbl = findViewById(R.id.appearance_font_size_value);
                    if (sb != null) sb.setProgress(Math.max(0, size - 6));
                    if (lbl != null) lbl.setText(String.valueOf(size));
                });
            }
        }
    }

    private void setupCursorStyle() {
        RadioGroup group = findViewById(R.id.appearance_cursor_style);
        if (group == null) return;
        String saved = mPrefs.getString(KEY_CURSOR_STYLE, "block");
        switch (saved) {
            case "underline": group.check(R.id.cursor_underline); break;
            case "bar":       group.check(R.id.cursor_bar);       break;
            default:          group.check(R.id.cursor_block);     break;
        }
    }

    private void setupDisplayToggles() {
        SwitchCompat keepOn = findViewById(R.id.appearance_keep_screen_on);
        SwitchCompat cursorBlink = findViewById(R.id.appearance_cursor_blink);
        if (keepOn != null)     keepOn.setChecked(mPrefs.getBoolean(KEY_KEEP_SCREEN_ON, false));
        if (cursorBlink != null) cursorBlink.setChecked(mPrefs.getBoolean(KEY_CURSOR_BLINK, false));
    }

    private void applyAndFinish() {
        SharedPreferences.Editor editor = mPrefs.edit();

        // BG animation
        editor.putString(KEY_BG_ANIM, mSelectedAnim);

        // Animation opacity
        SeekBar animOpacity = findViewById(R.id.appearance_anim_opacity_seekbar);
        if (animOpacity != null) editor.putInt(KEY_ANIM_OPACITY, animOpacity.getProgress());

        // Image opacity
        SeekBar imgOpacity = findViewById(R.id.appearance_opacity_seekbar);
        if (imgOpacity != null) editor.putInt(KEY_IMAGE_OPACITY, imgOpacity.getProgress());

        // Font size
        SeekBar fontSeek = findViewById(R.id.appearance_font_seekbar);
        if (fontSeek != null) {
            int size = fontSeek.getProgress() + 6;
            editor.putInt(KEY_FONT_SIZE, size);
            // Apply to TermuxAppSharedPreferences
            TermuxAppSharedPreferences prefs = TermuxAppSharedPreferences.build(this, false);
            if (prefs != null) prefs.setFontSize(size);
        }

        // Cursor style
        RadioGroup cursorGroup = findViewById(R.id.appearance_cursor_style);
        if (cursorGroup != null) {
            int checked = cursorGroup.getCheckedRadioButtonId();
            String cursorStyle = "block";
            if (checked == R.id.cursor_underline) cursorStyle = "underline";
            else if (checked == R.id.cursor_bar)  cursorStyle = "bar";
            editor.putString(KEY_CURSOR_STYLE, cursorStyle);
        }

        // Display toggles
        SwitchCompat keepOn = findViewById(R.id.appearance_keep_screen_on);
        SwitchCompat cursorBlink = findViewById(R.id.appearance_cursor_blink);
        if (keepOn != null)     editor.putBoolean(KEY_KEEP_SCREEN_ON, keepOn.isChecked());
        if (cursorBlink != null) editor.putBoolean(KEY_CURSOR_BLINK, cursorBlink.isChecked());

        editor.apply();
        setResult(RESULT_OK);
        finish();
    }
}
