package com.example.mathquiz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.io.InputStream;

public class LevelSelectionActivity extends AppCompatActivity {

    private static final int TOTAL_LEVELS = 10;
    private Button[] levelButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_selection);

        SharedPreferences prefs = getSharedPreferences("quizPrefs", Context.MODE_PRIVATE);
        levelButtons = new Button[TOTAL_LEVELS];

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

        for (int i = 0; i < TOTAL_LEVELS; i++) {
            final int level = i + 1;
            Button button = levelButtons[i];

            boolean unlocked = (level == 1) || (prefs.getInt("level" + (level - 1) + "_passes", 0) >= 3);

            String imageUri = prefs.getString("level" + level + "_image", null);
            if (imageUri != null) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(Uri.parse(imageUri));
                    Drawable drawable = Drawable.createFromStream(inputStream, imageUri);
                    inputStream.close();
                    if (drawable != null) {
                        button.setBackground(null);
                        drawable.setBounds(0, 0, 100, 100);
                        button.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
                    } else {
                        setDefaultDrawable(button, unlocked);
                    }
                } catch (IOException e) {
                    setDefaultDrawable(button, unlocked);
                }
            } else {
                setDefaultDrawable(button, unlocked);
            }

            button.setOnClickListener(v -> {
                if (unlocked) {
                    Intent intent = new Intent(LevelSelectionActivity.this, MainActivity.class);
                    intent.putExtra("level", level);
                    startActivity(intent);
                } else {
                    int passes = prefs.getInt("level" + (level - 1) + "_passes", 0);
                    int remaining = Math.max(0, 3 - passes);
                    String msg = "You need " + remaining + " more excellent completions on Level " + (level - 1) + " to unlock this level.";
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void setDefaultDrawable(Button button, boolean unlocked) {
        Drawable drawable = getResources().getDrawable(unlocked ? R.drawable.ic_unlocked : R.drawable.ic_locked);
        drawable.setBounds(0, 0, 60, 60);
        button.setBackgroundResource(R.drawable.level_item_background);
        button.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
    }
}
