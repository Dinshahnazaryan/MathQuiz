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
    private TextView emailTextView;
    private ImageButton passwordNavButton;
    private Button signOutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        Log.d(TAG, "AccountActivity opened");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        mAuth = FirebaseAuth.getInstance();

        emailTextView = findViewById(R.id.emailTextView);
        passwordNavButton = findViewById(R.id.passwordNavButton);
        signOutButton = findViewById(R.id.signOutButton);

        if (mAuth.getCurrentUser() == null) {
            Log.d(TAG, "No user signed in, disabling account features");
            emailTextView.setText("Email: No user signed in");
            passwordNavButton.setEnabled(false);
            signOutButton.setEnabled(false);
            Toast.makeText(this, "Please sign in to access account features", Toast.LENGTH_LONG).show();
        } else {
            String userEmail = mAuth.getCurrentUser().getEmail();
            Log.d(TAG, "User signed in: " + userEmail);
            emailTextView.setText("Email: " + userEmail);
            passwordNavButton.setOnClickListener(v -> {
                Log.d(TAG, "Navigating to PasswordOptionsActivity");
                Intent intent = new Intent(AccountActivity.this, PasswordOptionsActivity.class);
                startActivity(intent);
            });

            signOutButton.setOnClickListener(v -> {
                Log.d(TAG, "Sign out clicked, navigating to LoginActivity");
                mAuth.signOut();
                Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        Log.d(TAG, "Back arrow clicked, navigating to MainActivity");
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "System back pressed, navigating to MainActivity");
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}