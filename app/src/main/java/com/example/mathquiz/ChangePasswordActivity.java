package com.example.mathquiz;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {
    private static final String TAG = "ChangePasswordActivity";
    private FirebaseAuth mAuth;
    private EditText currentPasswordEditText, newPasswordEditText, confirmPasswordEditText;
    private Button submitButton, cancelButton;
    private ProgressBar progressBar;

    @SuppressLint({"StringFormatInvalid", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_change_password);
            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setTitle("");
                }
            }
            mAuth = FirebaseAuth.getInstance();
            currentPasswordEditText = findViewById(R.id.currentPasswordEditText);
            newPasswordEditText = findViewById(R.id.newPasswordEditText);
            confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
            submitButton = findViewById(R.id.submitButton);
            cancelButton = findViewById(R.id.cancelButton);
            progressBar = findViewById(R.id.progressBar);

            if (currentPasswordEditText == null || newPasswordEditText == null ||
                    confirmPasswordEditText == null || submitButton == null ||
                    cancelButton == null || progressBar == null) {
                Log.e(TAG, "UI elements missing: currentPasswordEditText=" + currentPasswordEditText +
                        ", newPasswordEditText=" + newPasswordEditText +
                        ", confirmPasswordEditText=" + confirmPasswordEditText +
                        ", submitButton=" + submitButton + ", cancelButton=" + cancelButton +
                        ", progressBar=" + progressBar);
                Toast.makeText(this, R.string.ui_init_failed, Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            submitButton.setOnClickListener(v -> changePassword());
            cancelButton.setOnClickListener(v -> finish());
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, getString(R.string.app_init_failed, e.getMessage()), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @SuppressLint("StringFormatInvalid")
    private void changePassword() {
        try {
            progressBar.setVisibility(View.VISIBLE);
            submitButton.setEnabled(false);
            cancelButton.setEnabled(false);

            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null || user.getEmail() == null) {
                progressBar.setVisibility(View.GONE);
                submitButton.setEnabled(true);
                cancelButton.setEnabled(true);
                Toast.makeText(this, R.string.no_user_signed_in, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!user.isEmailVerified()) {
                if (!isNetworkAvailable()) {
                    progressBar.setVisibility(View.GONE);
                    submitButton.setEnabled(true);
                    cancelButton.setEnabled(true);
                    Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                    return;
                }
                user.sendEmailVerification()
                        .addOnCompleteListener(task -> {
                            progressBar.setVisibility(View.GONE);
                            submitButton.setEnabled(true);
                            cancelButton.setEnabled(true);
                            if (task.isSuccessful()) {
                                Toast.makeText(this, R.string.verification_email_sent, Toast.LENGTH_LONG).show();
                            } else {
                                String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                                Toast.makeText(this, getString(R.string.verification_email_failed, errorMsg), Toast.LENGTH_LONG).show();
                            }
                        });
                return;
            }
            String currentPassword = currentPasswordEditText.getText().toString().trim();
            String newPassword = newPasswordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();
            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                progressBar.setVisibility(View.GONE);
                submitButton.setEnabled(true);
                cancelButton.setEnabled(true);
                Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                progressBar.setVisibility(View.GONE);
                submitButton.setEnabled(true);
                cancelButton.setEnabled(true);
                Toast.makeText(this, R.string.passwords_do_not_match, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPassword.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$")) {
                progressBar.setVisibility(View.GONE);
                submitButton.setEnabled(true);
                cancelButton.setEnabled(true);
                Toast.makeText(this, R.string.invalid_password_format, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isNetworkAvailable()) {
                progressBar.setVisibility(View.GONE);
                submitButton.setEnabled(true);
                cancelButton.setEnabled(true);
                Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                return;
            }
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
            user.reauthenticate(credential)
                    .addOnSuccessListener(aVoid -> {
                        user.updatePassword(newPassword)
                                .addOnSuccessListener(aVoid1 -> {
                                    progressBar.setVisibility(View.GONE);
                                    submitButton.setEnabled(true);
                                    cancelButton.setEnabled(true);
                                    Toast.makeText(this, R.string.password_updated, Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    submitButton.setEnabled(true);
                                    cancelButton.setEnabled(true);
                                    Toast.makeText(this, getString(R.string.password_update_failed, e.getMessage()), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        submitButton.setEnabled(true);
                        cancelButton.setEnabled(true);
                        Toast.makeText(this, R.string.incorrect_current_password, Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            submitButton.setEnabled(true);
            cancelButton.setEnabled(true);
            Log.e(TAG, "Error in changePassword: " + e.getMessage(), e);
            Toast.makeText(this, getString(R.string.password_change_error, e.getMessage()), Toast.LENGTH_LONG).show();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}