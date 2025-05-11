package com.example.mathquiz;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LauncherActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null && currentUser.isEmailVerified()) {
            // Auto-login if already verified
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("email", currentUser.getEmail());
            startActivity(intent);
        } else {
            // Go to login screen
            startActivity(new Intent(this, LoginActivity.class));
        }

        finish();
    }
}
