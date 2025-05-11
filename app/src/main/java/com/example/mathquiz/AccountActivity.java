package com.example.mathquiz;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

public class AccountActivity extends AppCompatActivity {
    private static final String TAG = "AccountActivity";
    private static final long MIN_CLICK_INTERVAL = 1000;
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 2000;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView emailText, quizResultsText, passwordToggle;
    private CardView passwordChangeLayout;
    private EditText passwordInput;
    private Button changePasswordBtn, backToStartBtn, signOutBtn, deleteAccountBtn;
    private long lastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_account);
            mAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();

            // Enable offline persistence
            db.enableNetwork().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Firestore network enabled");
                } else {
                    Log.e(TAG, "Failed to enable Firestore network: " + task.getException(), task.getException());
                }
            });

            emailText = findViewById(R.id.emailText);
            quizResultsText = findViewById(R.id.quizResultsText);
            passwordToggle = findViewById(R.id.passwordToggle);
            passwordChangeLayout = findViewById(R.id.passwordChangeLayout);
            passwordInput = findViewById(R.id.passwordInput);
            changePasswordBtn = findViewById(R.id.changePasswordBtn);
            backToStartBtn = findViewById(R.id.backToStartBtn);
            signOutBtn = findViewById(R.id.signOutBtn);
            deleteAccountBtn = findViewById(R.id.deleteAccountBtn);

            if (emailText == null || quizResultsText == null || passwordToggle == null ||
                    passwordChangeLayout == null || passwordInput == null || changePasswordBtn == null ||
                    backToStartBtn == null || signOutBtn == null || deleteAccountBtn == null) {
                Log.e(TAG, "UI elements missing");
                Toast.makeText(this, "UI initialization failed", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                String email = user.getEmail() != null ? user.getEmail() : "No email";
                emailText.setText("Email: " + email);
                Log.d(TAG, "Displaying user email: " + email);
                loadQuizResults(user.getUid(), 0);
            } else {
                Log.w(TAG, "No user signed in");
                Toast.makeText(this, "No user signed in", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return;
            }

            passwordToggle.setOnClickListener(v -> {
                if (isClickAllowed()) {
                    passwordChangeLayout.setVisibility(
                            passwordChangeLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE
                    );
                    Log.d(TAG, "Password change layout toggled: " + passwordChangeLayout.getVisibility());
                }
            });

            changePasswordBtn.setOnClickListener(v -> {
                if (isClickAllowed()) {
                    changePassword(user);
                }
            });

            backToStartBtn.setOnClickListener(v -> {
                if (isClickAllowed()) {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                }
            });

            signOutBtn.setOnClickListener(v -> {
                if (isClickAllowed()) {
                    mAuth.signOut();
                    Log.d(TAG, "User signed out");
                    Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            });

            deleteAccountBtn.setOnClickListener(v -> {
                if (isClickAllowed()) {
                    deleteAccount(user);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Account error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private boolean isClickAllowed() {
        long currentTime = SystemClock.elapsedRealtime();
        if (currentTime - lastClickTime < MIN_CLICK_INTERVAL) {
            return false;
        }
        lastClickTime = currentTime;
        return true;
    }

    private void loadQuizResults(String userId, int retryCount) {
        try {
            if (isNetworkAvailable()) {
                fetchFromServer(userId, retryCount);
            } else {
                Log.w(TAG, "No network available, attempting to load from cache");
                fetchFromCache(userId);
                Toast.makeText(this, "Offline: Trying cached quiz results", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            quizResultsText.setText("Quiz Results: Error");
            Log.e(TAG, "Error in loadQuizResults: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading quiz results: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void fetchFromServer(String userId, int retryCount) {
        db.collection("users").document(userId).get(Source.SERVER)
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Object scoreObj = documentSnapshot.get("totalScore");
                        String results = formatScore(scoreObj);
                        quizResultsText.setText("Quiz Results: " + results);
                        Log.d(TAG, "Quiz results loaded from server: " + results);
                    } else {
                        Log.w(TAG, "No document for user on server: " + userId);
                        fetchFromCache(userId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading quiz results from server: " + e.getMessage(), e);
                    if (retryCount < MAX_RETRIES && e.getMessage().contains("offline")) {
                        Log.d(TAG, "Retrying server fetch, attempt " + (retryCount + 1));
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            loadQuizResults(userId, retryCount + 1);
                        }, RETRY_DELAY_MS);
                    } else {
                        quizResultsText.setText("Quiz Results: Server offline");
                        Toast.makeText(this, "Server offline: Trying cached quiz results", Toast.LENGTH_LONG).show();
                        fetchFromCache(userId);
                    }
                });
    }

    private void fetchFromCache(String userId) {
        db.collection("users").document(userId).get(Source.CACHE)
                .addOnSuccessListener(cachedSnapshot -> {
                    if (cachedSnapshot.exists()) {
                        Object scoreObj = cachedSnapshot.get("totalScore");
                        String results = formatScore(scoreObj);
                        quizResultsText.setText("Quiz Results: " + results);
                        Log.d(TAG, "Quiz results loaded from cache: " + results);
                    } else {
                        quizResultsText.setText("Quiz Results: No cached data");
                        Log.w(TAG, "No cached document for user: " + userId);
                        Toast.makeText(this, "No quiz results in cache. Connect to internet to fetch data.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    quizResultsText.setText("Quiz Results: Cache error");
                    Log.e(TAG, "Error loading quiz results from cache: " + e.getMessage(), e);
                    Toast.makeText(this, "Failed to load quiz results from cache. Try connecting to internet.", Toast.LENGTH_LONG).show();
                });
    }

    private String formatScore(Object scoreObj) {
        if (scoreObj instanceof Long) {
            return "Total Score: " + scoreObj;
        } else if (scoreObj instanceof Integer) {
            return "Total Score: " + scoreObj;
        } else if (scoreObj instanceof String) {
            return "Total Score: " + scoreObj;
        } else {
            Log.w(TAG, "totalScore field missing or invalid type");
            return "No quiz results";
        }
    }

    private void changePassword(FirebaseUser user) {
        try {
            String newPassword = passwordInput.getText() != null ? passwordInput.getText().toString().trim() : "";
            Log.d(TAG, "Attempting to change password for user: " + user.getEmail());

            if (newPassword.isEmpty()) {
                Toast.makeText(this, "Please enter a new password", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Empty password input");
                return;
            }

            if (!newPassword.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$")) {
                Toast.makeText(this, "Password must be 8+ chars with letters and numbers", Toast.LENGTH_LONG).show();
                Log.w(TAG, "Invalid password format");
                return;
            }

            if (!isNetworkAvailable()) {
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "No network available");
                return;
            }

            user.updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Password updated successfully");
                            Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show();
                            passwordChangeLayout.setVisibility(View.GONE);
                            passwordInput.setText("");
                        } else {
                            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            Log.e(TAG, "Failed to update password: " + errorMsg, task.getException());
                            Toast.makeText(this, "Failed to update password: " + errorMsg, Toast.LENGTH_LONG).show();
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in changePassword: " + e.getMessage(), e);
            Toast.makeText(this, "Error changing password: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void deleteAccount(FirebaseUser user) {
        try {
            if (!isNetworkAvailable()) {
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "No network available");
                return;
            }

            user.delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Account deleted successfully");
                            Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            Log.e(TAG, "Failed to delete account: " + errorMsg, task.getException());
                            Toast.makeText(this, "Failed to delete account: " + errorMsg, Toast.LENGTH_LONG).show();
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in deleteAccount: " + e.getMessage(), e);
            Toast.makeText(this, "Error deleting account: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isAvailable = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        Log.d(TAG, "Network available: " + isAvailable);
        return isAvailable;
    }
}