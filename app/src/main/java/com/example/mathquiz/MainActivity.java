package com.example.mathquiz;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TextView questionProgressText, questionText, resultText, splashText, gradeText;
    private RadioGroup answerOptions;
    private RadioButton option1, option2, option3, option4;
    private Button submitAnswerBtn, startBtn, homeBtn;
    private ImageButton accountBtn;
    private ProgressBar timerProgress, splashProgress;
    private LinearLayout splashLayout, quizLayout, resultLayout;
    private CountDownTimer questionTimer;
    private ImageView gradeEmoji, splashIcon;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String[] questions = {
            "What is the sum of 130 + 125 + 191?",
            "If we minus 712 from 1500, how much do we get?",
            "50 times of 8 is equal to?",
            "110 divided by 10 is?",
            "20 + (90 ÷ 2) is equal to?",
            "The product of 82 and 5 is?",
            "Find the missing terms in multiple of 3: 3, 6, 9, __, 15",
            "Solve 24 ÷ 8 + 2.",
            "Solve: 300 – (150 × 2)",
            "The product of 121 × 0 × 200 × 25 is",
            "What is the next prime number after 5?"
    };
    private String[][] options = {
            {"446", "500", "400", "600"},
            {"788", "700", "1000", "600"},
            {"400", "500", "450", "350"},
            {"11", "12", "10", "9"},
            {"65", "60", "80", "100"},
            {"410", "400", "500", "300"},
            {"12", "10", "15", "14"},
            {"5", "7", "3", "2"},
            {"0", "150", "100", "200"},
            {"0", "250", "500", "1000"},
            {"7", "9", "11", "13"}
    };
    private String[] correctAnswersText = {
            "446", "788", "400", "11", "65", "410", "12", "5", "0", "0", "7"
    };
    private int currentQuestion = -1;
    private int correctCount = 0;
    private int incorrectCount = 0;
    private String currentCorrectAnswer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);
            mAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();
            splashLayout = findViewById(R.id.splashLayout);
            quizLayout = findViewById(R.id.quizLayout);
            resultLayout = findViewById(R.id.resultLayout);
            questionProgressText = findViewById(R.id.questionProgressText);
            questionText = findViewById(R.id.questionText);
            answerOptions = findViewById(R.id.answerOptions);
            option1 = findViewById(R.id.option1);
            option2 = findViewById(R.id.option2);
            option3 = findViewById(R.id.option3);
            option4 = findViewById(R.id.option4);
            submitAnswerBtn = findViewById(R.id.submitAnswerBtn);
            startBtn = findViewById(R.id.startBtn);
            homeBtn = findViewById(R.id.homeBtn);
            accountBtn = findViewById(R.id.accountBtn);
            resultText = findViewById(R.id.resultText);
            splashText = findViewById(R.id.splashText);
            timerProgress = findViewById(R.id.timerProgress);
            splashProgress = findViewById(R.id.splashProgress);
            gradeText = findViewById(R.id.gradeText);
            gradeEmoji = findViewById(R.id.gradeEmoji);
            splashIcon = findViewById(R.id.splashIcon);

            if (splashLayout == null || quizLayout == null || resultLayout == null ||
                    questionProgressText == null || questionText == null || answerOptions == null ||
                    option1 == null || option2 == null || option3 == null || option4 == null ||
                    submitAnswerBtn == null || startBtn == null || homeBtn == null || accountBtn == null ||
                    resultText == null || splashText == null || timerProgress == null ||
                    splashProgress == null || gradeText == null || gradeEmoji == null || splashIcon == null) {
                Log.e(TAG, "UI initialization failed");
                Toast.makeText(this, "UI initialization failed", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            // Set round white background with press effect for accountBtn
            GradientDrawable normal = new GradientDrawable();
            normal.setShape(GradientDrawable.OVAL);
            normal.setColor(Color.WHITE);
            GradientDrawable pressed = new GradientDrawable();
            pressed.setShape(GradientDrawable.OVAL);
            pressed.setColor(Color.LTGRAY);
            StateListDrawable states = new StateListDrawable();
            states.addState(new int[]{android.R.attr.state_pressed}, pressed);
            states.addState(new int[]{}, normal);
            accountBtn.setBackground(states);

            startBtn.setOnClickListener(v -> startQuiz());
            submitAnswerBtn.setOnClickListener(v -> submitAnswer());
            homeBtn.setOnClickListener(v -> returnToStart());
            accountBtn.setOnClickListener(v -> {
                Log.d(TAG, "Account button clicked");
                if (mAuth.getCurrentUser() == null) {
                    Log.w(TAG, "No user logged in, redirecting to RegisterActivity");
                    Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    Toast.makeText(this, "Please register to access account", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "User logged in: " + mAuth.getCurrentUser().getEmail() + ", starting AccountActivity");
                    Intent intent = new Intent(MainActivity.this, AccountActivity.class);
                    intent.putExtra("email", mAuth.getCurrentUser().getEmail());
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to start AccountActivity: " + e.getMessage(), e);
                        Toast.makeText(this, "Failed to open account: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });

            splashProgress.setMax(100);
            new CountDownTimer(3000, 30) {
                public void onTick(long millisUntilFinished) {
                    splashProgress.setProgress((int) ((3000 - millisUntilFinished) / 30));
                }

                public void onFinish() {
                    if (mAuth.getCurrentUser() == null) {
                        Log.w(TAG, "No user logged in after splash, redirecting to RegisterActivity");
                        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.d(TAG, "User logged in after splash: " + mAuth.getCurrentUser().getEmail());
                        splashLayout.setVisibility(View.GONE);
                        questionText.setVisibility(View.VISIBLE);
                        startBtn.setVisibility(View.VISIBLE);
                        resultText.setVisibility(View.VISIBLE);
                        accountBtn.setVisibility(View.VISIBLE);
                    }
                }
            }.start();

            String email = getIntent().getStringExtra("email");
            if (email != null) {
                Toast.makeText(this, "Welcome, " + email, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage());
            Toast.makeText(this, "Initialization failed", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void startQuiz() {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < questions.length; i++) {
            indices.add(i);
        }
        Collections.shuffle(indices);
        String[] shuffledQuestions = new String[questions.length];
        String[][] shuffledOptions = new String[options.length][];
        String[] shuffledCorrectAnswersText = new String[correctAnswersText.length];
        for (int i = 0; i < indices.size(); i++) {
            int index = indices.get(i);
            shuffledQuestions[i] = questions[index];
            shuffledOptions[i] = options[index];
            shuffledCorrectAnswersText[i] = correctAnswersText[index];
        }
        questions = shuffledQuestions;
        options = shuffledOptions;
        correctAnswersText = shuffledCorrectAnswersText;
        currentQuestion = 0;
        correctCount = 0;
        incorrectCount = 0;
        resultText.setText("");
        gradeText.setText("");
        gradeEmoji.setVisibility(View.GONE);
        splashLayout.setVisibility(View.GONE);
        startBtn.setVisibility(View.GONE);
        homeBtn.setVisibility(View.GONE);
        accountBtn.setVisibility(View.VISIBLE);
        quizLayout.setVisibility(View.VISIBLE);
        questionProgressText.setVisibility(View.VISIBLE);
        timerProgress.setVisibility(View.VISIBLE);
        questionText.setVisibility(View.VISIBLE);
        answerOptions.setVisibility(View.VISIBLE);
        submitAnswerBtn.setVisibility(View.VISIBLE);
        updateQuestionProgress();
        showNextQuestion();
    }

    private void showNextQuestion() {
        if (currentQuestion < questions.length) {
            questionText.setText(questions[currentQuestion]);
            updateQuestionProgress();
            List<String> currentOptionsList = new ArrayList<>();
            Collections.addAll(currentOptionsList, options[currentQuestion]);
            Collections.shuffle(currentOptionsList);
            option1.setText(currentOptionsList.get(0));
            option2.setText(currentOptionsList.get(1));
            option3.setText(currentOptionsList.get(2));
            option4.setText(currentOptionsList.get(3));
            currentCorrectAnswer = correctAnswersText[currentQuestion];
            try {
                option1.setBackgroundColor(Color.WHITE);
                option2.setBackgroundColor(Color.WHITE);
                option3.setBackgroundColor(Color.WHITE);
                option4.setBackgroundColor(Color.WHITE);
            } catch (Exception e) {
                Log.e(TAG, "Error setting radio button background: " + e.getMessage());
            }
            answerOptions.clearCheck();
            startQuestionTimer();
        } else {
            showResults();
        }
    }

    private void startQuestionTimer() {
        if (questionTimer != null) {
            questionTimer.cancel();
        }
        timerProgress.setMax(100);
        questionTimer = new CountDownTimer(15000, 150) {
            public void onTick(long millisUntilFinished) {
                timerProgress.setProgress((int) ((15000 - millisUntilFinished) / 150));
            }

            public void onFinish() {
                incorrectCount++;
                resultText.setText("Time's up! Correct answer: " + currentCorrectAnswer);
                highlightCorrectAnswer();
                currentQuestion++;
                new Handler(Looper.getMainLooper()).postDelayed(() -> showNextQuestion(), 2000);
            }
        }.start();
    }

    private void submitAnswer() {
        if (answerOptions == null) return;
        int selectedId = answerOptions.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
            return;
        }
        if (questionTimer != null) {
            questionTimer.cancel();
        }
        RadioButton selectedOption = findViewById(selectedId);
        if (selectedOption == null) return;
        String selectedAnswerText = selectedOption.getText().toString();
        if (selectedAnswerText.equals(currentCorrectAnswer)) {
            correctCount++;
            resultText.setText("Correct!");
            try {
                selectedOption.setBackgroundColor(Color.GREEN);
            } catch (Exception e) {
                Log.e(TAG, "Error setting correct background: " + e.getMessage());
            }
        } else {
            incorrectCount++;
            resultText.setText("Incorrect! Correct answer: " + currentCorrectAnswer);
            try {
                selectedOption.setBackgroundColor(Color.RED);
            } catch (Exception e) {
                Log.e(TAG, "Error setting incorrect background: " + e.getMessage());
            }
            highlightCorrectAnswer();
        }
        currentQuestion++;
        new Handler(Looper.getMainLooper()).postDelayed(() -> showNextQuestion(), 2000);
    }

    private void highlightCorrectAnswer() {
        for (RadioButton option : new RadioButton[]{option1, option2, option3, option4}) {
            if (option != null && option.getText().toString().equals(currentCorrectAnswer)) {
                try {
                    option.setBackgroundColor(Color.GREEN);
                } catch (Exception e) {
                    Log.e(TAG, "Error highlighting correct answer: " + e.getMessage());
                }
            }
        }
    }

    private void showResults() {
        quizLayout.setVisibility(View.GONE);
        resultLayout.setVisibility(View.VISIBLE);
        resultText.setText("Quiz Completed! Correct: " + correctCount + ", Incorrect: " + incorrectCount);
        try {
            if (correctCount <= 3) {
                gradeText.setText("Bad");
                gradeEmoji.setImageResource(R.drawable.bad);
            } else if (correctCount <= 6) {
                gradeText.setText("Normal");
                gradeEmoji.setImageResource(R.drawable.good);
            } else if (correctCount <= 10) {
                gradeText.setText("Good");
                gradeEmoji.setImageResource(R.drawable.norm);
            } else {
                gradeText.setText("Excellent");
                gradeEmoji.setImageResource(R.drawable.excellent);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting grade: " + e.getMessage());
            gradeText.setText("Results");
        }
        gradeText.setVisibility(View.VISIBLE);
        gradeEmoji.setVisibility(View.VISIBLE);
        homeBtn.setVisibility(View.VISIBLE);
        accountBtn.setVisibility(View.VISIBLE);
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            Map<String, Object> scoreData = new HashMap<>();
            scoreData.put("correct", correctCount);
            scoreData.put("incorrect", incorrectCount);
            scoreData.put("timestamp", System.currentTimeMillis());
            db.collection("users").document(userId).collection("scores")
                    .add(scoreData)
                    .addOnSuccessListener(documentReference -> Toast.makeText(this, "Score saved", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to save score", Toast.LENGTH_SHORT).show());
        }
    }

    private void returnToStart() {
        currentQuestion = -1;
        correctCount = 0;
        incorrectCount = 0;
        splashLayout.setVisibility(View.GONE);
        quizLayout.setVisibility(View.GONE);
        resultLayout.setVisibility(View.GONE);
        questionText.setText("");
        questionText.setVisibility(View.VISIBLE);
        startBtn.setVisibility(View.VISIBLE);
        homeBtn.setVisibility(View.GONE);
        accountBtn.setVisibility(View.VISIBLE);
        resultText.setText("");
        resultText.setVisibility(View.VISIBLE);
        gradeText.setVisibility(View.GONE);
        gradeEmoji.setVisibility(View.GONE);
        submitAnswerBtn.setVisibility(View.GONE);
        answerOptions.setVisibility(View.GONE);
        timerProgress.setVisibility(View.GONE);
        questionProgressText.setVisibility(View.GONE);
    }

    private void updateQuestionProgress() {
        questionProgressText.setText((currentQuestion + 1) + "/" + questions.length);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (questionTimer != null) {
            questionTimer.cancel();
        }
    }
}