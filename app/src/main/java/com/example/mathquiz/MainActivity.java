package com.example.mathquiz;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
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

    private TextView questionText;
    private RadioGroup answerOptions;
    private RadioButton option1, option2, option3, option4;
    private Button submitAnswerBtn, startBtn, homeBtn;
    private TextView resultText;
    private TextView splashText;
    private ProgressBar timerProgress, splashProgress;
    private LinearLayout splashLayout;
    private CountDownTimer questionTimer;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        currentQuestion = 0;
        correctCount = 0;
        incorrectCount = 0;
        resultText.setText("");
        startBtn.setVisibility(View.GONE);
        homeBtn.setVisibility(View.GONE);
        answerOptions.setVisibility(View.VISIBLE);
        submitAnswerBtn.setVisibility(View.VISIBLE);
        timerProgress.setVisibility(View.VISIBLE);
        showNextQuestion();
    }

    private void showNextQuestion() {
        if (currentQuestion < questions.length) {
            questionText.setText(questions[currentQuestion]);

            List<String> currentOptionsList = new ArrayList<>();
            for (String option : options[currentQuestion]) {
                currentOptionsList.add(option);
            }

            Collections.shuffle(currentOptionsList);

            option1.setText(currentOptionsList.get(0));
            option2.setText(currentOptionsList.get(1));
            option3.setText(currentOptionsList.get(2));
            option4.setText(currentOptionsList.get(3));

            correctAnswers[currentQuestion] = currentOptionsList.indexOf(options[currentQuestion][0]);

            option1.setBackgroundResource(R.drawable.radiobutton_selector);
            option2.setBackgroundResource(R.drawable.radiobutton_selector);
            option3.setBackgroundResource(R.drawable.radiobutton_selector);
            option4.setBackgroundResource(R.drawable.radiobutton_selector);

            answerOptions.clearCheck();

            startQuestionTimer();
        } else {
            resultText.setText("Quiz Completed! Correct answers: " + correctCount + ", Incorrect answers: " + incorrectCount);
            submitAnswerBtn.setVisibility(View.GONE);
            answerOptions.setVisibility(View.GONE);
            timerProgress.setVisibility(View.GONE);
            homeBtn.setVisibility(View.VISIBLE);
            if (questionTimer != null) {
                questionTimer.cancel();
            }
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
                resultText.setText("Time's up! The correct answer was: " + options[currentQuestion][correctAnswers[currentQuestion]]);
                changeColorOnIncorrectAnswer();
                currentQuestion++;
                showNextQuestion();
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
        int selectedAnswerIndex = getSelectedOptionIndex(selectedOption);

        if (selectedAnswerIndex == correctAnswers[currentQuestion]) {
            correctCount++;
            resultText.setText("Correct!");
            selectedOption.setBackgroundColor(getResources().getColor(R.color.green));
        } else {
            incorrectCount++;
            resultText.setText("Incorrect! The correct answer is: " + options[currentQuestion][correctAnswers[currentQuestion]]);
            changeColorOnIncorrectAnswer();
        }

        currentQuestion++;
        new android.os.Handler().postDelayed(() -> showNextQuestion(), 1000);
    }

    private int getSelectedOptionIndex(RadioButton selectedOption) {
        if (selectedOption == option1) return 0;
        if (selectedOption == option2) return 1;
        if (selectedOption == option3) return 2;
        if (selectedOption == option4) return 3;
        return -1;
    }

    private void changeColorOnIncorrectAnswer() {
        if (option1.getText().equals(options[currentQuestion][correctAnswers[currentQuestion]])) {
            option1.setBackgroundColor(getResources().getColor(R.color.green));
        } else {
            option1.setBackgroundColor(getResources().getColor(R.color.red));
        }

        if (option2.getText().equals(options[currentQuestion][correctAnswers[currentQuestion]])) {
            option2.setBackgroundColor(getResources().getColor(R.color.green));
        } else {
            option2.setBackgroundColor(getResources().getColor(R.color.red));
        }

        if (option3.getText().equals(options[currentQuestion][correctAnswers[currentQuestion]])) {
            option3.setBackgroundColor(getResources().getColor(R.color.green));
        } else {
            option3.setBackgroundColor(getResources().getColor(R.color.red));
        }

        if (option4.getText().equals(options[currentQuestion][correctAnswers[currentQuestion]])) {
            option4.setBackgroundColor(getResources().getColor(R.color.green));
        } else {
            option4.setBackgroundColor(getResources().getColor(R.color.red));
        }
    }

    private void returnToStart() {
        questionText.setText("Press Start to begin the quiz");
        questionText.setVisibility(View.VISIBLE);
        startBtn.setVisibility(View.VISIBLE);
        resultText.setText("");
        resultText.setVisibility(View.VISIBLE);
        splashLayout.setVisibility(View.GONE);
        answerOptions.setVisibility(View.GONE);
        submitAnswerBtn.setVisibility(View.GONE);
        timerProgress.setVisibility(View.GONE);
        homeBtn.setVisibility(View.GONE);
        currentQuestion = -1;
        correctCount = 0;
        incorrectCount = 0;
    }
}