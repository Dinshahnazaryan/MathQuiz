package com.example.mathquiz;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class PasswordOptionsActivity extends AppCompatActivity {
    private static final String TAG = "PasswordOptionsActivity";
    private ImageButton changePasswordNavButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_password_options);
            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setTitle("");
                }
            }
            changePasswordNavButton = findViewById(R.id.changePasswordNavButton);
            if (changePasswordNavButton == null) {
                Log.e(TAG, "UI element missing: changePasswordNavButton=" + changePasswordNavButton);
                Toast.makeText(this, R.string.ui_init_failed, Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            changePasswordNavButton.setOnClickListener(v -> {
                Intent intent = new Intent(PasswordOptionsActivity.this, ChangePasswordActivity.class);
                startActivity(intent);
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, getString(R.string.app_init_failed, e.getMessage()), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}