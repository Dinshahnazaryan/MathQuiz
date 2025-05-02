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
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Random;

public class ForgotPasswordActivity extends AppCompatActivity {
    private static final String TAG = "ForgotPasswordActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText emailInput;
    private Button sendCodeButton, cancelButton;
    private ProgressBar progressBar;

    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_forgot_password);
            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setTitle("");
                }
            }
            mAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();
            emailInput = findViewById(R.id.emailInput);
            sendCodeButton = findViewById(R.id.sendCodeButton);
            cancelButton = findViewById(R.id.cancelButton);
            progressBar = findViewById(R.id.progressBar);

            if (emailInput == null || sendCodeButton == null || cancelButton == null || progressBar == null) {
                Log.e(TAG, "UI elements missing: emailInput=" + emailInput +
                        ", sendCodeButton=" + sendCodeButton + ", cancelButton=" + cancelButton +
                        ", progressBar=" + progressBar);
                Toast.makeText(this, R.string.ui_init_failed, Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            sendCodeButton.setOnClickListener(v -> sendVerificationCode());
            cancelButton.setOnClickListener(v -> finish());
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, getString(R.string.app_init_failed, e.getMessage()), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @SuppressLint("StringFormatInvalid")
    private void sendVerificationCode() {
        try {
            progressBar.setVisibility(View.VISIBLE);
            sendCodeButton.setEnabled(false);
            cancelButton.setEnabled(false);

            String email = emailInput.getText().toString().trim();
            if (email.isEmpty()) {
                progressBar.setVisibility(View.GONE);
                sendCodeButton.setEnabled(true);
                cancelButton.setEnabled(true);
                Toast.makeText(this, R.string.enter_email, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                progressBar.setVisibility(View.GONE);
                sendCodeButton.setEnabled(true);
                cancelButton.setEnabled(true);
                Toast.makeText(this, R.string.invalid_email, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isNetworkAvailable()) {
                progressBar.setVisibility(View.GONE);
                sendCodeButton.setEnabled(true);
                cancelButton.setEnabled(true);
                Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                return;
            }

            String code = String.format("%06d", new Random().nextInt(999999));
            long expiryTime = System.currentTimeMillis() + 15 * 60 * 1000;

            db.collection("reset_codes").document(email)
                    .set(new ResetCode(code, expiryTime))
                    .addOnSuccessListener(aVoid -> {
                        mAuth.sendPasswordResetEmail(email)
                                .addOnCompleteListener(task -> {
                                    progressBar.setVisibility(View.GONE);
                                    sendCodeButton.setEnabled(true);
                                    cancelButton.setEnabled(true);
                                    if (task.isSuccessful()) {
                                        Toast.makeText(this, R.string.verification_code_sent, Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(ForgotPasswordActivity.this, VerifyCodeActivity.class);
                                        intent.putExtra("email", email);
                                        startActivity(intent);
                                    } else {
                                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                                        Toast.makeText(this, getString(R.string.code_send_failed, errorMsg), Toast.LENGTH_LONG).show();
                                    }
                                });
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        sendCodeButton.setEnabled(true);
                        cancelButton.setEnabled(true);
                        Toast.makeText(this, getString(R.string.code_store_failed, e.getMessage()), Toast.LENGTH_LONG).show();
                    });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            sendCodeButton.setEnabled(true);
            cancelButton.setEnabled(true);
            Log.e(TAG, "Error in sendVerificationCode: " + e.getMessage(), e);
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

    public static class ResetCode {
        public String code;
        public long expiryTime;

        public ResetCode() {}

        public ResetCode(String code, long expiryTime) {
            this.code = code;
            this.expiryTime = expiryTime;
        }
    }
}