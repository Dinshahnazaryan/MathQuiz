package com.example.mathquiz;

import android.app.AlertDialog;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    private static final long MIN_CLICK_INTERVAL = 1000;
    private FirebaseAuth mAuth;
    private EditText emailInput, passwordInput;
    private Button registerBtn;
    private TextView loginText;
    private ProgressBar progressBar;
    private long lastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

            registerBtn.setOnClickListener(v -> {
                if (isClickAllowed()) {
                    checkEmailAndRegister();
                }
            });
            loginText.setOnClickListener(v -> {
                if (isClickAllowed()) {
                    Log.d(TAG, "Navigating to LoginActivity");
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
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

    private void checkEmailAndRegister() {
        try {
            progressBar.setVisibility(View.VISIBLE);
            registerBtn.setEnabled(false);
            loginText.setEnabled(false);

            String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
            String password = passwordInput.getText() != null ? passwordInput.getText().toString().trim() : "";

            if (email.isEmpty() || password.isEmpty()) {
                progressBar.setVisibility(View.GONE);
                registerBtn.setEnabled(true);
                loginText.setEnabled(true);
                showMessage("Please fill all fields", false);
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                progressBar.setVisibility(View.GONE);
                registerBtn.setEnabled(true);
                loginText.setEnabled(true);
                showMessage("Invalid email", false);
                return;
            }
            if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$")) {
                progressBar.setVisibility(View.GONE);
                registerBtn.setEnabled(true);
                loginText.setEnabled(true);
                showMessage("Password must be 6+ chars with letters and numbers", false);
                return;
            }
            if (!isNetworkAvailable()) {
                progressBar.setVisibility(View.GONE);
                registerBtn.setEnabled(true);
                loginText.setEnabled(true);
                showMessage("No internet connection", false);
                return;
            }

            Log.d(TAG, "Checking if email exists: " + email);
            mAuth.fetchSignInMethodsForEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            SignInMethodQueryResult result = task.getResult();
                            if (result != null && result.getSignInMethods() != null && !result.getSignInMethods().isEmpty()) {
                                Log.w(TAG, "Email already in use: " + email);
                                progressBar.setVisibility(View.GONE);
                                registerBtn.setEnabled(true);
                                loginText.setEnabled(true);
                                showMessage("It is account with this email. оно не входила", false);
                            } else {
                                registerUser(email, password);
                            }
                        } else {
                            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            Log.e(TAG, "Failed to check email: " + errorMsg, task.getException());
                            progressBar.setVisibility(View.GONE);
                            registerBtn.setEnabled(true);
                            loginText.setEnabled(true);
                            showMessage("Error checking email: " + errorMsg, false);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in checkEmailAndRegister: " + e.getMessage(), e);
            progressBar.setVisibility(View.GONE);
            registerBtn.setEnabled(true);
            loginText.setEnabled(true);
            showMessage("Registration error", false);
        }
    }

    private void registerUser(String email, String password) {
        try {
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
                                                Log.d(TAG, "Verification email sent to " + email);
                                                showMessage("Verification email sent to " + email, false);
                                            } else {
                                                String errorMsg = verifyTask.getException() != null ? verifyTask.getException().getMessage() : "Unknown error";
                                                Log.e(TAG, "Failed to send verification email: " + errorMsg, verifyTask.getException());
                                                showMessage("Failed to send verification email: " + errorMsg, false);
                                            }
                                        });
                                if (user.isEmailVerified()) {
                                    Log.d(TAG, "User email verified, navigating to MainActivity");
                                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                    intent.putExtra("email", email);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Log.d(TAG, "User email not verified, signing out and navigating to LoginActivity");
                                    mAuth.signOut();
                                    showMessage("Please verify your email before logging in", false);
                                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            } else {
                                Log.e(TAG, "Registration succeeded but user is null");
                                showMessage("Registration failed: User not found", false);
                            }
                        } else {
                            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            Log.e(TAG, "Registration failed: " + errorMsg, task.getException());
                            showMessage("Registration failed: " + errorMsg, false);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in registerUser: " + e.getMessage(), e);
            progressBar.setVisibility(View.GONE);
            registerBtn.setEnabled(true);
            loginText.setEnabled(true);
            showMessage("Registration error", false);
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
        Log.d(TAG, "Showing message: " + message);
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