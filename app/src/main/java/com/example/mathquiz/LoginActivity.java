package com.example.mathquiz;

import android.content.Intent;
import android.os.Build;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private EditText emailInput, passwordInput;
    private Button loginBtn, registerBtn;
    private TextView forgotPasswordText;
    private ProgressBar progressBar, splashProgress;
    private LinearLayout splashLayout, loginLayout;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        try {
            mAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();
            initializeUI();
            startSplashScreen();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, R.string.app_init_failed, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initializeUI() {
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginBtn = findViewById(R.id.loginBtn);
        registerBtn = findViewById(R.id.registerBtn);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
        progressBar = findViewById(R.id.progressBar);
        splashProgress = findViewById(R.id.splashProgress);
        splashLayout = findViewById(R.id.splashLayout);
        loginLayout = findViewById(R.id.loginLayout);

        if (emailInput == null || passwordInput == null || loginBtn == null ||
                forgotPasswordText == null || progressBar == null ||
                splashProgress == null || splashLayout == null || loginLayout == null) {
            Log.e(TAG, "UI elements missing");
            Toast.makeText(this, R.string.ui_init_failed, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        loginBtn.setOnClickListener(v -> loginUser());
        forgotPasswordText.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
                Log.d(TAG, "Navigating to ForgottenPasswordActivity");
            } catch (Exception e) {
                Log.e(TAG, "Error starting ForgottenPasswordActivity: " + e.getMessage(), e);
                Toast.makeText(this, R.string.nav_error, Toast.LENGTH_SHORT).show();
            }
        });

        if (registerBtn != null) {
            registerBtn.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                    startActivity(intent);
                    Log.d(TAG, "Navigating to RegisterActivity");
                } catch (Exception e) {
                    Log.e(TAG, "Error starting RegisterActivity: " + e.getMessage(), e);
                    Toast.makeText(this, R.string.nav_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void startSplashScreen() {
        Log.d(TAG, "Starting login splash screen");
        new CountDownTimer(3000, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                splashProgress.setProgress((int) (100 - (millisUntilFinished / 30)));
            }

            @Override
            public void onFinish() {
                splashLayout.setVisibility(View.GONE);
                loginLayout.setVisibility(View.VISIBLE);
                Log.d(TAG, "Login screen displayed");
            }
        }.start();
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        loginBtn.setEnabled(false);
        Log.d(TAG, "Attempting login for: " + email);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    loginBtn.setEnabled(true);

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Log.d(TAG, "Login successful for: " + user.getEmail());
                            // Log login to Firestore
                            Map<String, Object> loginData = new HashMap<>();
                            loginData.put("email", user.getEmail());
                            loginData.put("timestamp", System.currentTimeMillis());
                            loginData.put("device_model", Build.MODEL);
                            loginData.put("os_version", Build.VERSION.RELEASE);

                            db.collection("login_attempts")
                                    .add(loginData)
                                    .addOnSuccessListener(documentReference -> {
                                        Log.d(TAG, "Login recorded in Firestore for: " + user.getEmail());
                                        // Navigate to MainActivity
                                        try {
                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            intent.putExtra("email", user.getEmail());
                                            startActivity(intent);
                                            Log.d(TAG, "Navigating to MainActivity for: " + user.getEmail());
                                            finish();
                                        } catch (Exception e) {
                                            Log.e(TAG, "Error starting MainActivity: " + e.getMessage(), e);
                                            Toast.makeText(LoginActivity.this, R.string.nav_error, Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Failed to record login in Firestore: " + e.getMessage(), e);
                                        Toast.makeText(LoginActivity.this, "Error logging login", Toast.LENGTH_SHORT).show();
                                        // Still navigate to MainActivity
                                        try {
                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            intent.putExtra("email", user.getEmail());
                                            startActivity(intent);
                                            Log.d(TAG, "Navigating to MainActivity for: " + user.getEmail());
                                            finish();
                                        } catch (Exception ex) {
                                            Log.e(TAG, "Error starting MainActivity: " + ex.getMessage(), ex);
                                            Toast.makeText(LoginActivity.this, R.string.nav_error, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Log.e(TAG, "User is null after successful login");
                            Toast.makeText(LoginActivity.this, "User not found", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Log.e(TAG, "Login failed: " + errorMsg, task.getException());
                        Toast.makeText(LoginActivity.this, "Login failed: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }
}