package com.termux.app.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.termux.R;
import com.termux.app.account.AccountSession;

/**
 * Google account login / signup screen.
 *
 * Signing in with Google both creates and restores the local profile, so the
 * same screen serves login and signup. Requires the OAuth web client id to be
 * set in {@code R.string.google_oauth_web_client_id}, see
 * docs/GOOGLE_SIGN_IN_SETUP.md.
 */
public class LoginActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_GOOGLE_SIGN_IN = 9001;

    private GoogleSignInClient mGoogleSignInClient;
    private AccountSession mSession;
    private TextView mStatusView;

    public static void startLoginActivity(@NonNull Context context) {
        context.startActivity(new Intent(context, LoginActivity.class));
    }

    /** Opens the profile when already signed in, the login screen otherwise. */
    public static void startAccountActivity(@NonNull Context context) {
        if (new AccountSession(context).isSignedIn())
            ProfileActivity.startProfileActivity(context);
        else
            startLoginActivity(context);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mSession = new AccountSession(this);
        mStatusView = findViewById(R.id.login_status);

        View back = findViewById(R.id.login_back);
        if (back != null) back.setOnClickListener(v -> finish());

        GoogleSignInOptions.Builder optionsBuilder =
            new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile();

        String webClientId = getString(R.string.google_oauth_web_client_id);
        if (!TextUtils.isEmpty(webClientId))
            optionsBuilder.requestIdToken(webClientId);

        mGoogleSignInClient = GoogleSignIn.getClient(this, optionsBuilder.build());

        findViewById(R.id.login_google_button).setOnClickListener(v -> signIn());
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Already signed in on this device, go straight to the profile.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            persistAccount(account);
            openProfile();
        }
    }

    private void signIn() {
        setStatus(getString(R.string.account_status_signing_in));
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), REQUEST_CODE_GOOGLE_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_CODE_GOOGLE_SIGN_IN) return;

        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account == null) {
                setStatus(getString(R.string.account_status_sign_in_failed));
                return;
            }
            persistAccount(account);
            Toast.makeText(this, R.string.account_status_signed_in, Toast.LENGTH_SHORT).show();
            openProfile();
        } catch (ApiException e) {
            setStatus(getString(R.string.account_status_sign_in_failed) + " (" + e.getStatusCode() + ")");
        }
    }

    private void persistAccount(@NonNull GoogleSignInAccount account) {
        mSession.save(
            account.getId(),
            account.getDisplayName(),
            account.getEmail(),
            account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : null);
    }

    private void openProfile() {
        ProfileActivity.startProfileActivity(this);
        finish();
    }

    private void setStatus(@NonNull String status) {
        if (mStatusView == null) return;
        mStatusView.setText(status);
        mStatusView.setVisibility(View.VISIBLE);
    }

}
