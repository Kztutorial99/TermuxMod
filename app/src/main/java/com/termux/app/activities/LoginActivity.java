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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.termux.R;
import com.termux.app.account.AccountSession;

/** Firebase Email/Password login and signup screen. */
public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private AccountSession mSession;
    private EditText mEmailInput;
    private EditText mPasswordInput;
    private TextView mModeToggle;
    private TextView mPrimaryButton;
    private TextView mStatusView;
    private boolean mSignupMode;

    public static void startLoginActivity(@NonNull Context context) {
        context.startActivity(new Intent(context, LoginActivity.class));
    }

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

        mAuth = FirebaseAuth.getInstance();
        mSession = new AccountSession(this);
        mEmailInput = findViewById(R.id.login_email);
        mPasswordInput = findViewById(R.id.login_password);
        mModeToggle = findViewById(R.id.login_mode_toggle);
        mPrimaryButton = findViewById(R.id.login_primary_button);
        mStatusView = findViewById(R.id.login_status);

        View back = findViewById(R.id.login_back);
        if (back != null) back.setOnClickListener(v -> finish());

        findViewById(R.id.login_primary_button).setOnClickListener(v -> submit());
        findViewById(R.id.login_mode_toggle).setOnClickListener(v -> setSignupMode(!mSignupMode));
        findViewById(R.id.login_forgot_password).setOnClickListener(v -> sendPasswordReset());
        setSignupMode(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            persistFirebaseUser(user);
            openProfile();
        }
    }

    private void setSignupMode(boolean signup) {
        mSignupMode = signup;
        mPrimaryButton.setText(signup ? R.string.account_signup_button : R.string.account_login_button);
        mModeToggle.setText(signup ? R.string.account_switch_to_login : R.string.account_switch_to_signup);
        findViewById(R.id.login_forgot_password).setVisibility(signup ? View.GONE : View.VISIBLE);
        setStatus(signup ? getString(R.string.account_signup_hint) : getString(R.string.account_login_hint));
    }

    private void submit() {
        String email = mEmailInput.getText().toString().trim();
        String password = mPasswordInput.getText().toString();

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmailInput.setError(getString(R.string.account_error_email));
            return;
        }
        if (password.length() < 6) {
            mPasswordInput.setError(getString(R.string.account_error_password));
            return;
        }

        setBusy(true);
        if (mSignupMode) {
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    setBusy(false);
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                        FirebaseUser user = task.getResult().getUser();
                        persistFirebaseUser(user);
                        Toast.makeText(this, R.string.account_signup_success, Toast.LENGTH_SHORT).show();
                        openProfile();
                    } else {
                        showFirebaseError(task.getException());
                    }
                });
        } else {
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    setBusy(false);
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                        persistFirebaseUser(task.getResult().getUser());
                        Toast.makeText(this, R.string.account_status_signed_in, Toast.LENGTH_SHORT).show();
                        openProfile();
                    } else {
                        showFirebaseError(task.getException());
                    }
                });
        }
    }

    private void sendPasswordReset() {
        String email = mEmailInput.getText().toString().trim();
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmailInput.setError(getString(R.string.account_error_email));
            return;
        }
        setStatus(getString(R.string.account_status_sending_reset));
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(this, task -> {
            if (task.isSuccessful())
                setStatus(getString(R.string.account_password_reset_sent));
            else
                showFirebaseError(task.getException());
        });
    }

    private void persistFirebaseUser(@NonNull FirebaseUser user) {
        String email = user.getEmail();
        String name = email != null && email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        mSession.save(user.getUid(), name, email,
            user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null);
    }

    private void openProfile() {
        ProfileActivity.startProfileActivity(this);
        finish();
    }

    private void setBusy(boolean busy) {
        mPrimaryButton.setEnabled(!busy);
        mModeToggle.setEnabled(!busy);
        mEmailInput.setEnabled(!busy);
        mPasswordInput.setEnabled(!busy);
        setStatus(getString(busy ? R.string.account_status_signing_in : R.string.account_login_hint));
    }

    private void showFirebaseError(@Nullable Exception exception) {
        String message = getString(R.string.account_status_sign_in_failed);
        if (exception != null && exception.getMessage() != null)
            message = exception.getMessage();
        setStatus(message);
    }

    private void setStatus(@NonNull String status) {
        if (mStatusView == null) return;
        mStatusView.setText(status);
        mStatusView.setVisibility(View.VISIBLE);
    }
}
