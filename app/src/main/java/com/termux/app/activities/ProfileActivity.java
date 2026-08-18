package com.termux.app.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
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
import com.google.firebase.auth.UserInfo;
import com.termux.R;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Menu Profile User TermuxMod — tampilan ringkas, hanya info & menu penting.
 */
public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private final FirebaseAuth.AuthStateListener mAuthListener = auth -> {
        FirebaseUser u = auth.getCurrentUser();
        if (u == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else if (!isFinishing() && !isDestroyed()) {
            bindUser(u);
        }
    };
    private GoogleSignInOptions mGso;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();

        View back = findViewById(R.id.btn_profile_back);
        if (back != null) back.setOnClickListener(v -> finish());

        View logout = findViewById(R.id.btn_profile_logout);
        if (logout != null) logout.setOnClickListener(v -> confirmLogout());
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
        // Sinkronisasi otomatis: pantau perubahan akun & segarkan data tanpa tombol.
        mAuth.addAuthStateListener(mAuthListener);
        syncAccount();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);
    }

    /** Ambil data akun terbaru dari server lalu perbarui tampilan otomatis. */
    private void syncAccount() {
        FirebaseUser current = mAuth.getCurrentUser();
        if (current == null) return;
        current.reload().addOnCompleteListener(t -> {
            if (isFinishing() || isDestroyed()) return;
            FirebaseUser fresh = mAuth.getCurrentUser();
            if (fresh != null) bindUser(fresh);
        });
    }

    private void bindUser(FirebaseUser user) {
        TextView name = findViewById(R.id.profile_name);
        TextView email = findViewById(R.id.profile_email);
        TextView status = findViewById(R.id.profile_status);
        ImageView photo = findViewById(R.id.profile_photo);

        name.setText(isEmpty(user.getDisplayName())
            ? getString(R.string.profile_value_empty) : user.getDisplayName());
        email.setText(isEmpty(user.getEmail())
            ? getString(R.string.profile_value_empty) : user.getEmail());

        StringBuilder providers = new StringBuilder();
        for (UserInfo info : user.getProviderData()) {
            String pretty = prettyProvider(info.getProviderId());
            if (pretty == null || "Firebase".equals(pretty)) continue;
            if (providers.length() > 0) providers.append(", ");
            providers.append(pretty);
        }
        String verified = getString(user.isEmailVerified()
            ? R.string.profile_verified : R.string.profile_not_verified);
        status.setText(providers.length() > 0 ? providers + " • " + verified : verified);

        loadPhoto(user.getPhotoUrl(), photo);
        buildInfoRows(user);

        mGso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail().build();
    }

    private String prettyProvider(String providerId) {
        if (providerId == null) return null;
        switch (providerId) {
            case "google.com": return "Google";
            case "password": return "Email & Password";
            case "phone": return "Nomor Telepon";
            case "firebase": return "Firebase";
            default: return providerId;
        }
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

    /** Bangun daftar info akun lengkap (read-only, long-press utk copy). */
    private void buildInfoRows(FirebaseUser user) {
        android.widget.LinearLayout container = findViewById(R.id.profile_info_container);
        if (container == null) return;
        container.removeAllViews();

        String googleId = null, givenName = null, familyName = null, providerNames = "";
        for (UserInfo info : user.getProviderData()) {
            String pretty = prettyProvider(info.getProviderId());
            if (pretty == null || "Firebase".equals(pretty)) continue;
            providerNames = providerNames.isEmpty() ? pretty : providerNames + ", " + pretty;
            if ("google.com".equals(info.getProviderId())) {
                googleId = info.getUid();
                String display = info.getDisplayName();
                if (display != null && display.contains(" ")) {
                    int i = display.indexOf(' ');
                    givenName = display.substring(0, i);
                    familyName = display.substring(i + 1);
                } else {
                    givenName = display;
                }
            }
        }

        addRow(container, getString(R.string.profile_field_name), user.getDisplayName());
        addRow(container, getString(R.string.profile_field_email), user.getEmail());
        addRow(container, getString(R.string.profile_field_email_verified),
            getString(user.isEmailVerified() ? R.string.profile_verified : R.string.profile_not_verified));
        addRow(container, getString(R.string.profile_field_phone), user.getPhoneNumber());
        addRow(container, getString(R.string.profile_field_uid), user.getUid());
        addRow(container, getString(R.string.profile_field_provider), providerNames);
        addRow(container, getString(R.string.profile_field_anonymous),
            getString(user.isAnonymous() ? R.string.profile_yes : R.string.profile_no));
        if (user.getMetadata() != null) {
            addRow(container, getString(R.string.profile_field_created),
                formatTime(user.getMetadata().getCreationTimestamp()));
            addRow(container, getString(R.string.profile_field_last_signin),
                formatTime(user.getMetadata().getLastSignInTimestamp()));
        }
    }

    private String formatTime(long millis) {
        if (millis <= 0) return null;
        return new java.text.SimpleDateFormat("dd MMM yyyy • HH:mm", java.util.Locale.getDefault())
            .format(new java.util.Date(millis));
    }

    private void addRow(android.widget.LinearLayout parent, final String label, @Nullable String value) {
        final String shown = isEmpty(value) ? getString(R.string.profile_value_empty) : value;
        float d = getResources().getDisplayMetrics().density;

        android.widget.LinearLayout row = new android.widget.LinearLayout(this);
        row.setOrientation(android.widget.LinearLayout.VERTICAL);
        row.setPadding((int) (12 * d), (int) (11 * d), (int) (12 * d), (int) (11 * d));

        TextView lbl = new TextView(this);
        lbl.setText(label);
        lbl.setTextSize(11f);
        lbl.setTextColor(getResources().getColor(R.color.color_text_secondary));

        TextView val = new TextView(this);
        val.setText(shown);
        val.setTextSize(14f);
        val.setTextColor(getResources().getColor(R.color.color_text_primary));

        row.addView(lbl);
        row.addView(val);
        row.setOnLongClickListener(v -> {
            android.content.ClipboardManager cm =
                (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (cm != null) cm.setPrimaryClip(android.content.ClipData.newPlainText(label, shown));
            Toast.makeText(this, getString(R.string.profile_copied, label), Toast.LENGTH_SHORT).show();
            return true;
        });
        parent.addView(row);

        View divider = new View(this);
        android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT, (int) Math.max(1, d));
        lp.leftMargin = (int) (12 * d);
        lp.rightMargin = (int) (12 * d);
        divider.setLayoutParams(lp);
        divider.setBackgroundColor(getResources().getColor(R.color.color_border));
        parent.addView(divider);
    }

    private boolean isEmpty(@Nullable String s) {
        return s == null || s.trim().isEmpty();
    }
}
