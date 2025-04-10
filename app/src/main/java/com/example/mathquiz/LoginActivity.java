package com.example.mathquiz;

import android.content.Intent;
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

            // Initialize views
            emailInput = findViewById(R.id.emailInput);
            passwordInput = findViewById(R.id.passwordInput);
            loginBtn = findViewById(R.id.loginBtn);
            registerBtn = findViewById(R.id.registerBtn);
            splashLayout = findViewById(R.id.splashLayout);
            loginLayout = findViewById(R.id.loginLayout);
            splashProgress = findViewById(R.id.splashProgress);
            splashText = findViewById(R.id.splashText);
            mAuth = FirebaseAuth.getInstance();

            // Check if views are null
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
                    Log.d(TAG, "Register button clicked");
                    Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error starting RegisterActivity: " + e.getMessage());
                    Toast.makeText(this, "Error opening registration: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

            // Splash screen timer
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

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            Toast.makeText(this, "Invalid email or password: " + errorMsg, Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Login failed: " + errorMsg);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in loginUser: " + e.getMessage());
            Toast.makeText(this, "Login error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}