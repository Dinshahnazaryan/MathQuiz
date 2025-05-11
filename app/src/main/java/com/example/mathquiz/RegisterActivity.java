package com.example.mathquiz;

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
                Toast.makeText(this, "UI initialization failed", Toast.LENGTH_LONG).show();
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
                    registerUser();
                }
            });
            loginText.setOnClickListener(v -> {
                if (isClickAllowed()) {
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
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

    private void registerUser() {
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
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                progressBar.setVisibility(View.GONE);
                registerBtn.setEnabled(true);
                loginText.setEnabled(true);
                Toast.makeText(this, "Invalid email", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$")) {
                progressBar.setVisibility(View.GONE);
                registerBtn.setEnabled(true);
                loginText.setEnabled(true);
                Toast.makeText(this, "Password must be 8+ chars with letters and numbers", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isNetworkAvailable()) {
                progressBar.setVisibility(View.GONE);
                registerBtn.setEnabled(true);
                loginText.setEnabled(true);
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
                return;
            }

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
                                                Toast.makeText(this, "Verification email sent to " + email, Toast.LENGTH_LONG).show();
                                            } else {
                                                String errorMsg = verifyTask.getException() != null ? verifyTask.getException().getMessage() : "Unknown error";
                                                Log.e(TAG, "Failed to send verification email: " + errorMsg);
                                                Toast.makeText(this, "Failed to send verification email: " + errorMsg, Toast.LENGTH_LONG).show();
                                            }
                                        });
                                mAuth.signOut();
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(this, "Registration failed: User not found", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            Log.e(TAG, "Registration failed: " + errorMsg);
                            Toast.makeText(this, "Registration failed: " + errorMsg, Toast.LENGTH_LONG).show();
                        }
                    });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            registerBtn.setEnabled(true);
            loginText.setEnabled(true);
            Log.e(TAG, "Error in registerUser: " + e.getMessage());
            Toast.makeText(this, "Registration error", Toast.LENGTH_LONG).show();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
