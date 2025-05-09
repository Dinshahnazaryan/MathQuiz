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

            if (emailInput == null || passwordInput == null || loginBtn == null ||
                    forgotPasswordText == null || progressBar == null) {
                Log.e(TAG, "UI elements missing");
                Toast.makeText(this, "UI initialization failed", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            loginBtn.setOnClickListener(v -> loginUser());
            forgotPasswordText.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error starting ForgottenPasswordActivity: " + e.getMessage());
                    Toast.makeText(this, "Navigation error", Toast.LENGTH_SHORT).show();
                }
            });

            if (registerBtn != null) {
                registerBtn.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "Error starting RegisterActivity: " + e.getMessage());
                        Toast.makeText(this, "Navigation error", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            new CountDownTimer(3000, 100) {
                @Override
                public void onTick(long millisUntilFinished) {
                    if (splashProgress != null) {
                        splashProgress.setProgress((int) (100 - (millisUntilFinished / 30)));
                    }
                }

                @Override
                public void onFinish() {
                    if (splashLayout != null) {
                        splashLayout.setVisibility(View.GONE);
                    }
                    if (loginLayout != null) {
                        loginLayout.setVisibility(View.VISIBLE);
                    }
                }
            }.start();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage());
            Toast.makeText(this, "Initialization failed", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void loginUser() {
        try {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Invalid email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isNetworkAvailable()) {
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            loginBtn.setEnabled(false);

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        progressBar.setVisibility(View.GONE);
                        loginBtn.setEnabled(true);

                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                try {
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.putExtra("email", user.getEmail());
                                    startActivity(intent);
                                    finish();
                                } catch (Exception e) {
                                    Log.e(TAG, "Error starting MainActivity: " + e.getMessage());
                                    Toast.makeText(this, "Navigation error", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e(TAG, "User is null after successful login");
                                Toast.makeText(this, "User not found", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            Log.e(TAG, "Login failed: " + errorMsg);
                            Toast.makeText(this, "Login failed: " + errorMsg, Toast.LENGTH_LONG).show();
                        }
                    });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            loginBtn.setEnabled(true);
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