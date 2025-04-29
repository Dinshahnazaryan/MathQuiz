package com.example.mathquiz;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private static final String TAG = "ChangePasswordActivity";
    private FirebaseAuth mAuth;
    private EditText currentPasswordEditText;
    private EditText newPasswordEditText;
    private EditText confirmPasswordEditText;
    private Button submitButton;
    private Button cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_change_password);
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
            mAuth = FirebaseAuth.getInstance();
            currentPasswordEditText = findViewById(R.id.currentPasswordEditText);
            newPasswordEditText = findViewById(R.id.newPasswordEditText);
            confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
            submitButton = findViewById(R.id.submitButton);
            cancelButton = findViewById(R.id.cancelButton);

            if (currentPasswordEditText == null || newPasswordEditText == null ||
                    confirmPasswordEditText == null || submitButton == null || cancelButton == null) {
                Log.e(TAG, "One or more views are null");
                Toast.makeText(this, "UI initialization failed", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            submitButton.setOnClickListener(v -> changePassword());
            cancelButton.setOnClickListener(v -> finish());
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Initialization error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void changePassword() {
        try {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Changing password...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null || user.getEmail() == null) {
                progressDialog.dismiss();
                Toast.makeText(this, "No user is signed in or email not found", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!user.isEmailVerified()) {
                if (!isNetworkAvailable()) {
                    progressDialog.dismiss();
                    Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressDialog.setMessage("Sending verification email...");
                user.sendEmailVerification()
                        .addOnCompleteListener(task -> {
                            progressDialog.dismiss();
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Verification email sent. Please verify and try again.", Toast.LENGTH_LONG).show();
                            } else {
                                String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                                Toast.makeText(this, "Failed to send verification email: " + errorMsg, Toast.LENGTH_LONG).show();
                            }
                        });
                return;
            }
            String currentPassword = currentPasswordEditText.getText().toString().trim();
            String newPassword = newPasswordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();
            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                progressDialog.dismiss();
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                progressDialog.dismiss();
                Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPassword.length() < 6) {
                progressDialog.dismiss();
                Toast.makeText(this, "New password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isNetworkAvailable()) {
                progressDialog.dismiss();
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
                return;
            }
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
            user.reauthenticate(credential)
                    .addOnSuccessListener(aVoid -> {
                        user.updatePassword(newPassword)
                                .addOnSuccessListener(aVoid1 -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(this, "Failed to update password: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Incorrect current password", Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in changePassword: " + e.getMessage(), e);
            Toast.makeText(this, "Password change error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}