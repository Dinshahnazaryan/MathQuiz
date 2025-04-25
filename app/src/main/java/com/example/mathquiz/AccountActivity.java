package com.example.mathquiz;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;

public class AccountActivity extends AppCompatActivity {

    private static final String TAG = "AccountActivity";
    private FirebaseAuth mAuth;
    private TextView emailTextView, accountTitle;
    private ImageButton passwordNavButton;
    private Button signOutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_account);
            Log.d(TAG, "AccountActivity: onCreate started");

            mAuth = FirebaseAuth.getInstance();
            if (mAuth == null) {
                Log.e(TAG, "FirebaseAuth is null");
                Toast.makeText(this, "Authentication error", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setTitle("");
                } else {
                    Log.w(TAG, "getSupportActionBar returned null");
                }
            } else {
                Log.e(TAG, "Toolbar not found");
            }

            accountTitle = findViewById(R.id.accountTitle);
            emailTextView = findViewById(R.id.emailTextView);
            passwordNavButton = findViewById(R.id.passwordNavButton);
            signOutButton = findViewById(R.id.signOutButton);

            if (emailTextView == null || passwordNavButton == null || signOutButton == null) {
                Log.e(TAG, "UI elements missing: emailTextView=" + emailTextView +
                        ", passwordNavButton=" + passwordNavButton + ", signOutButton=" + signOutButton);
                Toast.makeText(this, "UI initialization failed", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            if (accountTitle != null) {
                accountTitle.setText("Account Information");
            }

            if (mAuth.getCurrentUser() == null) {
                Log.d(TAG, "No user signed in");
                emailTextView.setText("Email: No user signed in");
                passwordNavButton.setEnabled(false);
                signOutButton.setEnabled(false);
                Toast.makeText(this, "Please sign in", Toast.LENGTH_LONG).show();
            } else {
                String userEmail = mAuth.getCurrentUser().getEmail();
                Log.d(TAG, "User signed in: " + userEmail);
                emailTextView.setText("Email: " + (userEmail != null ? userEmail : "Unknown"));
                passwordNavButton.setOnClickListener(v -> {
                    Log.d(TAG, "Navigating to PasswordOptionsActivity");
                    try {
                        startActivity(new Intent(AccountActivity.this, PasswordOptionsActivity.class));
                    } catch (Exception e) {
                        Log.e(TAG, "Error starting PasswordOptionsActivity: " + e.getMessage(), e);
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

                signOutButton.setOnClickListener(v -> {
                    Log.d(TAG, "Sign out clicked");
                    mAuth.signOut();
                    Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        Log.d(TAG, "Back arrow clicked");
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "System back pressed");
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}