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

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private EditText emailInput, passwordInput;
    private Button loginBtn, registerBtn;
    private LinearLayout splashLayout, loginLayout;
    private ProgressBar splashProgress;
    private TextView splashText;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_login);

            emailInput = findViewById(R.id.emailInput);
            passwordInput = findViewById(R.id.passwordInput);
            loginBtn = findViewById(R.id.loginBtn);
            registerBtn = findViewById(R.id.registerBtn);
            splashLayout = findViewById(R.id.splashLayout);
            loginLayout = findViewById(R.id.loginLayout);
            splashProgress = findViewById(R.id.splashProgress);
            splashText = findViewById(R.id.splashText);
            mAuth = FirebaseAuth.getInstance();

            if (emailInput == null || passwordInput == null || loginBtn == null ||
                    registerBtn == null || splashLayout == null || loginLayout == null ||
                    splashProgress == null || splashText == null) {
                Log.e(TAG, "One or more views are null");
                Toast.makeText(this, "Error initializing UI", Toast.LENGTH_LONG).show();
                return;
            }

            loginBtn.setOnClickListener(v -> loginUser());
            registerBtn.setOnClickListener(v -> {
                try {
                    Log.d(TAG, "Navigating to RegisterActivity");
                    Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error starting RegisterActivity: " + e.getMessage());
                    Toast.makeText(this, "Error opening registration: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

            splashProgress.setMax(100);
            new CountDownTimer(3000, 30) {
                public void onTick(long millisUntilFinished) {
                    int progress = (int) ((3000 - millisUntilFinished) / 30);
                    splashProgress.setProgress(progress);
                }

                public void onFinish() {
                    splashLayout.setVisibility(View.GONE);
                    loginLayout.setVisibility(View.VISIBLE);
                }
            }.start();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage());
            Toast.makeText(this, "Initialization error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loginUser() {
        try {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isNetworkAvailable()) {
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Login successful, navigating to MainActivity");
                            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            if (errorMsg.contains("no user record")) {
                                Toast.makeText(this, "No account found with this email", Toast.LENGTH_LONG).show();
                            } else if (errorMsg.contains("password is invalid")) {
                                Toast.makeText(this, "Incorrect password", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this, "Login failed: " + errorMsg, Toast.LENGTH_LONG).show();
                            }
                            Log.e(TAG, "Login failed: " + errorMsg);
                        }
                    });
        } catch (Exception e) {
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