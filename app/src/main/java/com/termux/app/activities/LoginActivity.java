package com.termux.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.termux.R;

/**
 * Halaman Login Google untuk TermuxMod.
 * Memakai Google Sign-In + Firebase Authentication.
 */
public class LoginActivity extends AppCompatActivity {

    private static final int RC_GOOGLE_SIGN_IN = 9001;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleClient;

    private ProgressBar mProgress;
    private View mLoginButton;
    private TextView mStatus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mProgress = findViewById(R.id.login_progress);
        mStatus = findViewById(R.id.login_status);
        mLoginButton = findViewById(R.id.btn_google_login);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build();
        mGoogleClient = GoogleSignIn.getClient(this, gso);

        mLoginButton.setOnClickListener(v -> startGoogleSignIn());

        View back = findViewById(R.id.btn_login_back);
        if (back != null) back.setOnClickListener(v -> finish());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            openProfile();
        }
    }

    private void startGoogleSignIn() {
        setLoading(true, getString(R.string.login_status_connecting));
        mGoogleClient.signOut().addOnCompleteListener(task ->
            startActivityForResult(mGoogleClient.getSignInIntent(), RC_GOOGLE_SIGN_IN));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != RC_GOOGLE_SIGN_IN) return;

        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account == null || account.getIdToken() == null) {
                fail(getString(R.string.login_error_no_token));
                return;
            }
            firebaseAuthWithGoogle(account.getIdToken());
        } catch (ApiException e) {
            fail(getString(R.string.login_error_failed) + " (code " + e.getStatusCode() + ")");
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        setLoading(true, getString(R.string.login_status_verifying));
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show();
                openProfile();
            } else {
                String msg = task.getException() != null ? task.getException().getLocalizedMessage() : "";
                fail(getString(R.string.login_error_failed) + "\n" + msg);
            }
        });
    }

    private void openProfile() {
        startActivity(new Intent(this, ProfileActivity.class));
        finish();
    }

    private void fail(String message) {
        setLoading(false, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void setLoading(boolean loading, String status) {
        if (mProgress != null) mProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (mLoginButton != null) mLoginButton.setEnabled(!loading);
        if (mStatus != null) {
            mStatus.setText(status);
            mStatus.setVisibility(status == null || status.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }
}
