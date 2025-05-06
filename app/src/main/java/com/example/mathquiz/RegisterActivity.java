package com.example.mathquiz;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    private static final long MIN_CLICK_INTERVAL = 1000;
    private FirebaseAuth mAuth;
    private EditText emailInput, passwordInput;
    private Button registerBtn;
    private TextView loginText;
    private ProgressBar progressBar;
    private long lastClickTime = 0;

    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        try {
            setContentView(R.layout.activity_register);
            mAuth = FirebaseAuth.getInstance();
            emailInput = findViewById(R.id.emailInput);
            passwordInput = findViewById(R.id.passwordInput);
            registerBtn = findViewById(R.id.registerBtn);
            loginText = findViewById(R.id.loginText);
            progressBar = findViewById(R.id.progressBar);

            if (emailInput == null || passwordInput == null || registerBtn == null || loginText == null || progressBar == null) {
                Log.e(TAG, "UI elements missing");
                showToast(R.string.ui_init_failed);
                finish();
                return;
            }

            TextWatcher textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() > 0 && s.toString().trim().isEmpty()) {
                        EditText editText = (EditText) findViewById(getCurrentFocus() != null ? getCurrentFocus().getId() : R.id.emailInput);
                        editText.setText(s.toString().trim());
                        editText.setSelection(editText.getText().length());
                    }
                }
            };
            emailInput.addTextChangedListener(textWatcher);
            passwordInput.addTextChangedListener(textWatcher);

            registerBtn.setOnClickListener(v -> {
                if (isClickAllowed()) {
                    registerUser();
                }
            });
            loginText.setOnClickListener(v -> {
                if (isClickAllowed()) {
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            showToast(getString(R.string.app_init_failed, e.getMessage()));
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
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

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showToast(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }

    private void registerUser() {
        try {
            progressBar.setVisibility(View.VISIBLE);
            registerBtn.setEnabled(false);
            loginText.setEnabled(false);

            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            StringBuilder errorMessage = new StringBuilder();
            if (email.isEmpty() || password.isEmpty()) {
                errorMessage.append(getString(R.string.fill_all_fields)).append("\n");
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                errorMessage.append(getString(R.string.invalid_email)).append("\n");
            }
            if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$")) {
                errorMessage.append(getString(R.string.invalid_password_format)).append("\n");
            }
            if (!isNetworkAvailable()) {
                errorMessage.append(getString(R.string.no_internet)).append("\n");
            }

            if (errorMessage.length() > 0) {
                progressBar.setVisibility(View.GONE);
                registerBtn.setEnabled(true);
                loginText.setEnabled(true);
                showToast(errorMessage.toString().trim());
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        progressBar.setVisibility(View.GONE);
                        registerBtn.setEnabled(true);
                        loginText.setEnabled(true);
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                user.sendEmailVerification()
                                        .addOnCompleteListener(verifyTask -> {
                                            if (verifyTask.isSuccessful()) {
                                                showToast("Registration successful. Verification email sent.");
                                            } else {
                                                Log.w(TAG, "Failed to send verification email: " + verifyTask.getException().getMessage());
                                                showToast("Registered, but failed to send verification email.");
                                            }
                                        });
                                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                finish();
                            } else {
                                showToast(R.string.registration_failed);
                            }
                        } else {
                            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            showToast("Registration failed: " + errorMsg);
                        }
                    });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            registerBtn.setEnabled(true);
            loginText.setEnabled(true);
            Log.e(TAG, "Error in registerUser: " + e.getMessage(), e);
            showToast("Registration error: " + e.getMessage());
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}