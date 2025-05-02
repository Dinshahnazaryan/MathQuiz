package com.example.mathquiz;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ResetPasswordActivity extends AppCompatActivity {
    private static final String TAG = "ResetPasswordActivity";
    private FirebaseAuth mAuth;
    private EditText newPasswordEditText;
    private Button resetButton, cancelButton;
    private ProgressBar progressBar;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_reset_password);
            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setTitle("");
                }
            }
            mAuth = FirebaseAuth.getInstance();
            newPasswordEditText = findViewById(R.id.newPasswordEditText);
            resetButton = findViewById(R.id.resetButton);
            cancelButton = findViewById(R.id.cancelButton);
            progressBar = findViewById(R.id.progressBar);

            if (newPasswordEditText == null || resetButton == null || cancelButton == null || progressBar == null) {
                Log.e(TAG, "UI elements missing: newPasswordEditText=" + newPasswordEditText +
                        ", resetButton=" + resetButton + ", cancelButton=" + cancelButton +
                        ", progressBar=" + progressBar);
                Toast.makeText(this, R.string.ui_init_failed, Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            email = getIntent().getStringExtra("email");
            if (email == null || email.isEmpty()) {
                Log.e(TAG, "Email not provided");
                Toast.makeText(this, R.string.invalid_email, Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            resetButton.setOnClickListener(v -> resetPassword());
            cancelButton.setOnClickListener(v -> finish());
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, getString(R.string.app_init_failed, e.getMessage()), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @SuppressLint("StringFormatInvalid")
    private void resetPassword() {
        try {
            progressBar.setVisibility(View.VISIBLE);
            resetButton.setEnabled(false);
            cancelButton.setEnabled(false);

            String newPassword = newPasswordEditText.getText().toString().trim();
            if (newPassword.isEmpty()) {
                progressBar.setVisibility(View.GONE);
                resetButton.setEnabled(true);
                cancelButton.setEnabled(true);
                Toast.makeText(this, R.string.enter_password, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPassword.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$")) {
                progressBar.setVisibility(View.GONE);
                resetButton.setEnabled(true);
                cancelButton.setEnabled(true);
                Toast.makeText(this, R.string.invalid_password_format, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isNetworkAvailable()) {
                progressBar.setVisibility(View.GONE);
                resetButton.setEnabled(true);
                cancelButton.setEnabled(true);
                Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null && user.getEmail() != null && user.getEmail().equals(email)) {
                user.updatePassword(newPassword)
                        .addOnSuccessListener(aVoid -> {
                            progressBar.setVisibility(View.GONE);
                            resetButton.setEnabled(true);
                            cancelButton.setEnabled(true);
                            Toast.makeText(this, R.string.password_reset_success, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            progressBar.setVisibility(View.GONE);
                            resetButton.setEnabled(true);
                            cancelButton.setEnabled(true);
                            Toast.makeText(this, getString(R.string.password_reset_failed, e.getMessage()), Toast.LENGTH_LONG).show();
                        });
            } else {
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            progressBar.setVisibility(View.GONE);
                            resetButton.setEnabled(true);
                            cancelButton.setEnabled(true);
                            if (task.isSuccessful()) {
                                Toast.makeText(this, R.string.password_reset_email_sent, Toast.LENGTH_LONG).show();
                                finish();
                            } else {
                                String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                                Toast.makeText(this, getString(R.string.reset_email_failed, errorMsg), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            resetButton.setEnabled(true);
            cancelButton.setEnabled(true);
            Log.e(TAG, "Error in resetPassword: " + e.getMessage(), e);
            Toast.makeText(this, getString(R.string.app_error, e.getMessage()), Toast.LENGTH_LONG).show();
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