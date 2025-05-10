package com.example.mathquiz;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private EditText emailInput, passwordInput;
    private Button loginBtn, registerBtn;
    private TextView forgotPasswordText;
    private ProgressBar progressBar, splashProgress;
    private LinearLayout splashLayout, loginLayout;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_login);
            mAuth = FirebaseAuth.getInstance();

            emailInput = findViewById(R.id.emailInput);
            passwordInput = findViewById(R.id.passwordInput);
            loginBtn = findViewById(R.id.loginBtn);
            registerBtn = findViewById(R.id.registerBtn);
            forgotPasswordText = findViewById(R.id.forgotPasswordText);
            progressBar = findViewById(R.id.progressBar);
            splashProgress = findViewById(R.id.splashProgress);
            splashLayout = findViewById(R.id.splashLayout);
            loginLayout = findViewById(R.id.loginLayout);

            StringBuilder nullViews = new StringBuilder();
            if (emailInput == null) nullViews.append("emailInput, ");
            if (passwordInput == null) nullViews.append("passwordInput, ");
            if (loginBtn == null) nullViews.append("loginBtn, ");
            if (registerBtn == null) nullViews.append("registerBtn, ");
            if (forgotPasswordText == null) nullViews.append("forgotPasswordText, ");
            if (progressBar == null) nullViews.append("progressBar, ");
            if (splashProgress == null) nullViews.append("splashProgress, ");
            if (splashLayout == null) nullViews.append("splashLayout, ");
            if (loginLayout == null) nullViews.append("loginLayout, ");

            if (nullViews.length() > 0) {
                Log.e(TAG, "Missing UI elements: " + nullViews.toString());
                Toast.makeText(this, "UI initialization failed: missing " + nullViews.toString(), Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            loginBtn.setOnClickListener(v -> loginUser());
            registerBtn.setOnClickListener(v -> {
                try {
                    startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                } catch (Exception e) {
                    Log.e(TAG, "Error starting RegisterActivity: " + e.getMessage());
                    Toast.makeText(this, "Navigation error", Toast.LENGTH_SHORT).show();
                }
            });
            forgotPasswordText.setOnClickListener(v -> {
                try {
                    startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
                } catch (Exception e) {
                    Log.e(TAG, "Error starting ForgotPasswordActivity: " + e.getMessage());
                    Toast.makeText(this, "Navigation error", Toast.LENGTH_SHORT).show();
                }
            });

            splashProgress.setMax(100);
            new CountDownTimer(3000, 30) {
                @Override
                public void onTick(long millisUntilFinished) {
                    splashProgress.setProgress((int) ((3000 - millisUntilFinished) / 30));
                }

                @Override
                public void onFinish() {
                    splashLayout.setVisibility(View.GONE);
                    loginLayout.setVisibility(View.VISIBLE);
                }
            }.start();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Initialization failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void loginUser() {
        try {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty()) {
                emailInput.setError("Email is required");
                emailInput.requestFocus();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.setError("Invalid email format");
                emailInput.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                passwordInput.setError("Password is required");
                passwordInput.requestFocus();
                return;
            }

            if (password.length() < 6) {
                passwordInput.setError("Password must be at least 6 characters");
                passwordInput.requestFocus();
                return;
            }

            if (!isNetworkAvailable()) {
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            loginBtn.setEnabled(false);

            Log.d(TAG, "Attempting login with email: " + email);
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        progressBar.setVisibility(View.GONE);
                        loginBtn.setEnabled(true);

                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                Log.d(TAG, "Login successful for user: " + user.getEmail());
                                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, MainActivity.class)
                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                        .putExtra("email", user.getEmail()));
                                finish();
                            } else {
                                Log.e(TAG, "User is null after successful login");
                                Toast.makeText(this, "User not found", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            Log.e(TAG, "Login failed: " + errorMsg);
                            if (errorMsg.contains("no user record")) {
                                Toast.makeText(this, "User not registered. Please register.", Toast.LENGTH_LONG).show();
                            } else if (errorMsg.contains("password is invalid")) {
                                Toast.makeText(this, "Incorrect password", Toast.LENGTH_LONG).show();
                            } else if (errorMsg.contains("network error")) {
                                Toast.makeText(this, "Network error. Check your connection.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this, "Login failed: " + errorMsg, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            loginBtn.setEnabled(true);
            Log.e(TAG, "Error in loginUser: " + e.getMessage());
            Toast.makeText(this, "Login error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}