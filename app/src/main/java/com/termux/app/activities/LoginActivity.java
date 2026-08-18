package com.termux.app.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.termux.R;
import com.termux.app.account.AccountSession;

/** Firebase Authentication login with both Google and Email/Password. */
public class LoginActivity extends AppCompatActivity {
    private static final int RC_GOOGLE_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleClient;
    private AccountSession mSession;
    private EditText mEmailInput;
    private EditText mPasswordInput;
    private TextView mModeToggle;
    private TextView mPrimaryButton;
    private TextView mGoogleButton;
    private TextView mStatusView;
    private boolean mSignupMode;

    public static void startLoginActivity(@NonNull Context context) { context.startActivity(new Intent(context, LoginActivity.class)); }
    public static void startAccountActivity(@NonNull Context context) {
        if (new AccountSession(context).isSignedIn()) ProfileActivity.startProfileActivity(context); else startLoginActivity(context);
    }

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        mSession = new AccountSession(this);
        mEmailInput = findViewById(R.id.login_email);
        mPasswordInput = findViewById(R.id.login_password);
        mModeToggle = findViewById(R.id.login_mode_toggle);
        mPrimaryButton = findViewById(R.id.login_primary_button);
        mGoogleButton = findViewById(R.id.login_google_button);
        mStatusView = findViewById(R.id.login_status);
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        mGoogleClient = GoogleSignIn.getClient(this, options);
        View back = findViewById(R.id.login_back);
        if (back != null) back.setOnClickListener(v -> finish());
        mPrimaryButton.setOnClickListener(v -> submitEmail());
        mModeToggle.setOnClickListener(v -> setSignupMode(!mSignupMode));
        findViewById(R.id.login_forgot_password).setOnClickListener(v -> sendPasswordReset());
        mGoogleButton.setOnClickListener(v -> signInWithGoogle());
        setSignupMode(false);
    }

    @Override protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) { persistFirebaseUser(user); openProfile(); }
    }

    private void setSignupMode(boolean signup) {
        mSignupMode = signup;
        mPrimaryButton.setText(signup ? R.string.account_signup_button : R.string.account_login_button);
        mModeToggle.setText(signup ? R.string.account_switch_to_login : R.string.account_switch_to_signup);
        findViewById(R.id.login_forgot_password).setVisibility(signup ? View.GONE : View.VISIBLE);
        setStatus(getString(signup ? R.string.account_signup_hint : R.string.account_login_hint));
    }

    private void submitEmail() {
        String email = mEmailInput.getText().toString().trim();
        String password = mPasswordInput.getText().toString();
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) { mEmailInput.setError(getString(R.string.account_error_email)); return; }
        if (password.length() < 6) { mPasswordInput.setError(getString(R.string.account_error_password)); return; }
        setBusy(true);
        Task<?> task = mSignupMode ? mAuth.createUserWithEmailAndPassword(email, password) : mAuth.signInWithEmailAndPassword(email, password);
        task.addOnCompleteListener(this, result -> {
            setBusy(false);
            if (result.isSuccessful() && mAuth.getCurrentUser() != null) {
                persistFirebaseUser(mAuth.getCurrentUser());
                Toast.makeText(this, mSignupMode ? R.string.account_signup_success : R.string.account_status_signed_in, Toast.LENGTH_SHORT).show();
                openProfile();
            } else showFirebaseError(result.getException());
        });
    }

    private void signInWithGoogle() { setBusy(true); startActivityForResult(mGoogleClient.getSignInIntent(), RC_GOOGLE_SIGN_IN); }

    @Override protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != RC_GOOGLE_SIGN_IN) return;
        if (data == null) { setBusy(false); setStatus(getString(R.string.account_google_sign_in_cancelled)); return; }
        try {
            GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
            if (account == null || account.getIdToken() == null) { setBusy(false); setStatus(getString(R.string.account_google_sign_in_failed)); return; }
            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
            mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
                setBusy(false);
                if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                    persistFirebaseUser(mAuth.getCurrentUser());
                    Toast.makeText(this, R.string.account_status_signed_in, Toast.LENGTH_SHORT).show();
                    openProfile();
                } else showFirebaseError(task.getException());
            });
        } catch (ApiException e) { setBusy(false); setStatus(getString(R.string.account_google_sign_in_failed)); }
    }

    private void sendPasswordReset() {
        String email = mEmailInput.getText().toString().trim();
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) { mEmailInput.setError(getString(R.string.account_error_email)); return; }
        setStatus(getString(R.string.account_status_sending_reset));
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(this, task -> { if (task.isSuccessful()) setStatus(getString(R.string.account_password_reset_sent)); else showFirebaseError(task.getException()); });
    }

    private void persistFirebaseUser(@NonNull FirebaseUser user) {
        String email = user.getEmail();
        String name = user.getDisplayName();
        if (name == null || name.trim().isEmpty()) name = email != null && email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        mSession.save(user.getUid(), name, email, user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null);
    }
    private void openProfile() { ProfileActivity.startProfileActivity(this); finish(); }
    private void setBusy(boolean busy) {
        mPrimaryButton.setEnabled(!busy); mModeToggle.setEnabled(!busy); mGoogleButton.setEnabled(!busy); mEmailInput.setEnabled(!busy); mPasswordInput.setEnabled(!busy);
        setStatus(getString(busy ? R.string.account_status_signing_in : R.string.account_login_hint));
    }
    private void showFirebaseError(@Nullable Exception exception) { setStatus(exception != null && exception.getMessage() != null ? exception.getMessage() : getString(R.string.account_status_sign_in_failed)); }
    private void setStatus(@NonNull String status) { if (mStatusView != null) { mStatusView.setText(status); mStatusView.setVisibility(View.VISIBLE); } }
}
