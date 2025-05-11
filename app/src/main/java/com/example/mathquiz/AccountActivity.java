 package com.example.mathquiz;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccountActivity extends AppCompatActivity {
    private EditText newPasswordEditText, confirmPasswordEditText;
    private Button changePasswordButton;
    private CardView passwordCardView;
    private FirebaseAuth mAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        mAuth = FirebaseAuth.getInstance();
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        changePasswordButton = findViewById(R.id.changePasswordButton);
        passwordCardView = findViewById(R.id.passwordCardView);
        changePasswordButton.setOnClickListener(v -> changePassword());
    }

    private void changePassword() {
        String newPassword = newPasswordEditText != null && newPasswordEditText.getText() != null
                ? newPasswordEditText.getText().toString().trim()
                : "";
        String confirmPassword = confirmPasswordEditText != null && confirmPasswordEditText.getText() != null
                ? confirmPasswordEditText.getText().toString().trim()
                : "";
        if (newPassword.isEmpty()) {
            Toast.makeText(this, "New password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newPassword.length() < 8) {
            Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(AccountActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AccountActivity.this, "Failed to update password", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }
}
