package com.example.mathquiz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class AccountActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView emailTextView;
    private ImageButton passwordNavButton;
    private Button signOutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        mAuth = FirebaseAuth.getInstance();

        emailTextView = findViewById(R.id.emailTextView);
        passwordNavButton = findViewById(R.id.passwordNavButton);
        signOutButton = findViewById(R.id.signOutButton);

        String userEmail = mAuth.getCurrentUser() != null ?
                mAuth.getCurrentUser().getEmail() : "No user signed in";
        emailTextView.setText("Email: " + userEmail);

        passwordNavButton.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, PasswordOptionsActivity.class);
            startActivity(intent);
        });

        signOutButton.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}