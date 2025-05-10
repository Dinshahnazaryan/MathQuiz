package com.example.mathquiz;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

public class AccountActivity extends AppCompatActivity {
    private static final String TAG = "AccountActivity";

    private TextView emailText, quizResultsText;
    private EditText passwordInput;
    private Button changePasswordBtn, backToStartBtn, signOutBtn, deleteAccountBtn;
    private LinearLayout passwordToggle, passwordChangeLayout;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_account);
            mAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();

            emailText = findViewById(R.id.emailText);
            passwordToggle = findViewById(R.id.passwordToggle);
            passwordChangeLayout = findViewById(R.id.passwordChangeLayout);
            passwordInput = findViewById(R.id.passwordInput);
            changePasswordBtn = findViewById(R.id.changePasswordBtn);
            quizResultsText = findViewById(R.id.quizResultsText);
            backToStartBtn = findViewById(R.id.backToStartBtn);
            signOutBtn = findViewById(R.id.signOutBtn);
            deleteAccountBtn = findViewById(R.id.deleteAccountBtn);

            if (emailText == null || passwordToggle == null || passwordChangeLayout == null || passwordInput == null ||
                    changePasswordBtn == null || quizResultsText == null || backToStartBtn == null ||
                    signOutBtn == null || deleteAccountBtn == null) {
                Log.e(TAG, "One or more views are not found. Check activity_account.xml IDs.");
                Toast.makeText(this, "UI loading error", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
                return;
            }

            passwordToggle.setOnClickListener(v -> togglePasswordLayout());
            changePasswordBtn.setOnClickListener(v -> changePassword());
            signOutBtn.setOnClickListener(v -> signOut());
            deleteAccountBtn.setOnClickListener(v -> deleteAccount());
            backToStartBtn.setOnClickListener(v -> {
                startActivity(new Intent(AccountActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
            });

            loadUserData();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage());
            Toast.makeText(this, "Initialization failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        }
    }

    private void togglePasswordLayout() {
        if (passwordChangeLayout.getVisibility() == View.VISIBLE) {
            passwordChangeLayout.setVisibility(View.GONE);
        } else {
            passwordChangeLayout.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("SetTextI18n")
    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Log.w(TAG, "No user logged in, closing AccountActivity");
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
            return;
        }

        Log.d(TAG, "Loading data for user: " + user.getEmail());
        emailText.setText("Email: " + user.getEmail());

        try {
            db.collection("users").document(user.getUid()).collection("scores")
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.isEmpty()) {
                            quizResultsText.setText("No quiz results found");
                            return;
                        }

                        long totalQuizzes = snapshot.size();
                        double totalScore = 0;

                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Long correct = doc.getLong("correct");
                            Long incorrect = doc.getLong("incorrect");
                            if (correct != null && incorrect != null) {
                                double percent = (correct * 1.0 / (correct + incorrect)) * 100;
                                totalScore += percent;
                            }
                        }

                        double avg = totalScore / totalQuizzes;
                        quizResultsText.setText("Total Quizzes: " + totalQuizzes +
                                "\nAverage Score: " + String.format("%.1f%%", avg));
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading quiz data: " + e.getMessage());
                        quizResultsText.setText("Error loading quiz results");
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error querying Firestore: " + e.getMessage());
            quizResultsText.setText("Error loading quiz results");
        }
    }

    private void changePassword() {
        String newPassword = passwordInput.getText().toString().trim();
        if (newPassword.length() < 8) {
            Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show();
                            passwordInput.setText("");
                            passwordChangeLayout.setVisibility(View.GONE);
                        } else {
                            Log.e(TAG, "Failed to update password: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                            Toast.makeText(this, "Failed to update password: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Log.w(TAG, "No user logged in during password change");
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        }
    }

    private void signOut() {
        mAuth.signOut();
        startActivity(new Intent(this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    private void deleteAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Log.w(TAG, "No user logged in during account deletion");
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
            return;
        }

        String uid = user.getUid();

        try {
            db.collection("users").document(uid).collection("scores")
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        for (DocumentSnapshot doc : snapshot) {
                            doc.getReference().delete();
                        }

                        user.delete().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Account deleted for user: " + uid);
                                Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                finish();
                            } else {
                                Log.e(TAG, "Error deleting account: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                                Toast.makeText(this, "Error deleting account: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error deleting Firestore data: " + e.getMessage());
                        Toast.makeText(this, "Error deleting Firestore data", Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error querying Firestore for deletion: " + e.getMessage());
            Toast.makeText(this, "Error deleting Firestore data", Toast.LENGTH_SHORT).show();
        }
    }
}