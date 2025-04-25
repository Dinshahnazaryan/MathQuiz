package com.example.mathquiz;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
    private LinearLayout splashLayout;
    private CountDownTimer questionTimer;
    private ImageView gradeEmoji;
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

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        Log.d(TAG, "onCreate: Checking user authentication status");
        if (mAuth.getCurrentUser() == null) {
            Log.d(TAG, "No user signed in, navigating to LoginActivity");
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        } else {
            Log.d(TAG, "User signed in: " + mAuth.getCurrentUser().getEmail());
        }
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
        splashLayout = findViewById(R.id.splashLayout);
        gradeText = findViewById(R.id.gradeText);
        gradeEmoji = findViewById(R.id.gradeEmoji);
        if (questionProgressText == null || questionText == null || answerOptions == null ||
                option1 == null || option2 == null || option3 == null || option4 == null ||
                submitAnswerBtn == null || startBtn == null || homeBtn == null || accountBtn == null ||
                resultText == null || splashText == null || timerProgress == null ||
                splashProgress == null || splashLayout == null || gradeText == null ||
                gradeEmoji == null) {
            Log.e(TAG, "One or more UI elements not found");
            Toast.makeText(this, "UI initialization failed", Toast.LENGTH_LONG).show();
            return;
        }
        startBtn.setOnClickListener(v -> startQuiz());
        submitAnswerBtn.setOnClickListener(v -> submitAnswer());
        homeBtn.setOnClickListener(v -> returnToStart());
        accountBtn.setOnClickListener(v -> {
            Log.d(TAG, "Account button clicked, visibility: " + (accountBtn.getVisibility() == View.VISIBLE ? "VISIBLE" : "GONE"));
            if (mAuth.getCurrentUser() == null) {
                Log.d(TAG, "No user signed in, navigating to LoginActivity");
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                Toast.makeText(this, "Please sign in to access account", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "User signed in, navigating to AccountActivity");
                Intent intent = new Intent(MainActivity.this, AccountActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        splashProgress.setMax(100);
        new CountDownTimer(3000, 30) {
            public void onTick(long millisUntilFinished) {
                int progress = (int) ((3000 - millisUntilFinished) / 30);
                if (splashProgress != null) splashProgress.setProgress(progress);
            }

            public void onFinish() {
                Log.d(TAG, "Splash screen finished, updating UI visibility");
                if (splashLayout != null) splashLayout.setVisibility(View.GONE);
                if (questionText != null) questionText.setVisibility(View.VISIBLE);
                if (startBtn != null) startBtn.setVisibility(View.VISIBLE);
                if (resultText != null) resultText.setVisibility(View.VISIBLE);
                if (accountBtn != null) {
                    accountBtn.setVisibility(View.VISIBLE);
                    Log.d(TAG, "Account button set to VISIBLE after splash");
                }
            }
        }.start();
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
        if (resultText != null) resultText.setText("");
        if (gradeText != null) gradeText.setText("");
        if (gradeEmoji != null) gradeEmoji.setVisibility(View.GONE);
        if (startBtn != null) startBtn.setVisibility(View.GONE);
        if (homeBtn != null) homeBtn.setVisibility(View.GONE);
        if (accountBtn != null) accountBtn.setVisibility(View.GONE);
        if (answerOptions != null) answerOptions.setVisibility(View.VISIBLE);
        if (submitAnswerBtn != null) submitAnswerBtn.setVisibility(View.VISIBLE);
        if (timerProgress != null) timerProgress.setVisibility(View.VISIBLE);
        if (questionProgressText != null) questionProgressText.setVisibility(View.VISIBLE);
        updateQuestionProgress();
        showNextQuestion();
    }

    private void showNextQuestion() {
        if (currentQuestion < questions.length) {
            if (questionText != null) questionText.setText(questions[currentQuestion]);
            updateQuestionProgress();
            List<String> currentOptionsList = new ArrayList<>();
            Collections.addAll(currentOptionsList, options[currentQuestion]);
            Collections.shuffle(currentOptionsList);
            if (option1 != null) option1.setText(currentOptionsList.get(0));
            if (option2 != null) option2.setText(currentOptionsList.get(1));
            if (option3 != null) option3.setText(currentOptionsList.get(2));
            if (option4 != null) option4.setText(currentOptionsList.get(3));
            currentCorrectAnswer = correctAnswersText[currentQuestion];
            if (option1 != null) option1.setBackgroundResource(R.drawable.radiobutton_selector);
            if (option2 != null) option2.setBackgroundResource(R.drawable.radiobutton_selector);
            if (option3 != null) option3.setBackgroundResource(R.drawable.radiobutton_selector);
            if (option4 != null) option4.setBackgroundResource(R.drawable.radiobutton_selector);
            if (answerOptions != null) answerOptions.clearCheck();
            startQuestionTimer();
        } else {
            showResults();
        }
    }

    private void startQuestionTimer() {
        if (questionTimer != null) {
            questionTimer.cancel();
        }
        if (timerProgress != null) timerProgress.setMax(100);
        questionTimer = new CountDownTimer(15000, 150) {
            public void onTick(long millisUntilFinished) {
                int progress = (int) ((15000 - millisUntilFinished) / 150);
                if (timerProgress != null) timerProgress.setProgress(progress);
            }

            public void onFinish() {
                incorrectCount++;
                if (resultText != null) resultText.setText("Time's up! Correct answer: " + currentCorrectAnswer);
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
            if (resultText != null) resultText.setText("Correct!");
            selectedOption.setBackgroundColor(getResources().getColor(R.color.green));
        } else {
            incorrectCount++;
            if (resultText != null) resultText.setText("Incorrect! Correct answer: " + currentCorrectAnswer);
            selectedOption.setBackgroundColor(getResources().getColor(R.color.red));
            highlightCorrectAnswer();
        }
        currentQuestion++;
        new Handler(Looper.getMainLooper()).postDelayed(() -> showNextQuestion(), 2000);
    }

    private void highlightCorrectAnswer() {
        for (RadioButton option : new RadioButton[]{option1, option2, option3, option4}) {
            if (option != null && option.getText().toString().equals(currentCorrectAnswer)) {
                option.setBackgroundColor(getResources().getColor(R.color.green));
            }
        }
    }

    private void showResults() {
        if (resultText != null) {
            resultText.setText("Quiz Completed! Correct: " + correctCount + ", Incorrect: " + incorrectCount);
        }
        if (gradeText != null && gradeEmoji != null) {
            if (correctCount <= 3) {
                gradeText.setText("Bad");
                gradeEmoji.setImageResource(R.drawable.bad);
            } else if (correctCount <= 6) {
                gradeText.setText("Normal");
                gradeEmoji.setImageResource(R.drawable.norm);
            } else if (correctCount <= 10) {
                gradeText.setText("Good");
                gradeEmoji.setImageResource(R.drawable.good);
            } else {
                gradeText.setText("Excellent");
                gradeEmoji.setImageResource(R.drawable.excellent);
            }
        }
        if (gradeText != null) gradeText.setVisibility(View.VISIBLE);
        if (gradeEmoji != null) gradeEmoji.setVisibility(View.VISIBLE);
        if (submitAnswerBtn != null) submitAnswerBtn.setVisibility(View.GONE);
        if (answerOptions != null) answerOptions.setVisibility(View.GONE);
        if (timerProgress != null) timerProgress.setVisibility(View.GONE);
        if (questionProgressText != null) questionProgressText.setVisibility(View.GONE);
        if (questionText != null) questionText.setVisibility(View.GONE);
        if (homeBtn != null) homeBtn.setVisibility(View.VISIBLE);
        if (accountBtn != null) {
            accountBtn.setVisibility(View.VISIBLE);
            Log.d(TAG, "Account button set to VISIBLE in showResults");
        }
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            Map<String, Object> scoreData = new HashMap<>();
            scoreData.put("correct", correctCount);
            scoreData.put("incorrect", incorrectCount);
            scoreData.put("timestamp", System.currentTimeMillis());
            db.collection("users").document(userId).collection("scores")
                    .add(scoreData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Score saved", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to save score: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void returnToStart() {
        mAuth.signOut();
        Log.d(TAG, "Home button clicked, signing out and navigating to LoginActivity");
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void updateQuestionProgress() {
        if (questionProgressText != null) {
            questionProgressText.setText((currentQuestion + 1) + "/" + questions.length);
        }
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