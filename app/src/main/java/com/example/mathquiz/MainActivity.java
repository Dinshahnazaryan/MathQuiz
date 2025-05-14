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

        initViews();
        showSplashScreen();

        startBtn.setOnClickListener(v -> startQuiz());
        submitAnswerBtn.setOnClickListener(v -> submitAnswer());
        homeBtn.setOnClickListener(v -> returnToStart());
        accountBtn.setOnClickListener(v -> startActivity(new Intent(this, AccountActivity.class)));
        levelsBtn.setOnClickListener(v -> startActivity(new Intent(this, LevelSelectionActivity.class)));
        learnTopicsBtn.setOnClickListener(v -> startActivity(new Intent(this, TopicExplanationActivity.class)));

        answerOptions.setOnCheckedChangeListener((group, checkedId) -> {
            submitAnswerBtn.setEnabled(checkedId != -1);
            highlightSelectedOption(checkedId);
        });
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
        learnTopicsBtn = findViewById(R.id.learnTopicsBtn);
        resultText = findViewById(R.id.resultText);
        gradeText = findViewById(R.id.gradeText);
        timerProgress = findViewById(R.id.timerProgress);
    }

    private void showSplashScreen() {
        splashLayout.setVisibility(View.VISIBLE);
        titleLayout.setVisibility(View.GONE);
        startBtn.setVisibility(View.GONE);
        learnTopicsBtn.setVisibility(View.GONE);
        quizLayout.setVisibility(View.GONE);
        resultLayout.setVisibility(View.GONE);

        new CountDownTimer(3000, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                splashProgress.setProgress((int) ((3000 - millisUntilFinished) / 30));
            }
            @Override
            public void onFinish() {
                splashLayout.setVisibility(View.GONE);
                titleLayout.setVisibility(View.VISIBLE);
                startBtn.setVisibility(View.VISIBLE);
                learnTopicsBtn.setVisibility(View.VISIBLE);
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
                        quizItems.add(new QuizItem(question, options.toArray(new String[0]), answer));
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

            quizLayout.setVisibility(View.VISIBLE);
            titleLayout.setVisibility(View.GONE);
            startBtn.setVisibility(View.GONE);
            learnTopicsBtn.setVisibility(View.GONE);
            showNextQuestion();
        });
    }

    private void showNextQuestion() {
        if (currentQuestion >= questionOrder.size()) {
            showResults();
            return;
        }
        answerOptions.clearCheck();
        resetOptionColors();
        submitAnswerBtn.setEnabled(false);
        QuizItem item = quizItems.get(questionOrder.get(currentQuestion));
        questionText.setText(item.question);
        questionProgressText.setText((currentQuestion + 1) + "/" + questionOrder.size());
        currentCorrectAnswer = item.correctAnswer;
        List<String> opts = new ArrayList<>(Arrays.asList(item.options));
        Collections.shuffle(opts);
        option1.setText(opts.get(0));
        option2.setText(opts.get(1));
        option3.setText(opts.get(2));
        option4.setText(opts.get(3));
        startQuestionTimer();
    }

    private void startQuestionTimer() {
        if (questionTimer != null) questionTimer.cancel();
        timerProgress.setMax(100);
        timerProgress.setProgress(100);
        remainingTime = 15000;
        questionTimer = new CountDownTimer(15000, 100) {
            @Override
            public void onTick(long ms) {
                remainingTime = ms;
                timerProgress.setProgress((int) (ms / 150));
            }
            @Override
            public void onFinish() {
                incorrectCount++;
                resultText.setText("Time's up! Correct: " + currentCorrectAnswer);
                currentQuestion++;
                new Handler(Looper.getMainLooper()).postDelayed(() -> showNextQuestion(), 2000);
            }
        }.start();
    }

    private void submitAnswer() {
        int selectedId = answerOptions.getCheckedRadioButtonId();
        if (selectedId == -1) return;
        if (questionTimer != null) questionTimer.cancel();
        RadioButton selectedOption = findViewById(selectedId);
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
            selectedOption.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
        }
    }

    private void resetOptionColors() {
        option1.setBackgroundColor(getResources().getColor(android.R.color.white));
        option2.setBackgroundColor(getResources().getColor(android.R.color.white));
        option3.setBackgroundColor(getResources().getColor(android.R.color.white));
        option4.setBackgroundColor(getResources().getColor(android.R.color.white));
    }

    private void highlightCorrectAnswer() {
        if (option1.getText().toString().equals(currentCorrectAnswer)) {
            option1.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        } else if (option2.getText().toString().equals(currentCorrectAnswer)) {
            option2.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        } else if (option3.getText().toString().equals(currentCorrectAnswer)) {
            option3.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        } else if (option4.getText().toString().equals(currentCorrectAnswer)) {
            option4.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        }
    }

    private void showResults() {
        quizLayout.setVisibility(View.GONE);
        resultLayout.setVisibility(View.VISIBLE);
        int percentage = (correctCount * 100) / questionOrder.size();
        gradeText.setText(percentage + "%\nCorrect: " + correctCount + "\nIncorrect: " + incorrectCount);

        ImageView gradeEmoji = findViewById(R.id.gradeEmoji);
        if (correctCount <= 3) {
            gradeEmoji.setImageResource(R.drawable.bad);
        } else if (correctCount <= 6) {
            gradeEmoji.setImageResource(R.drawable.norm);
        } else if (correctCount <= 9) {
            gradeEmoji.setImageResource(R.drawable.good);
        } else {
            gradeEmoji.setImageResource(R.drawable.excellent);
        }

        int level = getIntent().getIntExtra("level", 1);
        SharedPreferences prefs = getSharedPreferences("quizPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        if (percentage >= 80) {
            int passes = prefs.getInt("level" + level + "_passes", 0);
            if (passes < 3) {
                editor.putInt("level" + level + "_passes", passes + 1);
                editor.apply();
            }
        }
    }

    private void returnToStart() {
        if (questionTimer != null) questionTimer.cancel();
        currentQuestion = -1;
        quizLayout.setVisibility(View.GONE);
        resultLayout.setVisibility(View.GONE);
        titleLayout.setVisibility(View.VISIBLE);
        startBtn.setVisibility(View.VISIBLE);
        learnTopicsBtn.setVisibility(View.VISIBLE);
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