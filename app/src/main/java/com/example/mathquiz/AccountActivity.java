package com.example.mathquiz;

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

import java.util.HashMap;
import java.util.Map;

public class AccountActivity extends AppCompatActivity {
    private static final String TAG = "AccountActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView emailText, quizResultsText, achievementsText, passwordToggle;
    private EditText passwordInput;
    private Button changePasswordBtn, backToStartBtn, signOutBtn, deleteAccountBtn, topicExplanationsBtn;
    private CardView passwordChangeLayout;
    private ProgressBar quizProgressBar;
    private long lastClickTime = 0;
    private static final long CLICK_INTERVAL = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || !currentUser.isEmailVerified()) {
            Toast.makeText(this, "Please log in to continue", Toast.LENGTH_SHORT).show();
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

        if (emailText == null || quizResultsText == null || achievementsText == null ||
                passwordToggle == null || passwordInput == null || changePasswordBtn == null ||
                backToStartBtn == null || signOutBtn == null || deleteAccountBtn == null ||
                topicExplanationsBtn == null || passwordChangeLayout == null || quizProgressBar == null) {
            Log.e(TAG, "One or more views not found");
            Toast.makeText(this, "UI initialization failed", Toast.LENGTH_LONG).show();
            finish();
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
                    Log.d(TAG, "Password change layout toggled: " + passwordChangeLayout.getVisibility());
                }
            });
        }

        if (changePasswordBtn != null) {
            changePasswordBtn.setOnClickListener(v -> {
                if (isClickAllowed()) {
                    String newPassword = passwordInput != null ? passwordInput.getText().toString().trim() : "";
                    if (newPassword.length() < 6) {
                        Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        user.updatePassword(newPassword)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                    passwordChangeLayout.setVisibility(View.GONE);
                                    passwordInput.setText("");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Password update failed: " + e.getMessage(), e);
                                    Toast.makeText(this, "Password update failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    }
                }
            });
        }

        if (backToStartBtn != null) {
            backToStartBtn.setOnClickListener(v -> {
                if (isClickAllowed()) {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }
            });
        }

        if (signOutBtn != null) {
            signOutBtn.setOnClickListener(v -> {
                if (isClickAllowed()) {
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
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        String userId = user.getUid();
                        user.delete()
                                .addOnSuccessListener(aVoid -> {
                                    db.collection("users").document(userId).delete()
                                            .addOnSuccessListener(aVoid1 -> {
                                                Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(this, LoginActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e(TAG, "Failed to delete user data: " + e.getMessage(), e);
                                                Toast.makeText(this, "Failed to delete account data", Toast.LENGTH_LONG).show();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Account deletion failed: " + e.getMessage(), e);
                                    Toast.makeText(this, "Account deletion failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    }
                }
            });
        }

        if (topicExplanationsBtn != null) {
            topicExplanationsBtn.setOnClickListener(v -> {
                if (isClickAllowed()) {
                    Intent intent = new Intent(this, TopicExplanationActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    private boolean isClickAllowed() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < CLICK_INTERVAL) {
            return false;
        }
        lastClickTime = currentTime;
        return true;
    }

    private void displayUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && emailText != null) {
            emailText.setText("Email: " + user.getEmail());
        }

        db.collection("users").document(user != null ? user.getUid() : "")
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
                        }

                        if (quizProgressBar != null && lastQuizPercentage != null) {
                            quizProgressBar.setMax(100);
                            quizProgressBar.setProgress(lastQuizPercentage.intValue());
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
                        }
                    } else {
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
                    Toast.makeText(this, "Failed to load user data", Toast.LENGTH_LONG).show();
                });
    }

    private void applyAnimations() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        findViewById(R.id.MenuTitle).startAnimation(fadeIn);
        ((View) emailText.getParent().getParent()).startAnimation(fadeIn);
        ((View) quizResultsText.getParent().getParent()).startAnimation(fadeIn);
        ((View) passwordToggle.getParent().getParent()).startAnimation(fadeIn);
        ((View) achievementsText.getParent().getParent()).startAnimation(fadeIn);
        ((View) topicExplanationsBtn.getParent().getParent()).startAnimation(fadeIn);
        ((View) backToStartBtn.getParent().getParent()).startAnimation(fadeIn);
        ((View) signOutBtn.getParent().getParent()).startAnimation(fadeIn);
    }
}