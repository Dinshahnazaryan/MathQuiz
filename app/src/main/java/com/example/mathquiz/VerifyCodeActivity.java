package com.example.mathquiz;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.firestore.FirebaseFirestore;

public class VerifyCodeActivity extends AppCompatActivity {

    private static final String TAG = "VerifyCodeActivity";
    private FirebaseFirestore db;
    private EditText codeInput;
    private Button verifyButton, cancelButton;
    private ProgressBar progressBar;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_verify_code);
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
            db = FirebaseFirestore.getInstance();
            codeInput = findViewById(R.id.codeInput);
            verifyButton = findViewById(R.id.verifyButton);
            cancelButton = findViewById(R.id.cancelButton);
            progressBar = findViewById(R.id.progressBar);

            if (codeInput == null || verifyButton == null || cancelButton == null || progressBar == null) {
                Log.e(TAG, "One or more views are null");
                Toast.makeText(this, "UI initialization failed", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            email = getIntent().getStringExtra("email");
            if (email == null || email.isEmpty()) {
                Log.e(TAG, "Email not provided in Intent");
                Toast.makeText(this, "Invalid email", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            verifyButton.setOnClickListener(v -> verifyCode());
            cancelButton.setOnClickListener(v -> finish());
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Initialization error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void verifyCode() {
        try {
            progressBar.setVisibility(View.VISIBLE);
            verifyButton.setEnabled(false);
            cancelButton.setEnabled(false);

            String code = codeInput.getText().toString().trim();
            if (code.isEmpty() || code.length() != 6) {
                progressBar.setVisibility(View.GONE);
                verifyButton.setEnabled(true);
                cancelButton.setEnabled(true);
                Toast.makeText(this, "Please enter a valid 6-digit code", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isNetworkAvailable()) {
                progressBar.setVisibility(View.GONE);
                verifyButton.setEnabled(true);
                cancelButton.setEnabled(true);
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("reset_codes").document(email).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String storedCode = documentSnapshot.getString("code");
                            Long expiryTime = documentSnapshot.getLong("expiryTime");
                            if (storedCode == null || expiryTime == null) {
                                progressBar.setVisibility(View.GONE);
                                verifyButton.setEnabled(true);
                                cancelButton.setEnabled(true);
                                Toast.makeText(this, "Invalid code data", Toast.LENGTH_LONG).show();
                                return;
                            }
                            if (System.currentTimeMillis() > expiryTime) {
                                progressBar.setVisibility(View.GONE);
                                verifyButton.setEnabled(true);
                                cancelButton.setEnabled(true);
                                Toast.makeText(this, "Code has expired", Toast.LENGTH_LONG).show();
                                return;
                            }
                            if (code.equals(storedCode)) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(this, "Code verified", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(VerifyCodeActivity.this, ResetPasswordActivity.class);
                                intent.putExtra("email", email);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            } else {
                                progressBar.setVisibility(View.GONE);
                                verifyButton.setEnabled(true);
                                cancelButton.setEnabled(true);
                                Toast.makeText(this, "Incorrect code", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            progressBar.setVisibility(View.GONE);
                            verifyButton.setEnabled(true);
                            cancelButton.setEnabled(true);
                            Toast.makeText(this, "No code found for this email", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        verifyButton.setEnabled(true);
                        cancelButton.setEnabled(true);
                        Log.e(TAG, "Error verifying code: " + e.getMessage(), e);
                        Toast.makeText(this, "Error verifying code: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            verifyButton.setEnabled(true);
            cancelButton.setEnabled(true);
            Log.e(TAG, "Error in verifyCode: " + e.getMessage(), e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
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