package com.example.mathquiz;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mathquiz.R;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private EditText emailInput, passwordInput;
    private Button loginButton;
    private FirebaseAuth mAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Make sure this layout exists

        mAuth = FirebaseAuth.getInstance();
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            if (!email.isEmpty() && !password.isEmpty()) {
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener(authResult -> {
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("email", email);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                        );
            } else {
                Toast.makeText(this, "Email and password required", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
