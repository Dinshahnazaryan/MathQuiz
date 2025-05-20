package com.example.mathquiz;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LevelSelectionActivity extends AppCompatActivity {
    private static final String TAG = "LevelSelectionActivity";
    private FirebaseAuth mAuth;
    private Button[] levelButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_selection);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || !currentUser.isEmailVerified()) {
            Toast.makeText(this, "Please log in to continue", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return;
        }

        initializeButtons();
        setupLevelButtons();
        applyAnimations();
    }

    private void initializeButtons() {
        levelButtons = new Button[10];
        levelButtons[0] = findViewById(R.id.level1Btn);
        levelButtons[1] = findViewById(R.id.level2Btn);
        levelButtons[2] = findViewById(R.id.level3Btn);
        levelButtons[3] = findViewById(R.id.level4Btn);
        levelButtons[4] = findViewById(R.id.level5Btn);
        levelButtons[5] = findViewById(R.id.level6Btn);
        levelButtons[6] = findViewById(R.id.level7Btn);
        levelButtons[7] = findViewById(R.id.level8Btn);
        levelButtons[8] = findViewById(R.id.level9Btn);
        levelButtons[9] = findViewById(R.id.level10Btn);

        for (Button btn : levelButtons) {
            if (btn == null) {
                Log.e(TAG, "One or more level buttons not found");
                Toast.makeText(this, "UI initialization failed", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupLevelButtons() {
        SharedPreferences prefs = getSharedPreferences("quizPrefs", MODE_PRIVATE);

        for (int i = 0; i < levelButtons.length; i++) {
            final int level = i + 1;
            Button btn = levelButtons[i];
            int passes = prefs.getInt("level" + level + "_passes", 0);
            btn.setText(level + "\n" + passes + "/3");
            // Enable all levels
            btn.setEnabled(true);
            btn.setAlpha(1.0f);
            btn.setOnClickListener(v -> {
                Intent intent = new Intent(LevelSelectionActivity.this, MainActivity.class);
                intent.putExtra("level", level);
                startActivity(intent);
            });
            Log.d(TAG, "Level " + level + " enabled: " + btn.isEnabled());

            Animation scaleAnim = AnimationUtils.loadAnimation(this, R.anim.scale_button);
            btn.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.startAnimation(scaleAnim);
                }
                return false;
            });
        }
    }

    private void applyAnimations() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        for (Button btn : levelButtons) {
            btn.startAnimation(fadeIn);
        }
        findViewById(R.id.title).startAnimation(fadeIn);
    }
}