 package com.example.mathquiz;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

public class AccountActivity extends AppCompatActivity {
    private static final String TAG = "AccountActivity";

    private TextView emailText, quizResultsText, passwordToggle;
    private EditText passwordInput;
    private Button changePasswordBtn, backToStartBtn, signOutBtn, deleteAccountBtn;
    private CardView passwordChangeLayout; // Changed from LinearLayout to CardView
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Starting onCreate");
        try {
            Log.d(TAG, "Checking resource availability");
            Resources res = getResources();
            try {
                res.getDrawable(R.drawable.edittext_background_new);
                res.getDrawable(R.drawable.button_background);
                res.getColor(R.color.dark_blue);
                res.getColor(R.color.white);
                res.getColor(R.color.gray_700);
                res.getColor(R.color.gray_400);
                res.getColor(R.color.primary_blue);
                Log.d(TAG, "All required resources found");
            } catch (Resources.NotFoundException e) {
                Log.e(TAG, "Resource missing: " + e.getMessage(), e);
                Toast.makeText(this, "Error: Missing resource " + e.getMessage(), Toast.LENGTH_LONG).show();
                navigateToMain();
                return;
            }

            Log.d(TAG, "Setting content view: R.layout.activity_account");
            setContentView(R.layout.activity_account);
            Log.d(TAG, "Content view set successfully");

            mAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();

            Log.d(TAG, "Binding UI elements");
            emailText = findViewById(R.id.emailText);
            passwordToggle = findViewById(R.id.passwordToggle);
            passwordChangeLayout = findViewById(R.id.passwordChangeLayout);
            passwordInput = findViewById(R.id.passwordInput);
            changePasswordBtn = findViewById(R.id.changePasswordBtn);
            quizResultsText = findViewById(R.id.quizResultsText);
            backToStartBtn = findViewById(R.id.backToStartBtn);
            signOutBtn = findViewById(R.id.signOutBtn);
            deleteAccountBtn = findViewById(R.id.deleteAccountBtn);

            String missingViews = checkMissingViews();
            if (!missingViews.isEmpty()) {
                Log.e(TAG, "Missing UI elements: " + missingViews);
                Toast.makeText(this, "Error: Missing UI elements " + missingViews, Toast.LENGTH_LONG).show();
                navigateToMain();
                return;
            }

            Log.d(TAG, "Setting up click listeners");
            setupClickListeners();
            Log.d(TAG, "Loading user data");
            loadUserData();
        } catch (Exception e) {
            Log.e(TAG, "Initialization error: " + e.getMessage(), e);
            Log.e(TAG, "Exception type: " + e.getClass().getName());
            Log.e(TAG, "Stack trace: ", e);
            Toast.makeText(this, "Account screen error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            navigateToMain();
        }
    }

    private String checkMissingViews() {
        StringBuilder missing = new StringBuilder();
        if (emailText == null) missing.append("emailText ");
        if (passwordToggle == null) missing.append("passwordToggle ");
        if (passwordChangeLayout == null) missing.append("passwordChangeLayout ");
        if (passwordInput == null) missing.append("passwordInput ");
        if (changePasswordBtn == null) missing.append("changePasswordBtn ");
        if (quizResultsText == null) missing.append("quizResultsText ");
        if (backToStartBtn == null) missing.append("backToStartBtn ");
        if (signOutBtn == null) missing.append("signOutBtn ");
        if (deleteAccountBtn == null) missing.append("deleteAccountBtn ");
        return missing.toString().trim();
    }

    private void setupClickListeners() {
        passwordToggle.setOnClickListener(v -> togglePasswordLayout());
        changePasswordBtn.setOnClickListener(v -> changePassword());
        signOutBtn.setOnClickListener(v -> signOut());
        deleteAccountBtn.setOnClickListener(v -> deleteAccount());
        backToStartBtn.setOnClickListener(v -> navigateToMain());
    }

    private void navigateToMain() {
        Log.d(TAG, "Navigating to MainActivity");
        startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
    }

    private void togglePasswordLayout() {
        if (passwordChangeLayout.getVisibility() == View.VISIBLE) {
            passwordChangeLayout.setVisibility(View.GONE);
        } else {
            passwordChangeLayout.setVisibility(View.VISIBLE);
        }
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Log.w(TAG, "No user logged in, redirecting to LoginActivity");
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
            return;
        }

        Log.d(TAG, "Loading data for user: " + user.getEmail());
        emailText.setText("Email: " + user.getEmail());

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
                            Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show();
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
        Log.d(TAG, "Signing out user");
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
                            Toast.makeText(this, "Error deleting account", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting Firestore data: " + e.getMessage());
                    Toast.makeText(this, "Error deleting Firestore data", Toast.LENGTH_SHORT).show();
                });
    }
}
