package com.example.mathquiz;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
    private Button submitAnswerBtn, startBtn;
    private TextView resultText;

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

    private int[] correctAnswers = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

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
        resultText = findViewById(R.id.resultText);

        startBtn.setOnClickListener(v -> startQuiz());

        submitAnswerBtn.setOnClickListener(v -> submitAnswer());
    }

    private void startQuiz() {
        currentQuestion = 0;
        correctCount = 0;
        incorrectCount = 0;
        resultText.setText("");
        startBtn.setVisibility(View.GONE);
        answerOptions.setVisibility(View.VISIBLE);
        submitAnswerBtn.setVisibility(View.VISIBLE);
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
        } else {
            resultText.setText("Quiz Completed! Correct answers: " + correctCount + ", Incorrect answers: " + incorrectCount);
            submitAnswerBtn.setVisibility(View.GONE);
        }
    }

    private void submitAnswer() {
        int selectedId = answerOptions.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedOption = findViewById(selectedId);
        int selectedAnswerIndex = getSelectedOptionIndex(selectedOption);

        if (selectedAnswerIndex == correctAnswers[currentQuestion]) {
            correctCount++;
            resultText.setText("Correct!");
        } else {
            incorrectCount++;
            resultText.setText("Incorrect! The correct answer is: " + options[currentQuestion][correctAnswers[currentQuestion]]);
        }

        currentQuestion++;
        if (currentQuestion < questions.length) {
            showNextQuestion();
            answerOptions.clearCheck();
        }
    }

    private int getSelectedOptionIndex(RadioButton selectedOption) {
        if (selectedOption == option1) return 0;
        if (selectedOption == option2) return 1;
        if (selectedOption == option3) return 2;
        if (selectedOption == option4) return 3;
        return -1;
    }
}
