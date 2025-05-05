package com.example.mathquiz;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class AccountActivity extends AppCompatActivity {
    private static final String TAG = "AccountActivity";

    private TextView emailText, quizResultsText;
    private EditText passwordInput;
    private Button changePasswordBtn, backToStartBtn;
    private LinearLayout passwordToggle, passwordChangeLayout;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        try {
            mAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();
            initializeUI();
            loadUserData();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, R.string.app_init_failed, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initializeUI() {
        emailText = findViewById(R.id.emailText);
        passwordToggle = findViewById(R.id.passwordToggle);
        passwordChangeLayout = findViewById(R.id.passwordChangeLayout);
        passwordInput = findViewById(R.id.passwordInput);
        changePasswordBtn = findViewById(R.id.changePasswordBtn);
        quizResultsText = findViewById(R.id.quizResultsText);
        backToStartBtn = findViewById(R.id.backToStartBtn);

        if (emailText == null || passwordToggle == null || passwordChangeLayout == null ||
                passwordInput == null || changePasswordBtn == null || quizResultsText == null ||
                backToStartBtn == null) {
            Log.e(TAG, "UI elements missing");
            Toast.makeText(this, R.string.ui_init_failed, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        passwordToggle.setOnClickListener(v -> {
            Log.d(TAG, "Password toggle clicked");
            passwordChangeLayout.setVisibility(
                    passwordChangeLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });

        changePasswordBtn.setOnClickListener(v -> changePassword());

        backToStartBtn.setOnClickListener(v -> {
            Log.d(TAG, "Back to Start clicked");
            Intent intent = new Intent(AccountActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void changePassword() {
        String newPassword = passwordInput.getText().toString().trim();
        if (newPassword.isEmpty()) {
            Toast.makeText(this, "Please enter a new password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$")) {
            Toast.makeText(this, R.string.invalid_password_format, Toast.LENGTH_LONG).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Log.e(TAG, "No user logged in");
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        changePasswordBtn.setEnabled(false);
        user.updatePassword(newPassword)
                .addOnCompleteListener(task -> {
                    changePasswordBtn.setEnabled(true);
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Password updated for: " + user.getEmail());
                        Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                        passwordInput.setText("");
                        passwordChangeLayout.setVisibility(View.GONE);
                    } else {
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Log.e(TAG, "Failed to update password: " + errorMsg, task.getException());
                        Toast.makeText(this, "Failed to update password: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Log.e(TAG, "No user logged in");
            Toast.makeText(this, "User not logged in", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(AccountActivity.this, RegisterActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return;
        }

        String email = user.getEmail();
        if (email == null || email.isEmpty()) {
            Log.e(TAG, "No email found for user");
            Toast.makeText(this, "User email not found", Toast.LENGTH_LONG).show();
            return;
        }

        emailText.setText("Email: " + email);

        db.collection("users").document(user.getUid()).collection("scores")
                .get()
                .addOnCompleteListener(task -> {
                    try {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                long totalQuizzes = querySnapshot.size();
                                double totalPercentage = 0;
                                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                    Long correct = document.getLong("correct");
                                    Long incorrect = document.getLong("incorrect");
                                    Log.d(TAG, "Document: " + document.getId() + ", correct: " + correct + ", incorrect: " + incorrect);
                                    if (correct != null && incorrect != null && (correct + incorrect) > 0) {
                                        double percentage = (correct / (double) (correct + incorrect)) * 100;
                                        totalPercentage += percentage;
                                    } else {
                                        Log.w(TAG, "Invalid score data for document: " + document.getId());
                                    }
                                }
                                double averagePercentage = totalQuizzes > 0 ? totalPercentage / totalQuizzes : 0;
                                String results = "Total Quizzes: " + totalQuizzes + "\n" +
                                        "Average Score: " + String.format("%.1f%%", averagePercentage);
                                quizResultsText.setText(results);
                                Log.d(TAG, "Quiz results loaded: " + results);
                            } else {
                                quizResultsText.setText("No quiz results found");
                                Log.d(TAG, "No quiz results found for: " + email);
                            }
                        } else {
                            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            Log.e(TAG, "Failed to load quiz data: " + errorMsg, task.getException());
                            Toast.makeText(this, "Failed to load data: " + errorMsg, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing quiz data: " + e.getMessage(), e);
                        Toast.makeText(this, R.string.app_error, Toast.LENGTH_LONG).show();
                    }
                });
    }
}