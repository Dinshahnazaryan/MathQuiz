package com.example.mathquiz;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
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
    private FirebaseAuth mAuth;
    private EditText emailInput, passwordInput;
    private Button registerBtn;
    private TextView loginText;
    private ProgressBar progressBar;

    @SuppressLint("StringFormatInvalid")
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
                Log.e(TAG, "UI elements missing: emailInput=" + emailInput +
                        ", passwordInput=" + passwordInput + ", registerBtn=" + registerBtn +
                        ", loginText=" + loginText + ", progressBar=" + progressBar);
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

            registerBtn.setOnClickListener(v -> registerUser());
            loginText.setOnClickListener(v -> {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, getString(R.string.app_init_failed, e.getMessage()), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @SuppressLint("StringFormatInvalid")
    private void registerUser() {
        try {
            progressBar.setVisibility(View.VISIBLE);
            registerBtn.setEnabled(false);
            loginText.setEnabled(false);

            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                progressBar.setVisibility(View.GONE);
                registerBtn.setEnabled(true);
                loginText.setEnabled(true);
                Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                progressBar.setVisibility(View.GONE);
                registerBtn.setEnabled(true);
                loginText.setEnabled(true);
                Toast.makeText(this, R.string.invalid_email, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$")) {
                progressBar.setVisibility(View.GONE);
                registerBtn.setEnabled(true);
                loginText.setEnabled(true);
                Toast.makeText(this, R.string.invalid_password_format, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isNetworkAvailable()) {
                progressBar.setVisibility(View.GONE);
                registerBtn.setEnabled(true);
                loginText.setEnabled(true);
                Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
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
                                                Toast.makeText(this, R.string.registration_successful, Toast.LENGTH_LONG).show();
                                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Toast.makeText(this, getString(R.string.verification_email_failed, verifyTask.getException().getMessage()), Toast.LENGTH_LONG).show();
                                            }
                                        });
                            } else {
                                Toast.makeText(this, R.string.registration_failed, Toast.LENGTH_LONG).show();
                            }
                        } else {
                            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            Toast.makeText(this, getString(R.string.registration_failed_with_error, errorMsg), Toast.LENGTH_LONG).show();
                        }
                    });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            registerBtn.setEnabled(true);
            loginText.setEnabled(true);
            Log.e(TAG, "Error in registerUser: " + e.getMessage(), e);
            Toast.makeText(this, getString(R.string.registration_error, e.getMessage()), Toast.LENGTH_LONG).show();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}