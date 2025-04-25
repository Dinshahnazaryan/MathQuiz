package com.example.mathquiz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class PasswordOptionsActivity extends AppCompatActivity {

    private ImageButton changePasswordNavButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_options);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        changePasswordNavButton = findViewById(R.id.changePasswordNavButton);
        changePasswordNavButton.setOnClickListener(v -> {
            Intent intent = new Intent(PasswordOptionsActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}