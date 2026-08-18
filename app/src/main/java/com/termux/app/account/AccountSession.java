package com.termux.app.account;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Local, on-device cache of Firebase account metadata and user-editable
 * profile fields. Firebase Authentication remains the source of truth for
 * authentication state and credentials; passwords are never stored here.
 */
public class AccountSession {

    private static final String PREFS_NAME = "termux_account";

    private static final String KEY_ID = "account_id";
    private static final String KEY_NAME = "account_name";
    private static final String KEY_EMAIL = "account_email";
    private static final String KEY_PHOTO_URL = "account_photo_url";
    private static final String KEY_USERNAME = "profile_username";
    private static final String KEY_BIO = "profile_bio";
    private static final String KEY_SIGNED_IN_AT = "signed_in_at";

    @NonNull
    private final SharedPreferences mPreferences;

    public AccountSession(@NonNull Context context) {
        mPreferences = context.getApplicationContext()
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isSignedIn() {
        return getId() != null;
    }

    public void save(@Nullable String id, @Nullable String name, @Nullable String email,
                     @Nullable String photoUrl) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(KEY_ID, id);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PHOTO_URL, photoUrl);
        if (!mPreferences.contains(KEY_SIGNED_IN_AT))
            editor.putLong(KEY_SIGNED_IN_AT, System.currentTimeMillis());
        if (getUsername() == null && name != null)
            editor.putString(KEY_USERNAME, suggestUsername(name, email));
        editor.apply();
    }

    public void saveProfile(@Nullable String username, @Nullable String bio) {
        mPreferences.edit()
            .putString(KEY_USERNAME, username)
            .putString(KEY_BIO, bio)
            .apply();
    }

    public void clear() {
        mPreferences.edit().clear().apply();
    }

    @Nullable
    public String getId() { return emptyToNull(mPreferences.getString(KEY_ID, null)); }

    @Nullable
    public String getName() { return emptyToNull(mPreferences.getString(KEY_NAME, null)); }

    @Nullable
    public String getEmail() { return emptyToNull(mPreferences.getString(KEY_EMAIL, null)); }

    @Nullable
    public String getPhotoUrl() { return emptyToNull(mPreferences.getString(KEY_PHOTO_URL, null)); }

    @Nullable
    public String getUsername() { return emptyToNull(mPreferences.getString(KEY_USERNAME, null)); }

    @Nullable
    public String getBio() { return emptyToNull(mPreferences.getString(KEY_BIO, null)); }

    public long getSignedInAt() { return mPreferences.getLong(KEY_SIGNED_IN_AT, 0L); }

    @NonNull
    private static String suggestUsername(@NonNull String name, @Nullable String email) {
        String base = name;
        if (email != null && email.contains("@"))
            base = email.substring(0, email.indexOf('@'));
        String username = base.toLowerCase().replaceAll("[^a-z0-9_.]", "");
        return username.isEmpty() ? "termux_user" : username;
    }

    @Nullable
    private static String emptyToNull(@Nullable String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
