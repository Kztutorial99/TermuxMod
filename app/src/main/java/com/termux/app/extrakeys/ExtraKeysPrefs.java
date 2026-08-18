package com.termux.app.extrakeys;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Preferensi urutan & jumlah Extra Keys yang bisa diatur user.
 *
 * Extra keys ditampilkan dalam satu baris yang bisa digeser ke kiri/kanan,
 * dengan {@link #getKeysPerScreen(Context)} tombol yang terlihat sekaligus.
 */
public final class ExtraKeysPrefs {

    private static final String PREFS_NAME = "termux_extra_keys";
    private static final String KEY_ORDER = "order";
    private static final String KEY_PER_SCREEN = "keys_per_screen";

    /** Jumlah tombol yang terlihat sekaligus (sisanya digeser). */
    public static final int DEFAULT_KEYS_PER_SCREEN = 6;

    /** Urutan default extra keys. */
    public static final List<String> DEFAULT_ORDER = Arrays.asList(
        "ESC", "TAB", "CTRL", "ALT", "-", "/",
        "LEFT", "DOWN", "UP", "RIGHT",
        "HOME", "END", "PGUP", "PGDN",
        "KEYBOARD", "PASTE");

    /** Semua tombol yang bisa dipilih user di layar pengaturan. */
    public static final List<String> AVAILABLE_KEYS = Arrays.asList(
        "ESC", "TAB", "CTRL", "ALT", "SHIFT", "FN",
        "-", "_", "/", "|", "\\", "~", "*", "&", "$", "#", "%", "^", "!", "?",
        "LEFT", "DOWN", "UP", "RIGHT",
        "HOME", "END", "PGUP", "PGDN",
        "BKSP", "DEL", "INS", "ENTER", "SPACE",
        "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10", "F11", "F12",
        "KEYBOARD", "PASTE", "DRAWER");

    private ExtraKeysPrefs() {}

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Urutan extra keys aktif milik user (atau default kalau belum diatur). */
    public static List<String> getOrder(Context context) {
        String raw = prefs(context).getString(KEY_ORDER, null);
        if (raw == null || raw.trim().isEmpty()) return new ArrayList<>(DEFAULT_ORDER);

        List<String> keys = new ArrayList<>();
        for (String part : raw.split("\u001F")) {
            if (!part.trim().isEmpty()) keys.add(part);
        }
        return keys.isEmpty() ? new ArrayList<>(DEFAULT_ORDER) : keys;
    }

    public static void saveOrder(Context context, List<String> keys) {
        LinkedHashSet<String> unique = new LinkedHashSet<>(keys);
        StringBuilder sb = new StringBuilder();
        for (String k : unique) {
            if (sb.length() > 0) sb.append('\u001F');
            sb.append(k);
        }
        prefs(context).edit().putString(KEY_ORDER, sb.toString()).apply();
    }

    public static void resetOrder(Context context) {
        prefs(context).edit().remove(KEY_ORDER).apply();
    }

    public static int getKeysPerScreen(Context context) {
        int value = prefs(context).getInt(KEY_PER_SCREEN, DEFAULT_KEYS_PER_SCREEN);
        if (value < 4) value = 4;
        if (value > 10) value = 10;
        return value;
    }

    public static void setKeysPerScreen(Context context, int value) {
        prefs(context).edit().putInt(KEY_PER_SCREEN, value).apply();
    }

    /**
     * Membangun nilai properti extra keys (json array of arrays) dari urutan user.
     * Selalu satu baris agar bisa digeser horizontal.
     */
    public static String buildExtraKeysJson(Context context) {
        List<String> keys = getOrder(context);
        StringBuilder sb = new StringBuilder("[[");
        for (int i = 0; i < keys.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append('"').append(escape(keys.get(i))).append('"');
        }
        sb.append("]]");
        return sb.toString();
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
