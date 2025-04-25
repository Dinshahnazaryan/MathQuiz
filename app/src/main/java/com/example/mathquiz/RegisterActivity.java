package com.example.mathquiz;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private EditText emailInput, passwordInput;
    private Button registerBtn;
    private TextView loginLink;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_register);
            emailInput = findViewById(R.id.emailInput);
            passwordInput = findViewById(R.id.passwordInput);
            registerBtn = findViewById(R.id.registerBtn);
            loginLink = findViewById(R.id.loginLink);
            mAuth = FirebaseAuth.getInstance();
            if (emailInput == null || passwordInput == null || registerBtn == null || loginLink == null) {
                Log.e(TAG, "One or more views are null: emailInput=" + emailInput + ", passwordInput=" + passwordInput + ", registerBtn=" + registerBtn + ", loginLink=" + loginLink);
                Toast.makeText(this, "UI initialization failed", Toast.LENGTH_LONG).show();
                return;
            }
            registerBtn.setOnClickListener(v -> registerUser());
            loginLink.setOnClickListener(v -> {
                try {
                    Log.d(TAG, "Navigating to LoginActivity");
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to LoginActivity: " + e.getMessage());
                    Toast.makeText(this, "Navigation error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Initialization error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void registerUser() {
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
            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isNetworkAvailable()) {
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Registration successful, navigating to MainActivity");
                            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            if (errorMsg.contains("email address is already in use")) {
                                Toast.makeText(this, "Email is already registered", Toast.LENGTH_LONG).show();
                            } else if (errorMsg.contains("weak password")) {
                                Toast.makeText(this, "Password is too weak", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this, "Registration failed: " + errorMsg, Toast.LENGTH_LONG).show();
                            }
                            Log.e(TAG, "Registration failed: " + errorMsg);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in registerUser: " + e.getMessage(), e);
            Toast.makeText(this, "Registration error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}