package com.termux.app.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.auth.UserInfo;
import com.termux.R;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Menu Profile User TermuxMod.
 * Menampilkan info akun Google/Firebase user secara lengkap dalam Bahasa Indonesia.
 */
public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private LinearLayout mInfoContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        mInfoContainer = findViewById(R.id.profile_info_container);

        View back = findViewById(R.id.btn_profile_back);
        if (back != null) back.setOnClickListener(v -> finish());

        View settings = findViewById(R.id.btn_profile_settings);
        if (settings != null) settings.setOnClickListener(v ->
            startActivity(new Intent(ProfileActivity.this, SettingsActivity.class)));

        findViewById(R.id.btn_profile_refresh).setOnClickListener(v -> reloadUser());
        findViewById(R.id.btn_profile_logout).setOnClickListener(v -> confirmLogout());
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        bindUser(user);
    }

    private void reloadUser() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        user.reload().addOnCompleteListener(task -> {
            FirebaseUser fresh = mAuth.getCurrentUser();
            if (fresh != null) bindUser(fresh);
            Toast.makeText(this, R.string.profile_refreshed, Toast.LENGTH_SHORT).show();
        });
    }

    private void bindUser(FirebaseUser user) {
        TextView name = findViewById(R.id.profile_name);
        TextView email = findViewById(R.id.profile_email);
        ImageView photo = findViewById(R.id.profile_photo);

        String displayName = isEmpty(user.getDisplayName()) ? getString(R.string.profile_value_empty) : user.getDisplayName();
        name.setText(displayName);
        email.setText(isEmpty(user.getEmail()) ? getString(R.string.profile_value_empty) : user.getEmail());
        loadPhoto(user.getPhotoUrl(), photo);

        mInfoContainer.removeAllViews();
        addRow(R.string.profile_field_name, displayName);
        addRow(R.string.profile_field_email, user.getEmail());
        addRow(R.string.profile_field_email_verified,
            getString(user.isEmailVerified() ? R.string.profile_verified : R.string.profile_not_verified));
        addRow(R.string.profile_field_phone, user.getPhoneNumber());
        addRow(R.string.profile_field_uid, user.getUid());
        addRow(R.string.profile_field_anonymous,
            getString(user.isAnonymous() ? R.string.profile_yes : R.string.profile_no));
        addRow(R.string.profile_field_photo_url, user.getPhotoUrl() == null ? null : user.getPhotoUrl().toString());
        addRow(R.string.profile_field_tenant, user.getTenantId());

        FirebaseUserMetadata meta = user.getMetadata();
        if (meta != null) {
            addRow(R.string.profile_field_created, formatTime(meta.getCreationTimestamp()));
            addRow(R.string.profile_field_last_signin, formatTime(meta.getLastSignInTimestamp()));
        }

        StringBuilder providers = new StringBuilder();
        for (UserInfo info : user.getProviderData()) {
            if (providers.length() > 0) providers.append(", ");
            providers.append(prettyProvider(info.getProviderId()));
        }
        addRow(R.string.profile_field_provider, providers.toString());

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail().build();
        com.google.android.gms.auth.api.signin.GoogleSignInAccount google = GoogleSignIn.getLastSignedInAccount(this);
        if (google != null) {
            addRow(R.string.profile_field_google_id, google.getId());
            addRow(R.string.profile_field_given_name, google.getGivenName());
            addRow(R.string.profile_field_family_name, google.getFamilyName());
        }
        // gso dipakai saat logout agar sesi Google ikut dibersihkan.
        mGso = gso;
    }

    private GoogleSignInOptions mGso;

    private String prettyProvider(String providerId) {
        if (providerId == null) return getString(R.string.profile_value_empty);
        switch (providerId) {
            case "google.com": return "Google";
            case "password": return "Email & Password";
            case "phone": return "Nomor Telepon";
            case "firebase": return "Firebase";
            default: return providerId;
        }
    }

    private void addRow(int labelRes, String value) {
        final String shown = isEmpty(value) ? getString(R.string.profile_value_empty) : value;
        View row = LayoutInflater.from(this).inflate(R.layout.item_profile_field, mInfoContainer, false);
        ((TextView) row.findViewById(R.id.field_label)).setText(labelRes);
        ((TextView) row.findViewById(R.id.field_value)).setText(shown);
        row.setOnLongClickListener(v -> {
            copyToClipboard(getString(labelRes), shown);
            return true;
        });
        mInfoContainer.addView(row);
    }

    private void copyToClipboard(String label, String value) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm == null) return;
        cm.setPrimaryClip(ClipData.newPlainText(label, value));
        Toast.makeText(this, getString(R.string.profile_copied, label), Toast.LENGTH_SHORT).show();
    }

    private String formatTime(long millis) {
        if (millis <= 0) return null;
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, HH:mm", new Locale("id", "ID"));
        return sdf.format(new Date(millis));
    }

    private void loadPhoto(@Nullable Uri url, ImageView target) {
        if (url == null) return;
        final String src = url.toString();
        new Thread(() -> {
            Bitmap bitmap = null;
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) new URL(src).openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                InputStream in = conn.getInputStream();
                bitmap = BitmapFactory.decodeStream(in);
                in.close();
            } catch (Exception ignored) {
            } finally {
                if (conn != null) conn.disconnect();
            }
            final Bitmap result = bitmap;
            if (result != null) {
                new Handler(Looper.getMainLooper()).post(() -> target.setImageBitmap(result));
            }
        }).start();
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.profile_logout_title)
            .setMessage(R.string.profile_logout_message)
            .setNegativeButton(R.string.profile_cancel, null)
            .setPositiveButton(R.string.profile_logout, (d, w) -> doLogout())
            .show();
    }

    private void doLogout() {
        mAuth.signOut();
        GoogleSignInOptions gso = mGso != null ? mGso
            : new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        GoogleSignInClient client = GoogleSignIn.getClient(this, gso);
        client.signOut().addOnCompleteListener(task -> {
            Toast.makeText(this, R.string.profile_logout_done, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private boolean isEmpty(@Nullable String s) {
        return s == null || s.trim().isEmpty();
    }

    @SuppressWarnings("unused")
    private ViewGroup unusedGuard() {
        return mInfoContainer;
    }
}
