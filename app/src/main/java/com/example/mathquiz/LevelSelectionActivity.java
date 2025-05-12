package com.example.mathquiz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LevelSelectionActivity extends AppCompatActivity {

    private static final int TOTAL_LEVELS = 10;
    private GridLayout levelGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_selection);

        levelGrid = findViewById(R.id.levelGrid);
        SharedPreferences prefs = getSharedPreferences("quizPrefs", Context.MODE_PRIVATE);

        for (int i = 1; i <= TOTAL_LEVELS; i++) {
            LinearLayout levelItem = new LinearLayout(this);
            levelItem.setOrientation(LinearLayout.VERTICAL);
            levelItem.setGravity(LinearLayout.CENTER);
            levelItem.setPadding(16, 16, 16, 16);
            levelItem.setBackgroundResource(R.drawable.level_item_background);

            ImageView icon = new ImageView(this);
            boolean unlocked = prefs.getInt("level" + i + "_passes", 0) >= 5 || i == 1;
            icon.setImageResource(unlocked ? R.drawable.ic_unlocked : R.drawable.ic_locked);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(100, 100);
            iconParams.gravity = android.view.Gravity.CENTER;
            icon.setLayoutParams(iconParams);

            TextView label = new TextView(this);
            label.setText("Level " + i);
            label.setTextColor(getResources().getColor(android.R.color.white));
            label.setTextSize(16);
            label.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            labelParams.topMargin = 8;
            label.setLayoutParams(labelParams);

            levelItem.addView(icon);
            levelItem.addView(label);

            final int levelId = i;
            levelItem.setOnClickListener(v -> {
                if (unlocked) {
                    Intent intent = new Intent(LevelSelectionActivity.this, MainActivity.class);
                    intent.putExtra("level", levelId);
                    startActivity(intent);
                } else {
                    int passes = prefs.getInt("level" + (levelId - 1) + "_passes", 0);
                    int remaining = Math.max(0, 5 - passes);
                    String msg = "You need " + remaining + " more high-score completions on Level " + (levelId - 1) + " to unlock this level.";
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                }
            });

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED);
            levelItem.setLayoutParams(params);

            levelGrid.addView(levelItem);
        }
    }
}