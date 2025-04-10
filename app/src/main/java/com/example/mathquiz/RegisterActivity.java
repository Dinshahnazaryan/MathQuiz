package com.example.mathquiz;

import android.content.Intent;
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

            // Check if views are null
            if (emailInput == null || passwordInput == null || registerBtn == null || loginLink == null) {
                Log.e(TAG, "One or more views are null");
                Toast.makeText(this, "Error initializing UI", Toast.LENGTH_LONG).show();
                return;
            }

            registerBtn.setOnClickListener(v -> registerUser());
            loginLink.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    Log.e(TAG, "Error returning to LoginActivity: " + e.getMessage());
                    Toast.makeText(this, "Error returning to login: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage());
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

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            Toast.makeText(this, "Registration failed: " + errorMsg, Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Registration failed: " + errorMsg);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in registerUser: " + e.getMessage());
            Toast.makeText(this, "Registration error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}