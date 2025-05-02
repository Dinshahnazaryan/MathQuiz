package com.example.mathquiz;

import android.annotation.SuppressLint;
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
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.firestore.FirebaseFirestore;

public class VerifyCodeActivity extends AppCompatActivity {
    private static final String TAG = "VerifyCodeActivity";
    private FirebaseFirestore db;
    private EditText codeInput;
    private Button verifyButton, cancelButton;
    private TextView resendCodeText;
    private ProgressBar progressBar;
    private String email;

    @SuppressLint({"StringFormatInvalid", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_verify_code);
            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setTitle("");
                }
            }
            db = FirebaseFirestore.getInstance();
            codeInput = findViewById(R.id.codeInput);
            verifyButton = findViewById(R.id.verifyButton);
            cancelButton = findViewById(R.id.cancelButton);
            resendCodeText = findViewById(R.id.resendCodeText);
            progressBar = findViewById(R.id.progressBar);

            if (codeInput == null || verifyButton == null || cancelButton == null ||
                    resendCodeText == null || progressBar == null) {
                Log.e(TAG, "UI elements missing: codeInput=" + codeInput +
                        ", verifyButton=" + verifyButton + ", cancelButton=" + cancelButton +
                        ", resendCodeText=" + resendCodeText + ", progressBar=" + progressBar);
                Toast.makeText(this, R.string.ui_init_failed, Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            email = getIntent().getStringExtra("email");
            if (email == null || email.isEmpty()) {
                Log.e(TAG, "Email not provided in Intent");
                Toast.makeText(this, R.string.invalid_email, Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            verifyButton.setOnClickListener(v -> verifyCode());
            cancelButton.setOnClickListener(v -> finish());
            resendCodeText.setOnClickListener(v -> {
                Intent intent = new Intent(VerifyCodeActivity.this, ForgotPasswordActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
                finish();
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, getString(R.string.app_init_failed, e.getMessage()), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @SuppressLint("StringFormatInvalid")
    private void verifyCode() {
        try {
            progressBar.setVisibility(View.VISIBLE);
            verifyButton.setEnabled(false);
            cancelButton.setEnabled(false);
            resendCodeText.setEnabled(false);

            String code = codeInput.getText().toString().trim();
            if (!code.matches("\\d{6}")) {
                progressBar.setVisibility(View.GONE);
                verifyButton.setEnabled(true);
                cancelButton.setEnabled(true);
                resendCodeText.setEnabled(true);
                Toast.makeText(this, R.string.invalid_code, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isNetworkAvailable()) {
                progressBar.setVisibility(View.GONE);
                verifyButton.setEnabled(true);
                cancelButton.setEnabled(true);
                resendCodeText.setEnabled(true);
                Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
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
                                resendCodeText.setEnabled(true);
                                Toast.makeText(this, R.string.invalid_code_data, Toast.LENGTH_LONG).show();
                                return;
                            }
                            if (System.currentTimeMillis() > expiryTime) {
                                progressBar.setVisibility(View.GONE);
                                verifyButton.setEnabled(true);
                                cancelButton.setEnabled(true);
                                resendCodeText.setEnabled(true);
                                Toast.makeText(this, R.string.code_expired, Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(VerifyCodeActivity.this, ForgotPasswordActivity.class);
                                intent.putExtra("email", email);
                                startActivity(intent);
                                finish();
                                return;
                            }
                            if (code.equals(storedCode)) {
                                db.collection("reset_codes").document(email).delete();
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(this, R.string.code_verified, Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(VerifyCodeActivity.this, ResetPasswordActivity.class);
                                intent.putExtra("email", email);
                                startActivity(intent);
                                finish();
                            } else {
                                progressBar.setVisibility(View.GONE);
                                verifyButton.setEnabled(true);
                                cancelButton.setEnabled(true);
                                resendCodeText.setEnabled(true);
                                Toast.makeText(this, R.string.incorrect_code, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            progressBar.setVisibility(View.GONE);
                            verifyButton.setEnabled(true);
                            cancelButton.setEnabled(true);
                            resendCodeText.setEnabled(true);
                            Toast.makeText(this, R.string.no_code_found, Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        verifyButton.setEnabled(true);
                        cancelButton.setEnabled(true);
                        resendCodeText.setEnabled(true);
                        Log.e(TAG, "Error verifying code: " + e.getMessage(), e);
                        Toast.makeText(this, getString(R.string.code_verify_failed, e.getMessage()), Toast.LENGTH_LONG).show();
                    });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            verifyButton.setEnabled(true);
            cancelButton.setEnabled(true);
            resendCodeText.setEnabled(true);
            Log.e(TAG, "Error in verifyCode: " + e.getMessage(), e);
            Toast.makeText(this, getString(R.string.app_error, e.getMessage()), Toast.LENGTH_LONG).show();
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