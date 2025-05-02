package com.example.mathquiz;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
    private Button loginBtn, resendVerificationBtn, registerBtn;
    private ImageButton backArrowBtn;
    private TextView forgotPasswordText;
    private ProgressBar progressBar, splashProgress;
    private LinearLayout splashLayout, loginLayout;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        try {
            mAuth = FirebaseAuth.getInstance();
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
        resendVerificationBtn = findViewById(R.id.resendVerificationBtn);
        registerBtn = findViewById(R.id.registerBtn);
        backArrowBtn = findViewById(R.id.backArrowBtn);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
        progressBar = findViewById(R.id.progressBar);
        splashProgress = findViewById(R.id.splashProgress);
        splashLayout = findViewById(R.id.splashLayout);
        loginLayout = findViewById(R.id.loginLayout);

        loginBtn.setOnClickListener(v -> loginUser());
        forgotPasswordText.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error starting ForgotPasswordActivity: " + e.getMessage(), e);
                Toast.makeText(this, R.string.nav_error, Toast.LENGTH_SHORT).show();
            }
        });
        resendVerificationBtn.setOnClickListener(v -> resendVerificationEmail());
        registerBtn.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error starting RegisterActivity: " + e.getMessage(), e);
                Toast.makeText(this, R.string.nav_error, Toast.LENGTH_SHORT).show();
            }
        });
        backArrowBtn.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error starting RegisterActivity: " + e.getMessage(), e);
                Toast.makeText(this, R.string.nav_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startSplashScreen() {
        new CountDownTimer(3000, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                splashProgress.setProgress((int) (100 - (millisUntilFinished / 30)));
            }

            @Override
            public void onFinish() {
                splashLayout.setVisibility(View.GONE);
                loginLayout.setVisibility(View.VISIBLE);
                backArrowBtn.setVisibility(View.VISIBLE);
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

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    loginBtn.setEnabled(true);

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            try {
                                Intent intent = new Intent(LoginActivity.this, AccountActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            } catch (Exception e) {
                                Log.e(TAG, "Error starting AccountActivity: " + e.getMessage(), e);
                                Toast.makeText(LoginActivity.this, R.string.nav_error, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            resendVerificationBtn.setVisibility(View.VISIBLE);
                            Toast.makeText(LoginActivity.this, "Please verify your email", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void resendVerificationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Verification email sent", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Failed to send verification email", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(LoginActivity.this, R.string.please_sign_in, Toast.LENGTH_SHORT).show();
        }
    }
}