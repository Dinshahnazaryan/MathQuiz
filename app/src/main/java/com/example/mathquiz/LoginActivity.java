package com.example.mathquiz;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final long MIN_CLICK_INTERVAL = 1000;
    private FirebaseAuth mAuth;
    private EditText emailInput, passwordInput;
    private Button loginBtn, testUserLoginBtn;
    private TextView registerText, forgotPasswordText;
    private ProgressBar progressBar;
    private long lastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_login);
            mAuth = FirebaseAuth.getInstance();
            emailInput = findViewById(R.id.emailInput);
            passwordInput = findViewById(R.id.passwordInput);
            loginBtn = findViewById(R.id.loginBtn);
            testUserLoginBtn = findViewById(R.id.testUserLoginBtn);
            registerText = findViewById(R.id.registerText);
            forgotPasswordText = findViewById(R.id.forgotPasswordText);
            progressBar = findViewById(R.id.progressBar);

            if (emailInput == null || passwordInput == null || loginBtn == null ||
                    testUserLoginBtn == null || registerText == null || forgotPasswordText == null || progressBar == null) {
                Log.e(TAG, "UI elements missing");
                showErrorDialog("UI initialization failed");
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
                    if (s != null && s.length() > 0 && s.toString().trim().isEmpty()) {
                        View focusedView = getCurrentFocus();
                        if (focusedView instanceof EditText) {
                            EditText editText = (EditText) focusedView;
                            editText.setText(s.toString().trim());
                            editText.setSelection(editText.getText().length());
                        }
                    }
                }
            };
            emailInput.addTextChangedListener(textWatcher);
            passwordInput.addTextChangedListener(textWatcher);

            loginBtn.setOnClickListener(v -> {
                if (isClickAllowed()) {
                    loginUser();
                }
            });
            testUserLoginBtn.setOnClickListener(v -> {
                if (isClickAllowed()) {
                    Log.d(TAG, "Test user login button clicked");
                    loginTestUser();
                }
            });
            registerText.setOnClickListener(v -> {
                if (isClickAllowed()) {
                    Log.d(TAG, "Navigating to RegisterActivity");
                    Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                    startActivity(intent);
                }
            });
            forgotPasswordText.setOnClickListener(v -> {
                if (isClickAllowed()) {
                    Log.d(TAG, "Navigating to PasswordOptionsActivity");
                    Intent intent = new Intent(LoginActivity.this, PasswordOptionsActivity.class);
                    startActivity(intent);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            showErrorDialog("Initialization failed");
            finish();
        }
    }

    private boolean isClickAllowed() {
        long currentTime = SystemClock.elapsedRealtime();
        if (currentTime - lastClickTime < MIN_CLICK_INTERVAL) {
            Log.w(TAG, "Click blocked due to rapid successive clicks");
            return false;
        }
        lastClickTime = currentTime;
        return true;
    }

    private void loginTestUser() {
        try {
            if (!isNetworkAvailable()) {
                showMessage("No internet connection", false);
                Log.w(TAG, "No internet connection for test user login");
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            loginBtn.setEnabled(false);
            testUserLoginBtn.setEnabled(false);
            registerText.setEnabled(false);
            forgotPasswordText.setEnabled(false);

            Log.d(TAG, "Attempting test user login with email: individualproject2025@gmail.com");
            mAuth.signInWithEmailAndPassword("individualproject2025@gmail.com", "Samsung2025")
                    .addOnCompleteListener(this, task -> {
                        progressBar.setVisibility(View.GONE);
                        loginBtn.setEnabled(true);
                        testUserLoginBtn.setEnabled(true);
                        registerText.setEnabled(true);
                        forgotPasswordText.setEnabled(true);

                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                Log.d(TAG, "Test user logged in successfully, email: " + user.getEmail() + ", uid: " + user.getUid());
                                SharedPreferences prefs = getSharedPreferences("quizPrefs", MODE_PRIVATE);
                                prefs.edit().putBoolean("isTestUser", true).apply();
                                showMessage("Test user login successful", false);
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.putExtra("email", "individualproject2025@gmail.com");
                                intent.putExtra("isTestUser", true);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                Log.e(TAG, "Test user login failed: user is null");
                                showMessage("Test user login failed: user is null", false);
                            }
                        } else {
                            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            Log.e(TAG, "Test user login failed: " + errorMsg, task.getException());
                            if (errorMsg.contains("password is invalid") || errorMsg.contains("no user record")) {
                                errorMsg = "Invalid test user credentials. Check Firebase setup.";
                            } else if (errorMsg.contains("network")) {
                                errorMsg = "Network error. Please check your connection.";
                            }
                            showMessage("Test user login failed: " + errorMsg, false);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in loginTestUser: " + e.getMessage(), e);
            progressBar.setVisibility(View.GONE);
            loginBtn.setEnabled(true);
            testUserLoginBtn.setEnabled(true);
            registerText.setEnabled(true);
            forgotPasswordText.setEnabled(true);
            showMessage("Unexpected error during test user login", false);
        }
    }

    private void loginUser() {
        try {
            if (!isNetworkAvailable()) {
                showMessage("No internet connection", false);
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            loginBtn.setEnabled(false);
            testUserLoginBtn.setEnabled(false);
            registerText.setEnabled(false);
            forgotPasswordText.setEnabled(false);

            String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
            String password = passwordInput.getText() != null ? passwordInput.getText().toString().trim() : "";

            if (email.isEmpty() || password.isEmpty()) {
                progressBar.setVisibility(View.GONE);
                loginBtn.setEnabled(true);
                testUserLoginBtn.setEnabled(true);
                registerText.setEnabled(true);
                forgotPasswordText.setEnabled(true);
                showMessage("Please fill all fields", false);
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                progressBar.setVisibility(View.GONE);
                loginBtn.setEnabled(true);
                testUserLoginBtn.setEnabled(true);
                registerText.setEnabled(true);
                forgotPasswordText.setEnabled(true);
                showMessage("Invalid email format", false);
                return;
            }

            Log.d(TAG, "Attempting regular user login with email: " + email);
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        progressBar.setVisibility(View.GONE);
                        loginBtn.setEnabled(true);
                        testUserLoginBtn.setEnabled(true);
                        registerText.setEnabled(true);
                        forgotPasswordText.setEnabled(true);

                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                if (user.isEmailVerified()) {
                                    Log.d(TAG, "Regular user logged in successfully, email: " + user.getEmail());
                                    SharedPreferences prefs = getSharedPreferences("quizPrefs", MODE_PRIVATE);
                                    prefs.edit().putBoolean("isTestUser", false).apply();
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.putExtra("email", email);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Log.d(TAG, "User email not verified, sending verification email for: " + user.getEmail());
                                    user.sendEmailVerification()
                                            .addOnCompleteListener(verifyTask -> {
                                                if (verifyTask.isSuccessful()) {
                                                    showMessage("Verification email sent to " + email, false);
                                                } else {
                                                    String errorMsg = verifyTask.getException() != null ? verifyTask.getException().getMessage() : "Unknown error";
                                                    showMessage("Failed to send verification email: " + errorMsg, false);
                                                    Log.e(TAG, "Failed to send verification email: " + errorMsg, verifyTask.getException());
                                                }
                                            });
                                    mAuth.signOut();
                                    showMessage("Please verify your email before logging in", false);
                                    Intent intent = new Intent(LoginActivity.this, VerifyCodeActivity.class);
                                    intent.putExtra("email", email);
                                    startActivity(intent);
                                }
                            } else {
                                Log.e(TAG, "Regular user login failed: user is null");
                                showMessage("Login failed: user is null", false);
                            }
                        } else {
                            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            Log.e(TAG, "Regular user login failed: " + errorMsg, task.getException());
                            if (errorMsg.contains("password is invalid") || errorMsg.contains("no user record")) {
                                errorMsg = "Invalid email or password";
                            } else if (errorMsg.contains("network")) {
                                errorMsg = "Network error, please check your connection";
                            }
                            showMessage("Login failed: " + errorMsg, false);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in loginUser: " + e.getMessage(), e);
            progressBar.setVisibility(View.GONE);
            loginBtn.setEnabled(true);
            testUserLoginBtn.setEnabled(true);
            registerText.setEnabled(true);
            forgotPasswordText.setEnabled(true);
            showMessage("Unexpected error during login", false);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isAvailable = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        Log.d(TAG, "Network available: " + isAvailable);
        return isAvailable;
    }

    private void showMessage(String message, boolean isError) {
        Log.d(TAG, "Showing message: " + message + ", isError=" + isError);
        try {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "Failed to show Toast: " + e.getMessage(), e);
            showErrorDialog(message);
        }
    }

    private void showErrorDialog(String message) {
        Log.d(TAG, "Showing error dialog: " + message);
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }
}