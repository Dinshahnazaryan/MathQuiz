package com.example.mathquiz;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private TextView questionProgressText, questionText, resultText, splashText, gradeText;
    private RadioGroup answerOptions;
    private RadioButton option1, option2, option3, option4;
    private Button submitAnswerBtn, startBtn, homeBtn;
    private ImageButton accountBtn;
    private ProgressBar timerProgress, splashProgress;
    private LinearLayout splashLayout;
    private ImageView gradeEmoji;

    private ArrayList<Question> questions;
    private int currentQuestionIndex = 0;
    private int correctAnswers = 0;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            initializeUI();
            initializeQuestions();
            startSplashScreen();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, R.string.app_init_failed, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initializeUI() {
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
        gradeText = findViewById(R.id.gradeText);
        timerProgress = findViewById(R.id.timerProgress);
        splashProgress = findViewById(R.id.splashProgress);
        splashLayout = findViewById(R.id.splashLayout);
        gradeEmoji = findViewById(R.id.gradeEmoji);

        splashProgress.setMax(100);
        timerProgress.setMax(100);

        startBtn.setOnClickListener(v -> startQuiz());
        submitAnswerBtn.setOnClickListener(v -> checkAnswer());
        homeBtn.setOnClickListener(v -> resetQuiz());
        accountBtn.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(MainActivity.this, AccountActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error starting AccountActivity: " + e.getMessage(), e);
                Toast.makeText(this, R.string.nav_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startSplashScreen() {
        new CountDownTimer(3000, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                splashProgress.setProgress((int) (100 - (millisUntilFinished / 30)));
            }

            @Override
            public void onFinish() {
                try {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    Intent intent;
                    if (user != null && user.isEmailVerified()) {
                        intent = new Intent(MainActivity.this, AccountActivity.class);
                    } else {
                        intent = new Intent(MainActivity.this, LoginActivity.class);
                    }
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    Log.e(TAG, "Error starting activity: " + e.getMessage(), e);
                    Toast.makeText(MainActivity.this, R.string.nav_error, Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }.start();
    }

    private void initializeQuestions() {
        questions = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < 11; i++) {
            int num1 = rand.nextInt(100) + 1;
            int num2 = rand.nextInt(100) + 1;
            String operator = (rand.nextInt(2) == 0) ? "+" : "-";
            int correctAnswer = operator.equals("+") ? num1 + num2 : num1 - num2;
            String question = num1 + " " + operator + " " + num2 + " = ?";
            ArrayList<String> options = new ArrayList<>();
            options.add(String.valueOf(correctAnswer));
            options.add(String.valueOf(correctAnswer + rand.nextInt(10) + 1));
            options.add(String.valueOf(correctAnswer - rand.nextInt(10) - 1));
            options.add(String.valueOf(correctAnswer + rand.nextInt(20) - 10));
            Collections.shuffle(options);
            questions.add(new Question(question, options, options.indexOf(String.valueOf(correctAnswer))));
        }
    }

    private void startQuiz() {
        try {
            currentQuestionIndex = 0;
            correctAnswers = 0;
            splashLayout.setVisibility(View.GONE);
            startBtn.setVisibility(View.GONE);
            questionProgressText.setVisibility(View.VISIBLE);
            questionText.setVisibility(View.VISIBLE);
            answerOptions.setVisibility(View.VISIBLE);
            submitAnswerBtn.setVisibility(View.VISIBLE);
            timerProgress.setVisibility(View.VISIBLE);
            displayQuestion();
        } catch (Exception e) {
            Log.e(TAG, "Error starting quiz: " + e.getMessage(), e);
            Toast.makeText(this, R.string.app_error, Toast.LENGTH_LONG).show();
        }
    }

    private void displayQuestion() {
        try {
            Question q = questions.get(currentQuestionIndex);
            questionProgressText.setText((currentQuestionIndex + 1) + "/11");
            questionText.setText(q.getQuestion());
            option1.setText(q.getOptions().get(0));
            option2.setText(q.getOptions().get(1));
            option3.setText(q.getOptions().get(2));
            option4.setText(q.getOptions().get(3));
            answerOptions.clearCheck();
            resetRadioButtonBackgrounds();
            startTimer();
        } catch (Exception e) {
            Log.e(TAG, "Error displaying question: " + e.getMessage(), e);
            Toast.makeText(this, R.string.app_error, Toast.LENGTH_LONG).show();
        }
    }

    private void startTimer() {
        if (timer != null) {
            timer.cancel();
        }
        timerProgress.setProgress(100);
        timer = new CountDownTimer(15000, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerProgress.setProgress((int) (millisUntilFinished / 150));
            }

            @Override
            public void onFinish() {
                checkAnswer();
            }
        }.start();
    }

    private void checkAnswer() {
        try {
            if (timer != null) {
                timer.cancel();
            }
            int selectedId = answerOptions.getCheckedRadioButtonId();
            if (selectedId == -1) {
                moveToNextQuestion();
                return;
            }
            RadioButton selectedOption = findViewById(selectedId);
            int selectedIndex = answerOptions.indexOfChild(selectedOption);
            Question q = questions.get(currentQuestionIndex);
            if (selectedIndex == q.getCorrectAnswerIndex()) {
                selectedOption.setBackgroundResource(R.color.green);
                correctAnswers++;
            } else {
                selectedOption.setBackgroundResource(R.color.red);
                RadioButton correctOption = (RadioButton) answerOptions.getChildAt(q.getCorrectAnswerIndex());
                correctOption.setBackgroundResource(R.color.green);
            }
            submitAnswerBtn.setEnabled(false);
            new CountDownTimer(1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {}

                @Override
                public void onFinish() {
                    submitAnswerBtn.setEnabled(true);
                    moveToNextQuestion();
                }
            }.start();
        } catch (Exception e) {
            Log.e(TAG, "Error checking answer: " + e.getMessage(), e);
            Toast.makeText(this, R.string.app_error, Toast.LENGTH_LONG).show();
        }
    }

    private void moveToNextQuestion() {
        try {
            currentQuestionIndex++;
            if (currentQuestionIndex < questions.size()) {
                displayQuestion();
            } else {
                showResults();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error moving to next question: " + e.getMessage(), e);
            Toast.makeText(this, R.string.app_error, Toast.LENGTH_LONG).show();
        }
    }

    private void showResults() {
        try {
            questionProgressText.setVisibility(View.GONE);
            questionText.setVisibility(View.GONE);
            answerOptions.setVisibility(View.GONE);
            submitAnswerBtn.setVisibility(View.GONE);
            timerProgress.setVisibility(View.GONE);
            resultText.setVisibility(View.VISIBLE);
            gradeText.setVisibility(View.VISIBLE);
            gradeEmoji.setVisibility(View.VISIBLE);
            homeBtn.setVisibility(View.VISIBLE);

            double percentage = (correctAnswers / 11.0) * 100;
            String grade;
            int emojiResId;
            if (percentage >= 90) {
                grade = "A";
                emojiResId = R.drawable.excellent;
            } else if (percentage >= 80) {
                grade = "B";
                emojiResId = R.drawable.good;
            } else if (percentage >= 70) {
                grade = "C";
                emojiResId = R.drawable.norm;
            } else {
                grade = "F";
                emojiResId = R.drawable.bad;
            }

            gradeText.setText(grade);
            gradeEmoji.setImageResource(emojiResId);
            resultText.setText(String.format("You got %d out of 11 correct (%.1f%%)", correctAnswers, percentage));
        } catch (Exception e) {
            Log.e(TAG, "Error showing results: " + e.getMessage(), e);
            Toast.makeText(this, R.string.app_error, Toast.LENGTH_LONG).show();
        }
    }

    private void resetQuiz() {
        try {
            questionProgressText.setVisibility(View.GONE);
            questionText.setVisibility(View.VISIBLE);
            answerOptions.setVisibility(View.GONE);
            submitAnswerBtn.setVisibility(View.GONE);
            timerProgress.setVisibility(View.GONE);
            resultText.setVisibility(View.GONE);
            gradeText.setVisibility(View.GONE);
            gradeEmoji.setVisibility(View.GONE);
            homeBtn.setVisibility(View.GONE);
            startBtn.setVisibility(View.VISIBLE);
            questionText.setText("Press Start to begin the quiz");
            initializeQuestions();
        } catch (Exception e) {
            Log.e(TAG, "Error resetting quiz: " + e.getMessage(), e);
            Toast.makeText(this, R.string.app_error, Toast.LENGTH_LONG).show();
        }
    }

    private void resetRadioButtonBackgrounds() {
        option1.setBackgroundResource(R.drawable.radiobutton_selector);
        option2.setBackgroundResource(R.drawable.radiobutton_selector);
        option3.setBackgroundResource(R.drawable.radiobutton_selector);
        option4.setBackgroundResource(R.drawable.radiobutton_selector);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }

    private static class Question {
        private String question;
        private ArrayList<String> options;
        private int correctAnswerIndex;

        public Question(String question, ArrayList<String> options, int correctAnswerIndex) {
            this.question = question;
            this.options = options;
            this.correctAnswerIndex = correctAnswerIndex;
        }

        public String getQuestion() {
            return question;
        }

        public ArrayList<String> getOptions() {
            return options;
        }

        public int getCorrectAnswerIndex() {
            return correctAnswerIndex;
        }
    }
}