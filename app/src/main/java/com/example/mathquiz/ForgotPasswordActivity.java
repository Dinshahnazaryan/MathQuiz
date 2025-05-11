package com.example.mathquiz;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    private static final String TAG = "ForgotPasswordActivity";
    private static final long MIN_CLICK_INTERVAL = 1000;
    private FirebaseAuth mAuth;
    private EditText emailInput;
    private Button sendCodeButton, cancelButton;
    private ProgressBar progressBar;
    private long lastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_forgot_password);
            mAuth = FirebaseAuth.getInstance();

            emailInput = findViewById(R.id.emailInput);
            sendCodeButton = findViewById(R.id.sendCodeButton);
            cancelButton = findViewById(R.id.cancelButton);
            progressBar = findViewById(R.id.progressBar);

            if (emailInput == null || sendCodeButton == null || cancelButton == null || progressBar == null) {
                Log.e(TAG, "UI elements missing");
                Toast.makeText(this, "UI initialization failed", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            sendCodeButton.setOnClickListener(v -> {
                if (isClickAllowed()) {
                    sendResetEmail();
                }
            });

            cancelButton.setOnClickListener(v -> {
                if (isClickAllowed()) {
                    finish();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Initialization failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private boolean isClickAllowed() {
        long currentTime = SystemClock.elapsedRealtime();
        if (currentTime - lastClickTime < MIN_CLICK_INTERVAL) {
            return false;
        }
        lastClickTime = currentTime;
        return true;
    }

    private void sendResetEmail() {
        try {
            progressBar.setVisibility(View.VISIBLE);
            sendCodeButton.setEnabled(false);
            cancelButton.setEnabled(false);

            String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
            Log.d(TAG, "Attempting to send reset email to: " + email);

            if (email.isEmpty()) {
                progressBar.setVisibility(View.GONE);
                sendCodeButton.setEnabled(true);
                cancelButton.setEnabled(true);
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Email field empty");
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                progressBar.setVisibility(View.GONE);
                sendCodeButton.setEnabled(true);
                cancelButton.setEnabled(true);
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Invalid email format: " + email);
                return;
            }

            if (!isNetworkAvailable()) {
                progressBar.setVisibility(View.GONE);
                sendCodeButton.setEnabled(true);
                cancelButton.setEnabled(true);
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "No network available");
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        sendCodeButton.setEnabled(true);
                        cancelButton.setEnabled(true);
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Password reset email sent successfully to: " + email);
                            Toast.makeText(this, "Password reset email sent to " + email, Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            Log.e(TAG, "Failed to send reset email: " + errorMsg, task.getException());
                            Toast.makeText(this, "Failed to send reset email: " + errorMsg, Toast.LENGTH_LONG).show();
                        }
                    });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            sendCodeButton.setEnabled(true);
            cancelButton.setEnabled(true);
            Log.e(TAG, "Error in sendResetEmail: " + e.getMessage(), e);
            Toast.makeText(this, "Error sending reset email: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isAvailable = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        Log.d(TAG, "Network available: " + isAvailable);
        return isAvailable;
    }
}
