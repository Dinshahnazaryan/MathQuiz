package com.example.mathquiz;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AccountActivity extends AppCompatActivity {
    private static final String TAG = "AccountActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView emailText, quizResultsText, achievementsText, passwordToggle;
    private EditText passwordInput;
    private Button changePasswordBtn, backToStartBtn, signOutBtn, deleteAccountBtn, topicExplanationsBtn;
    private CardView passwordChangeLayout;
    private ProgressBar quizProgressBar, deleteProgressBar;
    private long lastClickTime = 0;
    private static final long CLICK_INTERVAL = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_account);
            mAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null || !currentUser.isEmailVerified()) {
                Log.w(TAG, "No verified user found, redirecting to LoginActivity");
                showMessage("Please log in to continue", false);
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return;
            }

            initViews();
            setupClickListeners();
            displayUserData();
            applyAnimations();

            Animation scaleAnim = AnimationUtils.loadAnimation(this, R.anim.scale_button);
            View.OnTouchListener scaleTouchListener = (v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.startAnimation(scaleAnim);
                }
                return false;
            };
            if (changePasswordBtn != null) changePasswordBtn.setOnTouchListener(scaleTouchListener);
            if (backToStartBtn != null) backToStartBtn.setOnTouchListener(scaleTouchListener);
            if (signOutBtn != null) signOutBtn.setOnTouchListener(scaleTouchListener);
            if (deleteAccountBtn != null) deleteAccountBtn.setOnTouchListener(scaleTouchListener);
            if (topicExplanationsBtn != null) topicExplanationsBtn.setOnTouchListener(scaleTouchListener);
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            showErrorDialog("Initialization failed");
            finish();
        }
    }

    private void initViews() {
        emailText = findViewById(R.id.emailText);
        quizResultsText = findViewById(R.id.quizResultsText);
        achievementsText = findViewById(R.id.achievementsText);
        passwordToggle = findViewById(R.id.passwordToggle);
        passwordInput = findViewById(R.id.passwordInput);
        changePasswordBtn = findViewById(R.id.changePasswordBtn);
        backToStartBtn = findViewById(R.id.backToStartBtn);
        signOutBtn = findViewById(R.id.signOutBtn);
        deleteAccountBtn = findViewById(R.id.deleteAccountBtn);
        topicExplanationsBtn = findViewById(R.id.topicExplanationsBtn);
        passwordChangeLayout = findViewById(R.id.passwordChangeLayout);
        quizProgressBar = findViewById(R.id.quizProgressBar);
        deleteProgressBar = findViewById(R.id.deleteProgressBar);

        if (emailText == null || quizResultsText == null || achievementsText == null ||
                passwordToggle == null || passwordInput == null || changePasswordBtn == null ||
                backToStartBtn == null || signOutBtn == null || deleteAccountBtn == null ||
                topicExplanationsBtn == null || passwordChangeLayout == null || quizProgressBar == null) {
            Log.e(TAG, "One or more critical views not found");
            showErrorDialog("UI initialization failed");
            finish();
            return;
        }
        if (deleteProgressBar == null) {
            Log.w(TAG, "deleteProgressBar not found; deletion will proceed without loading indicator");
        }
    }

    private void setupClickListeners() {
        if (passwordToggle != null) {
            passwordToggle.setOnClickListener(v -> {
                if (isClickAllowed()) {
                    if (passwordChangeLayout.getVisibility() == View.VISIBLE) {
                        passwordChangeLayout.setVisibility(View.GONE);
                    } else {
                        passwordChangeLayout.setVisibility(View.VISIBLE);
                        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
                        passwordChangeLayout.startAnimation(fadeIn);
                    }
                    Log.d(TAG, "Password change layout visibility: " + (passwordChangeLayout.getVisibility() == View.VISIBLE ? "VISIBLE" : "GONE"));
                }
            });
        }

        if (changePasswordBtn != null) {
            changePasswordBtn.setOnClickListener(v -> {
                if (isClickAllowed()) {
                    String newPassword = passwordInput != null ? passwordInput.getText().toString().trim() : "";
                    if (newPassword.length() < 6) {
                        showMessage("Password must be at least 6 characters", false);
                        return;
                    }
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        if (deleteProgressBar != null) deleteProgressBar.setVisibility(View.VISIBLE);
                        user.updatePassword(newPassword)
                                .addOnSuccessListener(aVoid -> {
                                    showMessage("Password updated successfully", false);
                                    passwordChangeLayout.setVisibility(View.GONE);
                                    passwordInput.setText("");
                                    if (deleteProgressBar != null) deleteProgressBar.setVisibility(View.GONE);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Password update failed: " + e.getMessage(), e);
                                    showMessage("Password update failed: " + e.getMessage(), false);
                                    if (deleteProgressBar != null) deleteProgressBar.setVisibility(View.GONE);
                                });
                    } else {
                        Log.w(TAG, "No user found for password update");
                        showMessage("No user signed in", false);
                    }
                }
            });
        }

        if (backToStartBtn != null) {
            backToStartBtn.setOnClickListener(v -> {
                if (isClickAllowed()) {
                    Log.d(TAG, "Navigating back to MainActivity");
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }
            });
        }

        if (signOutBtn != null) {
            signOutBtn.setOnClickListener(v -> {
                if (isClickAllowed()) {
                    Log.d(TAG, "Signing out user");
                    mAuth.signOut();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            });
        }

        if (deleteAccountBtn != null) {
            deleteAccountBtn.setOnClickListener(v -> {
                if (isClickAllowed()) {
                    Log.d(TAG, "Delete account button clicked");
                    showDeleteConfirmationDialog();
                }
            });
        }

        if (topicExplanationsBtn != null) {
            topicExplanationsBtn.setOnClickListener(v -> {
                if (isClickAllowed()) {
                    Log.d(TAG, "Navigating to TopicExplanationActivity");
                    Intent intent = new Intent(this, TopicExplanationActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    private void showDeleteConfirmationDialog() {
        Log.d(TAG, "Showing delete confirmation dialog");
        new AlertDialog.Builder(this)
                .setTitle("Confirm Account Deletion")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Log.d(TAG, "User confirmed deletion, proceeding with deletion");
                    deleteAccount();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    Log.d(TAG, "User cancelled deletion");
                    dialog.dismiss();
                })
                .setCancelable(true)
                .show();
    }

    private void deleteAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Log.w(TAG, "No user found for deletion");
            showMessage("No user signed in", false);
            return;
        }

        Log.d(TAG, "Attempting to delete account for user: " + user.getEmail());
        deleteAccountBtn.setEnabled(false);
        if (deleteProgressBar != null) deleteProgressBar.setVisibility(View.VISIBLE);

        String userId = user.getUid();
        user.delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Firebase Auth account deleted, deleting Firestore data");
                    db.collection("users").document(userId).delete()
                            .addOnSuccessListener(aVoid2 -> {
                                Log.d(TAG, "Firestore data deleted successfully");
                                showMessage("Account deleted successfully", false);
                                Intent intent = new Intent(this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to delete Firestore data: " + e.getMessage(), e);
                                showMessage("Account deleted, but failed to clear data: " + e.getMessage(), false);
                                Intent intent = new Intent(this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Account deletion failed: " + e.getMessage(), e);
                    String errorMessage = e.getMessage();
                    String toastMessage = "Account deletion failed: " + e.getMessage();
                    if (errorMessage != null) {
                        if (errorMessage.contains("recent authentication")) {
                            toastMessage = "Session expired. Please sign in again.";
                            Log.d(TAG, "Recent authentication required, redirecting to LoginActivity");
                            mAuth.signOut();
                            Intent intent = new Intent(this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else if (errorMessage.contains("network")) {
                            toastMessage = "Network error. Please check your connection.";
                        }
                    }
                    showMessage(toastMessage, false);
                    deleteAccountBtn.setEnabled(true);
                    if (deleteProgressBar != null) deleteProgressBar.setVisibility(View.GONE);
                });
    }

    private boolean isClickAllowed() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < CLICK_INTERVAL) {
            Log.w(TAG, "Click blocked due to rapid successive clicks");
            return false;
        }
        lastClickTime = currentTime;
        return true;
    }

    private void displayUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && emailText != null) {
            emailText.setText("Email: " + user.getEmail());
            Log.d(TAG, "Displaying user email: " + user.getEmail());
        }

        String userId = user != null ? user.getUid() : "";
        if (userId.isEmpty()) {
            Log.w(TAG, "No user ID found for Firestore query");
            return;
        }

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long totalScore = documentSnapshot.getLong("totalScore");
                        Long lastQuizPercentage = documentSnapshot.getLong("lastQuizPercentage");
                        Long level = documentSnapshot.getLong("level");

                        if (quizResultsText != null) {
                            String results = "Total Correct: " + (totalScore != null ? totalScore : 0) +
                                    ", Last Quiz: " + (lastQuizPercentage != null ? lastQuizPercentage + "%" : "N/A") +
                                    ", Level: " + (level != null ? level : 1);
                            quizResultsText.setText(results);
                            Log.d(TAG, "User data displayed: " + results);
                        }

                        if (quizProgressBar != null && lastQuizPercentage != null) {
                            quizProgressBar.setMax(100);
                            quizProgressBar.setProgress(lastQuizPercentage.intValue());
                            Log.d(TAG, "Quiz progress bar set to: " + lastQuizPercentage);
                        }

                        if (achievementsText != null) {
                            String achievements = "Achievements: ";
                            if (totalScore != null && totalScore >= 50) {
                                achievements += "Math Master (50+ correct)";
                            } else if (totalScore != null && totalScore >= 20) {
                                achievements += "Math Enthusiast (20+ correct)";
                            } else {
                                achievements += "No achievements yet";
                            }
                            achievementsText.setText(achievements);
                            Log.d(TAG, "Achievements displayed: " + achievements);
                        }
                    } else {
                        Log.w(TAG, "No user data found in Firestore");
                        if (quizResultsText != null) {
                            quizResultsText.setText("Quiz Results: No data");
                        }
                        if (achievementsText != null) {
                            achievementsText.setText("Achievements: No achievements yet");
                        }
                        if (quizProgressBar != null) {
                            quizProgressBar.setMax(100);
                            quizProgressBar.setProgress(0);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch user data: " + e.getMessage(), e);
                    showMessage("Failed to load user data: " + e.getMessage(), false);
                });
    }

    private void applyAnimations() {
        try {
            Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            findViewById(R.id.MenuTitle).startAnimation(fadeIn);
            ((View) emailText.getParent().getParent()).startAnimation(fadeIn);
            ((View) quizResultsText.getParent().getParent()).startAnimation(fadeIn);
            ((View) passwordToggle.getParent().getParent()).startAnimation(fadeIn);
            ((View) achievementsText.getParent().getParent()).startAnimation(fadeIn);
            ((View) topicExplanationsBtn.getParent().getParent()).startAnimation(fadeIn);
            ((View) backToStartBtn.getParent().getParent()).startAnimation(fadeIn);
            ((View) signOutBtn.getParent().getParent()).startAnimation(fadeIn);
            ((View) deleteAccountBtn.getParent().getParent()).startAnimation(fadeIn);
            Log.d(TAG, "Animations applied to UI elements");
        } catch (Exception e) {
            Log.e(TAG, "Failed to apply animations: " + e.getMessage(), e);
        }
    }

    private void showMessage(String message, boolean isError) {
        Log.d(TAG, "Showing message: " + message);
        try {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "Failed to show Toast: " + e.getMessage(), e);
            showErrorDialog(message);
        }
    }

    private void showErrorDialog(String message) {
        Log.d(TAG, "Showing error dialog: " + message);
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }
}