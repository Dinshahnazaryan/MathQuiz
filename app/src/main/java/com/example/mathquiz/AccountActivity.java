package com.example.mathquiz;

import android.annotation.SuppressLint;
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

    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_account);
            Log.d(TAG, "onCreate started");
            mAuth = FirebaseAuth.getInstance();
            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setTitle("");
                }
            }

            accountTitle = findViewById(R.id.accountTitle);
            emailTextView = findViewById(R.id.emailTextView);
            passwordNavButton = findViewById(R.id.passwordNavButton);
            signOutButton = findViewById(R.id.signOutButton);

            if (emailTextView == null || passwordNavButton == null || signOutButton == null || accountTitle == null) {
                Log.e(TAG, "UI elements missing: emailTextView=" + emailTextView +
                        ", passwordNavButton=" + passwordNavButton + ", signOutButton=" + signOutButton +
                        ", accountTitle=" + accountTitle);
                Toast.makeText(this, R.string.ui_init_failed, Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            accountTitle.setText(R.string.account_info);
            if (mAuth.getCurrentUser() == null) {
                Log.d(TAG, "No user signed in");
                emailTextView.setText(R.string.no_user_signed_in);
                passwordNavButton.setEnabled(false);
                signOutButton.setEnabled(false);
                Toast.makeText(this, R.string.please_sign_in, Toast.LENGTH_LONG).show();
            } else {
                String userEmail = mAuth.getCurrentUser().getEmail();
                Log.d(TAG, "User signed in: " + userEmail);
                emailTextView.setText(getString(R.string.email, userEmail != null ? userEmail : "Unknown"));
                passwordNavButton.setOnClickListener(v -> {
                    Log.d(TAG, "Navigating to PasswordOptionsActivity");
                    try {
                        startActivity(new Intent(AccountActivity.this, PasswordOptionsActivity.class));
                    } catch (Exception e) {
                        Log.e(TAG, "Error starting PasswordOptionsActivity: " + e.getMessage(), e);
                        Toast.makeText(this, getString(R.string.nav_error, e.getMessage()), Toast.LENGTH_LONG).show();
                    }
                });
                signOutButton.setOnClickListener(v -> {
                    Log.d(TAG, "Sign out clicked");
                    mAuth.signOut();
                    Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, getString(R.string.app_init_failed, e.getMessage()), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        Log.d(TAG, "Back arrow clicked");
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "System back pressed");
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}