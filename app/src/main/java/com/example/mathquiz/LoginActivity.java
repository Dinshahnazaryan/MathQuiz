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
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final long MIN_CLICK_INTERVAL = 1000;
    private FirebaseAuth mAuth;
    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView registerText, forgotPasswordText;
    private ProgressBar progressBar;
    private long lastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_login);
            mAuth = FirebaseAuth.getInstance();

            emailEditText = findViewById(R.id.emailInput);
            passwordEditText = findViewById(R.id.passwordInput);
            loginButton = findViewById(R.id.loginButton);
            registerText = findViewById(R.id.registerText);
            forgotPasswordText = findViewById(R.id.forgotPasswordText);
            progressBar = findViewById(R.id.progressBar);

            if (emailEditText == null || passwordEditText == null || loginButton == null ||
                    registerText == null || forgotPasswordText == null || progressBar == null) {
                Log.e(TAG, "UI elements missing");
                Toast.makeText(this, "UI initialization failed", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            loginButton.setOnClickListener(v -> {
                if (isClickAllowed()) {
                    loginUser();
                }
            });

            registerText.setOnClickListener(v -> {
                if (isClickAllowed()) {
                    Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                    startActivity(intent);
                }
            });

            forgotPasswordText.setOnClickListener(v -> {
                if (isClickAllowed()) {
                    Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                    startActivity(intent);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage());
            Toast.makeText(this, "Initialization failed", Toast.LENGTH_LONG).show();
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

    private void loginUser() {
        try {
            progressBar.setVisibility(View.VISIBLE);
            loginButton.setEnabled(false);
            registerText.setEnabled(false);
            forgotPasswordText.setEnabled(false);

            String email = emailEditText.getText() != null ? emailEditText.getText().toString().trim() : "";
            String password = passwordEditText.getText() != null ? passwordEditText.getText().toString().trim() : "";

            if (email.isEmpty() || password.isEmpty()) {
                progressBar.setVisibility(View.GONE);
                loginButton.setEnabled(true);
                registerText.setEnabled(true);
                forgotPasswordText.setEnabled(true);
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                progressBar.setVisibility(View.GONE);
                loginButton.setEnabled(true);
                registerText.setEnabled(true);
                forgotPasswordText.setEnabled(true);
                Toast.makeText(this, "Invalid email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isNetworkAvailable()) {
                progressBar.setVisibility(View.GONE);
                loginButton.setEnabled(true);
                registerText.setEnabled(true);
                forgotPasswordText.setEnabled(true);
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        progressBar.setVisibility(View.GONE);
                        loginButton.setEnabled(true);
                        registerText.setEnabled(true);
                        forgotPasswordText.setEnabled(true);
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                if (user.isEmailVerified()) {
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // Resend verification email
                                    user.sendEmailVerification()
                                            .addOnCompleteListener(verifyTask -> {
                                                if (verifyTask.isSuccessful()) {
                                                    Toast.makeText(this, "Verification email sent to " + email, Toast.LENGTH_LONG).show();
                                                } else {
                                                    String errorMsg = verifyTask.getException() != null ? verifyTask.getException().getMessage() : "Unknown error";
                                                    Toast.makeText(this, "Failed to send verification email: " + errorMsg, Toast.LENGTH_LONG).show();
                                                }
                                            });
                                    mAuth.signOut();
                                    Toast.makeText(this, "Please verify your email", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(this, "Login failed: User not found", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            Toast.makeText(this, "Login failed: " + errorMsg, Toast.LENGTH_LONG).show();
                        }
                    });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            loginButton.setEnabled(true);
            registerText.setEnabled(true);
            forgotPasswordText.setEnabled(true);
            Log.e(TAG, "Error in loginUser: " + e.getMessage());
            Toast.makeText(this, "Login error", Toast.LENGTH_LONG).show();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
