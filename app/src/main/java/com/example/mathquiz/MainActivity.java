package com.example.mathquiz;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView questionProgressText;
    private TextView questionText;
    private RadioGroup answerOptions;
    private RadioButton option1, option2, option3, option4;
    private Button submitAnswerBtn, startBtn, homeBtn;
    private TextView resultText;
    private TextView splashText;
    private ProgressBar timerProgress, splashProgress;
    private LinearLayout splashLayout;
    private CountDownTimer questionTimer;
    private TextView gradeText;
    private ImageView gradeEmoji;

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

    private int[] correctAnswers = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    private int currentQuestion = -1;
    private int correctCount = 0;
    private int incorrectCount = 0;
    private String currentCorrectAnswer;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        if (!prefs.contains("logged_in_user")) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
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
        resultText = findViewById(R.id.resultText);
        splashText = findViewById(R.id.splashText);
        timerProgress = findViewById(R.id.timerProgress);
        splashProgress = findViewById(R.id.splashProgress);
        splashLayout = findViewById(R.id.splashLayout);
        gradeText = findViewById(R.id.gradeText);
        gradeEmoji = findViewById(R.id.gradeEmoji);

        startBtn.setOnClickListener(v -> startQuiz());
        submitAnswerBtn.setOnClickListener(v -> submitAnswer());
        homeBtn.setOnClickListener(v -> returnToStart());

        splashProgress.setMax(100);
        new CountDownTimer(10000, 100) {
            public void onTick(long millisUntilFinished) {
                int progress = (int) ((10000 - millisUntilFinished) / 100);
                splashProgress.setProgress(progress);
            }
            public void onFinish() {
                splashLayout.setVisibility(View.GONE);
                questionText.setVisibility(View.VISIBLE);
                startBtn.setVisibility(View.VISIBLE);
                resultText.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    private void startQuiz() {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < questions.length; i++) {
            indices.add(i);
        }
        Collections.shuffle(indices);

        String[] shuffledQuestions = new String[questions.length];
        String[][] shuffledOptions = new String[options.length][];
        int[] shuffledCorrectAnswers = new int[correctAnswers.length];

        for (int i = 0; i < indices.size(); i++) {
            int index = indices.get(i);
            shuffledQuestions[i] = questions[index];
            shuffledOptions[i] = options[index];
            shuffledCorrectAnswers[i] = correctAnswers[index];
        }

        questions = shuffledQuestions;
        options = shuffledOptions;
        correctAnswers = shuffledCorrectAnswers;

        currentQuestion = 0;
        correctCount = 0;
        incorrectCount = 0;
        resultText.setText("");
        gradeText.setText("");
        startBtn.setVisibility(View.GONE);
        homeBtn.setVisibility(View.GONE);
        answerOptions.setVisibility(View.VISIBLE);
        submitAnswerBtn.setVisibility(View.VISIBLE);
        timerProgress.setVisibility(View.VISIBLE);
        questionProgressText.setVisibility(View.VISIBLE);
        updateQuestionProgress();
        showNextQuestion();
    }

    private void showNextQuestion() {
        if (currentQuestion < questions.length) {
            questionText.setText(questions[currentQuestion]);
            updateQuestionProgress();

            List<String> currentOptionsList = new ArrayList<>();
            for (String option : options[currentQuestion]) {
                currentOptionsList.add(option);
            }

            Collections.shuffle(currentOptionsList);

            option1.setText(currentOptionsList.get(0));
            option2.setText(currentOptionsList.get(1));
            option3.setText(currentOptionsList.get(2));
            option4.setText(currentOptionsList.get(3));

            currentCorrectAnswer = options[currentQuestion][correctAnswers[currentQuestion]];

            option1.setBackgroundResource(R.drawable.radiobutton_selector);
            option2.setBackgroundResource(R.drawable.radiobutton_selector);
            option3.setBackgroundResource(R.drawable.radiobutton_selector);
            option4.setBackgroundResource(R.drawable.radiobutton_selector);
            option1.setVisibility(View.VISIBLE);
            option2.setVisibility(View.VISIBLE);
            option3.setVisibility(View.VISIBLE);
            option4.setVisibility(View.VISIBLE);

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
        questionTimer = new CountDownTimer(30000, 300) {
            public void onTick(long millisUntilFinished) {
                int progress = (int) ((30000 - millisUntilFinished) / 300);
                timerProgress.setProgress(progress);
            }

            public void onFinish() {
                incorrectCount++;
                resultText.setText("");
                changeColorOnTimeUp();
                currentQuestion++;
                new Handler(Looper.getMainLooper()).postDelayed(() -> showNextQuestion(), 1000);
            }
        }.start();
    }

    private void submitAnswer() {
        int selectedId = answerOptions.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
            return;
        }

        if (questionTimer != null) {
            questionTimer.cancel();
        }

        RadioButton selectedOption = findViewById(selectedId);
        String selectedAnswerText = selectedOption.getText().toString();

        if (selectedAnswerText.equals(currentCorrectAnswer)) {
            correctCount++;
            resultText.setText("Correct!");
            selectedOption.setBackgroundColor(getResources().getColor(R.color.green));
        } else {
            incorrectCount++;
            resultText.setText("Incorrect! The correct answer is: " + currentCorrectAnswer);
            changeColorOnIncorrectAnswer(selectedOption);
        }

        currentQuestion++;
        new Handler(Looper.getMainLooper()).postDelayed(() -> showNextQuestion(), 1000);
    }

    private void showResults() {
        resultText.setText("Quiz Completed! Correct: " + correctCount + ", Incorrect: " + incorrectCount);
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
        gradeText.setVisibility(View.VISIBLE);
        gradeEmoji.setVisibility(View.VISIBLE);
        submitAnswerBtn.setVisibility(View.GONE);
        answerOptions.setVisibility(View.GONE);
        timerProgress.setVisibility(View.GONE);
        questionProgressText.setVisibility(View.GONE);
        questionText.setVisibility(View.GONE);
        homeBtn.setVisibility(View.VISIBLE);
    }

    private int getSelectedOptionIndex(RadioButton selectedOption) {
        if (selectedOption == option1) return 0;
        if (selectedOption == option2) return 1;
        if (selectedOption == option3) return 2;
        if (selectedOption == option4) return 3;
        return -1;
    }

    private void changeColorOnTimeUp() {
        if (option1.getText().toString().equals(currentCorrectAnswer)) {
            option1.setBackgroundColor(getResources().getColor(R.color.green));
        } else {
            option1.setVisibility(View.GONE);
        }

        if (option2.getText().toString().equals(currentCorrectAnswer)) {
            option2.setBackgroundColor(getResources().getColor(R.color.green));
        } else {
            option2.setVisibility(View.GONE);
        }

        if (option3.getText().toString().equals(currentCorrectAnswer)) {
            option3.setBackgroundColor(getResources().getColor(R.color.green));
        } else {
            option3.setVisibility(View.GONE);
        }

        if (option4.getText().toString().equals(currentCorrectAnswer)) {
            option4.setBackgroundColor(getResources().getColor(R.color.green));
        } else {
            option4.setVisibility(View.GONE);
        }
    }

    private void changeColorOnIncorrectAnswer(RadioButton selectedOption) {
        if (option1.getText().toString().equals(currentCorrectAnswer)) {
            option1.setBackgroundColor(getResources().getColor(R.color.green));
        } else if (option1 == selectedOption) {
            option1.setBackgroundColor(getResources().getColor(R.color.red));
        } else {
            option1.setVisibility(View.GONE);
        }

        if (option2.getText().toString().equals(currentCorrectAnswer)) {
            option2.setBackgroundColor(getResources().getColor(R.color.green));
        } else if (option2 == selectedOption) {
            option2.setBackgroundColor(getResources().getColor(R.color.red));
        } else {
            option2.setVisibility(View.GONE);
        }

        if (option3.getText().toString().equals(currentCorrectAnswer)) {
            option3.setBackgroundColor(getResources().getColor(R.color.green));
        } else if (option3 == selectedOption) {
            option3.setBackgroundColor(getResources().getColor(R.color.red));
        } else {
            option3.setVisibility(View.GONE);
        }

        if (option4.getText().toString().equals(currentCorrectAnswer)) {
            option4.setBackgroundColor(getResources().getColor(R.color.green));
        } else if (option4 == selectedOption) {
            option4.setBackgroundColor(getResources().getColor(R.color.red));
        } else {
            option4.setVisibility(View.GONE);
        }
    }

    private void returnToStart() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("logged_in_user");
        editor.apply();

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void updateQuestionProgress() {
        questionProgressText.setText((currentQuestion + 1) + "/" + questions.length);
    }
}