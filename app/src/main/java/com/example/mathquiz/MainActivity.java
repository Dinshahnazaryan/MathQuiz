package com.example.mathquiz;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TextView questionText, questionProgressText, resultText, gradeText;
    private RadioGroup answerOptions;
    private RadioButton option1, option2, option3, option4;
    private Button submitAnswerBtn, startBtn, homeBtn, learnTopicsBtn;
    private ImageButton accountBtn, levelsBtn;
    private ProgressBar timerProgress, splashProgress;
    private LinearLayout quizLayout, resultLayout, titleLayout, splashLayout;
    private ImageView gradeEmoji;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CountDownTimer questionTimer;

    private List<Integer> questionOrder;
    private int currentQuestion = -1;
    private int correctCount = 0;
    private int incorrectCount = 0;
    private String currentCorrectAnswer;
    private long remainingTime;

    private List<QuizItem> quizItems = new ArrayList<>();

    private static class QuizItem {
        String question;
        String[] options;
        String correctAnswer;
        QuizItem(String question, String[] options, String correctAnswer) {
            this.question = question;
            this.options = options;
            this.correctAnswer = correctAnswer;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Check authentication status
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || !currentUser.isEmailVerified()) {
            Toast.makeText(this, "Please log in to continue", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return;
        }

        initViews();
        showSplashScreen();

        // Set click listeners
        if (startBtn != null) startBtn.setOnClickListener(v -> startQuiz());
        if (submitAnswerBtn != null) submitAnswerBtn.setOnClickListener(v -> submitAnswer());
        if (homeBtn != null) homeBtn.setOnClickListener(v -> returnToStart());
        if (accountBtn != null) accountBtn.setOnClickListener(v -> startActivity(new Intent(this, AccountActivity.class)));
        if (levelsBtn != null) levelsBtn.setOnClickListener(v -> startActivity(new Intent(this, LevelSelectionActivity.class)));
        if (learnTopicsBtn != null) learnTopicsBtn.setOnClickListener(v -> startActivity(new Intent(this, TopicExplanationActivity.class)));

        if (answerOptions != null) {
            answerOptions.setOnCheckedChangeListener((group, checkedId) -> {
                if (submitAnswerBtn != null) submitAnswerBtn.setEnabled(checkedId != -1);
                highlightSelectedOption(checkedId);
            });
        }
    }

    private void initViews() {
        quizLayout = findViewById(R.id.quizLayout);
        resultLayout = findViewById(R.id.resultLayout);
        titleLayout = findViewById(R.id.titleLayout);
        splashLayout = findViewById(R.id.splashLayout);
        splashProgress = findViewById(R.id.splashProgress);
        questionText = findViewById(R.id.questionText);
        questionProgressText = findViewById(R.id.questionProgressText);
        answerOptions = findViewById(R.id.answerOptions);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);
        submitAnswerBtn = findViewById(R.id.submitAnswerBtn);
        startBtn = findViewById(R.id.startBtn);
        homeBtn = findViewById(R.id.homeBtn);
        accountBtn = findViewById(R.id.accountBtn);
        levelsBtn = findViewById(R.id.levelsBtn);

        resultText = findViewById(R.id.resultText);
        gradeText = findViewById(R.id.gradeText);
        timerProgress = findViewById(R.id.timerProgress);
        gradeEmoji = findViewById(R.id.gradeEmoji);

        // Debug logging for null views
        ;
        if (splashLayout == null) Log.e(TAG, "splashLayout is null");
        if (titleLayout == null) Log.e(TAG, "titleLayout is null");
        if (startBtn == null) Log.e(TAG, "startBtn is null");
        if (quizLayout == null) Log.e(TAG, "quizLayout is null");
        if (resultLayout == null) Log.e(TAG, "resultLayout is null");
        if (gradeEmoji == null) Log.e(TAG, "gradeEmoji is null");
    }

    private void showSplashScreen() {
        if (splashLayout != null) splashLayout.setVisibility(View.VISIBLE);
        if (titleLayout != null) titleLayout.setVisibility(View.GONE);
        if (startBtn != null) startBtn.setVisibility(View.GONE);
        if (learnTopicsBtn != null) learnTopicsBtn.setVisibility(View.GONE);
        if (quizLayout != null) quizLayout.setVisibility(View.GONE);
        if (resultLayout != null) resultLayout.setVisibility(View.GONE);

        new CountDownTimer(3000, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (splashProgress != null) {
                    splashProgress.setProgress((int) ((3000 - millisUntilFinished) / 30));
                }
            }
            @Override
            public void onFinish() {
                if (splashLayout != null) splashLayout.setVisibility(View.GONE);
                if (titleLayout != null) titleLayout.setVisibility(View.VISIBLE);
                if (startBtn != null) startBtn.setVisibility(View.VISIBLE);
                if (learnTopicsBtn != null) learnTopicsBtn.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    private void loadQuizData(int level, Runnable onSuccess) {
        db.collection("quiz_levels")
                .document("level" + level)
                .collection("questions")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    quizItems.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String question = doc.getString("question");
                        List<String> options = (List<String>) doc.get("options");
                        String answer = doc.getString("answer");
                        if (question != null && options != null && answer != null) {
                            quizItems.add(new QuizItem(question, options.toArray(new String[0]), answer));
                        }
                    }
                    onSuccess.run();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load quiz", Toast.LENGTH_SHORT).show());
    }

    private void startQuiz() {
        int level = getIntent().getIntExtra("level", 1);
        currentQuestion = 0;
        correctCount = 0;
        incorrectCount = 0;

        loadQuizData(level, () -> {
            questionOrder = new ArrayList<>();
            for (int i = 0; i < quizItems.size(); i++) questionOrder.add(i);
            Collections.shuffle(questionOrder);
            questionOrder = questionOrder.subList(0, Math.min(10, quizItems.size()));

            if (quizLayout != null) quizLayout.setVisibility(View.VISIBLE);
            if (titleLayout != null) titleLayout.setVisibility(View.GONE);
            if (startBtn != null) startBtn.setVisibility(View.GONE);
            if (learnTopicsBtn != null) learnTopicsBtn.setVisibility(View.GONE);
            showNextQuestion();
        });
    }

    private void showNextQuestion() {
        if (currentQuestion >= questionOrder.size()) {
            showResults();
            return;
        }
        if (answerOptions != null) answerOptions.clearCheck();
        resetOptionColors();
        if (submitAnswerBtn != null) submitAnswerBtn.setEnabled(false);
        QuizItem item = quizItems.get(questionOrder.get(currentQuestion));
        if (questionText != null) questionText.setText(item.question);
        if (questionProgressText != null) {
            questionProgressText.setText((currentQuestion + 1) + "/" + questionOrder.size());
        }
        currentCorrectAnswer = item.correctAnswer;
        List<String> opts = new ArrayList<>(Arrays.asList(item.options));
        Collections.shuffle(opts);
        if (option1 != null) option1.setText(opts.get(0));
        if (option2 != null) option2.setText(opts.get(1));
        if (option3 != null) option3.setText(opts.get(2));
        if (option4 != null) option4.setText(opts.get(3));
        startQuestionTimer();
    }

    private void startQuestionTimer() {
        if (questionTimer != null) questionTimer.cancel();
        if (timerProgress != null) {
            timerProgress.setMax(100);
            timerProgress.setProgress(100);
        }
        remainingTime = 15000;
        questionTimer = new CountDownTimer(15000, 100) {
            @Override
            public void onTick(long ms) {
                remainingTime = ms;
                if (timerProgress != null) timerProgress.setProgress((int) (ms / 150));
            }
            @Override
            public void onFinish() {
                incorrectCount++;
                if (resultText != null) {
                    resultText.setText("Time's up! Correct: " + currentCorrectAnswer);
                }
                currentQuestion++;
                new Handler(Looper.getMainLooper()).postDelayed(() -> showNextQuestion(), 2000);
            }
        }.start();
    }

    private void submitAnswer() {
        if (answerOptions == null) return;
        int selectedId = answerOptions.getCheckedRadioButtonId();
        if (selectedId == -1) return;
        if (questionTimer != null) questionTimer.cancel();
        RadioButton selectedOption = findViewById(selectedId);
        if (selectedOption == null) return;
        String selectedAnswer = selectedOption.getText().toString();
        resetOptionColors();
        if (selectedAnswer.equals(currentCorrectAnswer)) {
            correctCount++;
            selectedOption.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        } else {
            incorrectCount++;
            selectedOption.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            highlightCorrectAnswer();
        }
        currentQuestion++;
        new Handler(Looper.getMainLooper()).postDelayed(this::showNextQuestion, 2000);
    }

    private void highlightSelectedOption(int checkedId) {
        resetOptionColors();
        if (checkedId != -1) {
            RadioButton selectedOption = findViewById(checkedId);
            if (selectedOption != null) {
                selectedOption.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
            }
        }
    }

    private void resetOptionColors() {
        if (option1 != null) option1.setBackgroundColor(getResources().getColor(android.R.color.white));
        if (option2 != null) option2.setBackgroundColor(getResources().getColor(android.R.color.white));
        if (option3 != null) option3.setBackgroundColor(getResources().getColor(android.R.color.white));
        if (option4 != null) option4.setBackgroundColor(getResources().getColor(android.R.color.white));
    }

    private void highlightCorrectAnswer() {
        if (option1 != null && option1.getText().toString().equals(currentCorrectAnswer)) {
            option1.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        } else if (option2 != null && option2.getText().toString().equals(currentCorrectAnswer)) {
            option2.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        } else if (option3 != null && option3.getText().toString().equals(currentCorrectAnswer)) {
            option3.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        } else if (option4 != null && option4.getText().toString().equals(currentCorrectAnswer)) {
            option4.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        }
    }

    private void showResults() {
        if (quizLayout != null) quizLayout.setVisibility(View.GONE);
        if (resultLayout != null) resultLayout.setVisibility(View.VISIBLE);
        int percentage = (correctCount * 100) / questionOrder.size();
        if (gradeText != null) {
            gradeText.setText(percentage + "%\nCorrect: " + correctCount + "\nIncorrect: " + incorrectCount);
        }

        if (gradeEmoji != null) {
            if (correctCount <= 3) {
                gradeEmoji.setImageResource(R.drawable.bad);
            } else if (correctCount <= 6) {
                gradeEmoji.setImageResource(R.drawable.norm);
            } else if (correctCount <= 9) {
                gradeEmoji.setImageResource(R.drawable.good);
            } else {
                gradeEmoji.setImageResource(R.drawable.excellent);
            }
        }

        int level = getIntent().getIntExtra("level", 1);
        SharedPreferences prefs = getSharedPreferences("quizPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        if (percentage >= 80) {
            int passes = prefs.getInt("level" + level + "_passes", 0);
            if (passes < 3) {
            }
        }
    }

    private void returnToStart() {
        if (questionTimer != null) questionTimer.cancel();
        currentQuestion = -1;
        if (quizLayout != null) quizLayout.setVisibility(View.GONE);
        if (resultLayout != null) resultLayout.setVisibility(View.GONE);
        if (titleLayout != null) titleLayout.setVisibility(View.VISIBLE);
        if (startBtn != null) startBtn.setVisibility(View.VISIBLE);
        if (learnTopicsBtn != null) learnTopicsBtn.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPause() {
        if (questionTimer != null) questionTimer.cancel();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (questionTimer != null) questionTimer.cancel();
        super.onDestroy();
    }
}