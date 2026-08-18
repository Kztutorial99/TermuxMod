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
import com.termux.app.extrakeys.ExtraKeysOrderActivity;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Menu Profile User TermuxMod — tampilan ringkas, hanya info & menu penting.
 */
public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInOptions mGso;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();

        View back = findViewById(R.id.btn_profile_back);
        if (back != null) back.setOnClickListener(v -> finish());

        View extraKeys = findViewById(R.id.btn_profile_extrakeys);
        if (extraKeys != null) extraKeys.setOnClickListener(v ->
            startActivity(new Intent(ProfileActivity.this, ExtraKeysOrderActivity.class)));

        View settings = findViewById(R.id.btn_profile_settings);
        if (settings != null) settings.setOnClickListener(v ->
            startActivity(new Intent(ProfileActivity.this, SettingsActivity.class)));

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

    private boolean isEmpty(@Nullable String s) {
        return s == null || s.trim().isEmpty();
    }
}
