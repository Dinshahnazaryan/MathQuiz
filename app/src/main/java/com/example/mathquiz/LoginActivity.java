package com.example.mathquiz;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
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
    private Button loginBtn, resendVerificationBtn;
    private TextView forgotPasswordText;
    private LinearLayout splashLayout, loginLayout;
    private ProgressBar splashProgress;
    private TextView splashText;
    private FirebaseAuth mAuth;

    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_login);
            mAuth = FirebaseAuth.getInstance();
            emailInput = findViewById(R.id.emailInput);
            passwordInput = findViewById(R.id.passwordInput);
            loginBtn = findViewById(R.id.loginBtn);
            resendVerificationBtn = findViewById(R.id.resendVerificationBtn);
            forgotPasswordText = findViewById(R.id.forgotPasswordText);
            splashLayout = findViewById(R.id.splashLayout);
            loginLayout = findViewById(R.id.loginLayout);
            splashProgress = findViewById(R.id.splashProgress);
            splashText = findViewById(R.id.splashText);

            if (emailInput == null || passwordInput == null || loginBtn == null ||
                    resendVerificationBtn == null || forgotPasswordText == null ||
                    splashLayout == null || loginLayout == null || splashProgress == null ||
                    splashText == null) {
                Log.e(TAG, "UI elements missing: emailInput=" + emailInput +
                        ", passwordInput=" + passwordInput + ", loginBtn=" + loginBtn +
                        ", resendVerificationBtn=" + resendVerificationBtn +
                        ", forgotPasswordText=" + forgotPasswordText +
                        ", splashLayout=" + splashLayout + ", loginLayout=" + loginLayout +
                        ", splashProgress=" + splashProgress + ", splashText=" + splashText);
                Toast.makeText(this, R.string.ui_init_failed, Toast.LENGTH_LONG).show();
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

            loginBtn.setOnClickListener(v -> loginUser());
            forgotPasswordText.setOnClickListener(v -> {
                try {
                    Log.d(TAG, "Navigating to ForgotPasswordActivity");
                    Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error starting ForgotPasswordActivity: " + e.getMessage(), e);
                    Toast.makeText(this, getString(R.string.nav_error, e.getMessage()), Toast.LENGTH_LONG).show();
                }
            });
            resendVerificationBtn.setOnClickListener(v -> resendVerificationEmail());

            splashProgress.setMax(100);
            new CountDownTimer(3000, 100) {
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
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, getString(R.string.app_init_failed, e.getMessage()), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @SuppressLint("StringFormatInvalid")
    private void loginUser() {
        try {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, R.string.invalid_email, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isNetworkAvailable()) {
                Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null && user.isEmailVerified()) {
                                Log.d(TAG, "Login successful, email verified");
                                Toast.makeText(this, R.string.login_successful, Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            } else if (user != null) {
                                Log.d(TAG, "Email not verified for user: " + email);
                                Toast.makeText(this, R.string.verify_email, Toast.LENGTH_LONG).show();
                                resendVerificationBtn.setVisibility(View.VISIBLE);
                                mAuth.signOut();
                            } else {
                                Log.e(TAG, "User is null after login");
                                Toast.makeText(this, R.string.login_failed, Toast.LENGTH_LONG).show();
                            }
                        } else {
                            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            if (errorMsg.contains("no user record")) {
                                Toast.makeText(this, R.string.no_account, Toast.LENGTH_LONG).show();
                            } else if (errorMsg.contains("password is invalid")) {
                                Toast.makeText(this, R.string.incorrect_password, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this, getString(R.string.login_failed_with_error, errorMsg), Toast.LENGTH_LONG).show();
                            }
                            Log.e(TAG, "Login failed: " + errorMsg);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in loginUser: " + e.getMessage(), e);
            Toast.makeText(this, getString(R.string.login_error, e.getMessage()), Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("StringFormatInvalid")
    private void resendVerificationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && !user.isEmailVerified()) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, R.string.verification_email_resent, Toast.LENGTH_SHORT).show();
                        } else {
                            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            Toast.makeText(this, getString(R.string.verification_email_failed, errorMsg), Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            Toast.makeText(this, R.string.no_user_or_verified, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}