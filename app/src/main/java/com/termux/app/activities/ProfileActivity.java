package com.termux.app.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.termux.R;
import com.termux.app.account.AccountSession;
import com.termux.app.account.AvatarLoader;

/** Account profile screen backed by Firebase Authentication. */
public class ProfileActivity extends AppCompatActivity {

    private AccountSession mSession;
    private FirebaseAuth mAuth;
    private ImageView mAvatarView;
    private TextView mNameView;
    private TextView mEmailView;
    private TextView mAccountIdView;
    private TextView mSignedInAtView;
    private EditText mUsernameInput;
    private EditText mBioInput;

    public static void startProfileActivity(@NonNull Context context) {
        context.startActivity(new Intent(context, ProfileActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mSession = new AccountSession(this);
        mAuth = FirebaseAuth.getInstance();

        if (!mSession.isSignedIn() || mAuth.getCurrentUser() == null) {
            mSession.clear();
            LoginActivity.startLoginActivity(this);
            finish();
            return;
        }

        mAvatarView = findViewById(R.id.profile_avatar);
        mNameView = findViewById(R.id.profile_name);
        mEmailView = findViewById(R.id.profile_email);
        mAccountIdView = findViewById(R.id.profile_account_id);
        mSignedInAtView = findViewById(R.id.profile_signed_in_at);
        mUsernameInput = findViewById(R.id.profile_username_input);
        mBioInput = findViewById(R.id.profile_bio_input);

        View back = findViewById(R.id.profile_back);
        if (back != null) back.setOnClickListener(v -> finish());
        findViewById(R.id.profile_save_button).setOnClickListener(v -> saveProfile());
        findViewById(R.id.profile_sign_out_button).setOnClickListener(v -> signOut());
        bindAccount();
    }

    private void bindAccount() {
        mNameView.setText(orPlaceholder(mSession.getName()));
        mEmailView.setText(orPlaceholder(mSession.getEmail()));
        mAccountIdView.setText(orPlaceholder(mSession.getId()));
        long signedInAt = mSession.getSignedInAt();
        mSignedInAtView.setText(signedInAt > 0
            ? DateFormat.getDateFormat(this).format(signedInAt) + " " + DateFormat.getTimeFormat(this).format(signedInAt)
            : getString(R.string.account_value_unknown));
        mUsernameInput.setText(mSession.getUsername() != null ? mSession.getUsername() : "");
        mBioInput.setText(mSession.getBio() != null ? mSession.getBio() : "");
        AvatarLoader.load(mSession.getPhotoUrl(), mAvatarView);
    }

    private void saveProfile() {
        String username = mUsernameInput.getText().toString().trim();
        String bio = mBioInput.getText().toString().trim();
        if (username.isEmpty()) {
            mUsernameInput.setError(getString(R.string.account_error_username_required));
            return;
        }
        if (username.length() > 32) {
            mUsernameInput.setError(getString(R.string.account_error_username_too_long));
            return;
        }
        if (bio.length() > 200) {
            mBioInput.setError(getString(R.string.account_error_bio_too_long));
            return;
        }
        mSession.saveProfile(username, bio);
        Toast.makeText(this, R.string.account_profile_saved, Toast.LENGTH_SHORT).show();
    }

    private void signOut() {
        mAuth.signOut();
        mSession.clear();
        Toast.makeText(this, R.string.account_signed_out, Toast.LENGTH_SHORT).show();
        LoginActivity.startLoginActivity(this);
        finish();
    }

    @NonNull
    private String orPlaceholder(@Nullable String value) {
        return value != null ? value : getString(R.string.account_value_unknown);
    }
}
