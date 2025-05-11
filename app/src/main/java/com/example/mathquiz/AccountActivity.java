package com.example.mathquiz;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
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

public class AccountActivity extends AppCompatActivity {
    private static final String TAG = "AccountActivity";
    private static final long MIN_CLICK_INTERVAL = 1000;
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
                loadQuizResults(user.getUid());
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

    private void loadQuizResults(String userId) {
        try {
            if (!isNetworkAvailable()) {
                quizResultsText.setText("Quiz Results: No internet");
                Log.w(TAG, "No network available for quiz results");
                return;
            }

            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Long score = documentSnapshot.getLong("totalScore");
                            String results = score != null ? "Total Score: " + score : "No quiz results";
                            quizResultsText.setText("Quiz Results: " + results);
                            Log.d(TAG, "Quiz results loaded: " + results);
                        } else {
                            quizResultsText.setText("Quiz Results: No data");
                            Log.w(TAG, "No quiz results for user: " + userId);
                        }
                    })
                    .addOnFailureListener(e -> {
                        quizResultsText.setText("Quiz Results: Error");
                        Log.e(TAG, "Error loading quiz results: " + e.getMessage(), e);
                    });
        } catch (Exception e) {
            quizResultsText.setText("Quiz Results: Error");
            Log.e(TAG, "Error in loadQuizResults: " + e.getMessage(), e);
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
