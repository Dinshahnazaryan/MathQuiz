package com.example.mathquiz;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private TextView questionProgressText, questionText, resultText, splashText, gradeText, startPromptText, titleText;
    private RadioGroup answerOptions;
    private RadioButton option1, option2, option3, option4;
    private Button submitAnswerBtn, startBtn, homeBtn;
    private ImageButton accountBtn;
    private ProgressBar timerProgress, splashProgress;
    private LinearLayout splashLayout, quizLayout, resultLayout, titleLayout;
    private ImageView gradeEmoji;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CountDownTimer questionTimer;

    private String[] questions = {
            "What is the sum of 130 + 125 + 191?", "If we minus 712 from 1500, how much do we get?",
            "50 times of 8 is equal to?", "110 divided by 10 is?", "20 + (90 ÷ 2) is equal to?",
            "The product of 82 and 5 is?", "Find the missing terms in multiple of 3: 3, 6, 9, __, 15",
            "Solve 24 ÷ 8 + 2.", "Solve: 300 – (150 × 2)", "The product of 121 × 0 × 200 × 25 is",
            "What is the next prime number after 5?"
    };

    private String[][] options = {
            {"446", "500", "400", "600"}, {"788", "700", "1000", "600"}, {"400", "500", "450", "350"},
            {"11", "12", "10", "9"}, {"65", "60", "80", "100"}, {"410", "400", "500", "300"},
            {"12", "10", "15", "14"}, {"5", "7", "3", "2"}, {"0", "150", "100", "200"},
            {"0", "250", "500", "1000"}, {"7", "9", "11", "13"}
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
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();

        splashProgress.setMax(100);
        new CountDownTimer(3000, 100) {
            public void onTick(long millisUntilFinished) {
                splashProgress.setProgress((int) ((3000 - millisUntilFinished) / 30));
            }
            public void onFinish() {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user == null) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                } else {
                    Log.d(TAG, "Showing start screen");
                    splashLayout.setVisibility(View.GONE);
                    titleLayout.setVisibility(View.VISIBLE);
                    startPromptText.setVisibility(View.VISIBLE);
                    startBtn.setVisibility(View.VISIBLE);
                    accountBtn.setVisibility(View.VISIBLE);
                }
            }
        }.start();

        startBtn.setOnClickListener(v -> {
            Log.d(TAG, "Start button clicked");
            startQuiz();
        });

        submitAnswerBtn.setOnClickListener(v -> submitAnswer());
        homeBtn.setOnClickListener(v -> returnToStart());

        accountBtn.setOnClickListener(v -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            } else {
                Intent intent = new Intent(MainActivity.this, AccountActivity.class);
                intent.putExtra("email", user.getEmail());
                startActivity(intent);
            }
        });

        // Debug RadioButton selection
        answerOptions.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != -1) {
                RadioButton selected = findViewById(checkedId);
                Log.d(TAG, "RadioButton selected: " + selected.getText());
                submitAnswerBtn.setEnabled(true);
            }
        });
    }

    private void initViews() {
        splashLayout = findViewById(R.id.splashLayout);
        quizLayout = findViewById(R.id.quizLayout);
        resultLayout = findViewById(R.id.resultLayout);
        titleLayout = findViewById(R.id.titleLayout);

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
        startPromptText = findViewById(R.id.startPromptText);
        titleText = findViewById(R.id.titleText);
        timerProgress = findViewById(R.id.timerProgress);
        splashProgress = findViewById(R.id.splashProgress);
        gradeText = findViewById(R.id.gradeText);
        gradeEmoji = findViewById(R.id.gradeEmoji);

        // Debug initial state
        Log.d(TAG, "AnswerOptions enabled: " + answerOptions.isEnabled());
        Log.d(TAG, "AnswerOptions visible: " + (answerOptions.getVisibility() == View.VISIBLE));
        for (RadioButton rb : new RadioButton[]{option1, option2, option3, option4}) {
            Log.d(TAG, "RadioButton " + rb.getText() + " enabled: " + rb.isEnabled());
        }
    }

    private void startQuiz() {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Starting quiz");
        currentQuestion = 0;
        correctCount = 0;
        incorrectCount = 0;

        splashLayout.setVisibility(View.GONE);
        titleLayout.setVisibility(View.GONE);
        resultLayout.setVisibility(View.GONE);
        startBtn.setVisibility(View.GONE);
        startPromptText.setVisibility(View.GONE);
        quizLayout.setVisibility(View.VISIBLE);
        questionText.setVisibility(View.VISIBLE);
        questionProgressText.setVisibility(View.VISIBLE);
        timerProgress.setVisibility(View.VISIBLE);
        answerOptions.setVisibility(View.VISIBLE);
        submitAnswerBtn.setVisibility(View.VISIBLE);
        submitAnswerBtn.setEnabled(false); // Disable until selection
        resultText.setVisibility(View.GONE); // Hide until answer feedback

        showNextQuestion();
    }

    private void showNextQuestion() {
        Log.d(TAG, "Showing question " + (currentQuestion + 1));
        if (currentQuestion >= questions.length) {
            showResults();
            return;
        }

        answerOptions.clearCheck();

        for (RadioButton rb : new RadioButton[]{option1, option2, option3, option4}) {
            rb.setEnabled(true);
            rb.setBackgroundResource(R.drawable.radiobutton_background); // Fixed resource
            rb.setVisibility(View.VISIBLE);
        }

        submitAnswerBtn.setEnabled(false);

        questionText.setText(questions[currentQuestion]);
        questionProgressText.setText((currentQuestion + 1) + "/" + questions.length);
        currentCorrectAnswer = correctAnswersText[currentQuestion];

        List<String> optionList = new ArrayList<>(Arrays.asList(options[currentQuestion]));
        Collections.shuffle(optionList);

        option1.setText(optionList.get(0));
        option2.setText(optionList.get(1));
        option3.setText(optionList.get(2));
        option4.setText(optionList.get(3));

        startQuestionTimer();
    }

    private void startQuestionTimer() {
        if (questionTimer != null) questionTimer.cancel();

        timerProgress.setProgress(0);
        timerProgress.setMax(100);

        questionTimer = new CountDownTimer(15000, 150) {
            public void onTick(long millisUntilFinished) {
                timerProgress.setProgress((int) ((15000 - millisUntilFinished) / 150));
            }

            public void onFinish() {
                incorrectCount++;
                resultText.setText("Time's up! Correct: " + currentCorrectAnswer);
                resultText.setVisibility(View.VISIBLE);
                highlightCorrectAnswer();
                currentQuestion++;
                new Handler(Looper.getMainLooper()).postDelayed(() -> showNextQuestion(), 2000);
            }
        }.start();
    }

    private void submitAnswer() {
        int selectedId = answerOptions.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
            return;
        }

        if (questionTimer != null) questionTimer.cancel();

        RadioButton selectedOption = findViewById(selectedId);
        String selectedAnswer = selectedOption.getText().toString();

        if (selectedAnswer.equals(currentCorrectAnswer)) {
            correctCount++;
            resultText.setText("Correct!");
        } else {
            incorrectCount++;
            resultText.setText("Incorrect! Correct: " + currentCorrectAnswer);
            selectedOption.setBackgroundColor(Color.RED);
        }
        resultText.setVisibility(View.VISIBLE);

        highlightCorrectAnswer();
        currentQuestion++;
        new Handler(Looper.getMainLooper()).postDelayed(() -> showNextQuestion(), 2000);
    }

    private void highlightCorrectAnswer() {
        for (RadioButton rb : new RadioButton[]{option1, option2, option3, option4}) {
            if (rb.getText().toString().equals(currentCorrectAnswer)) {
                rb.setBackgroundColor(Color.GREEN);
            }
            rb.setEnabled(false);
        }
    }

    private void showResults() {
        Log.d(TAG, "Showing results");
        quizLayout.setVisibility(View.GONE);
        resultLayout.setVisibility(View.VISIBLE);
        resultText.setText("Correct: " + correctCount + ", Incorrect: " + incorrectCount);
        gradeText.setVisibility(View.VISIBLE);
        gradeEmoji.setVisibility(View.VISIBLE);
        homeBtn.setVisibility(View.VISIBLE);
        int percentage = (correctCount * 100) / questions.length;
        gradeText.setText(percentage + "%");
    }

    private void returnToStart() {
        Log.d(TAG, "Returning to start");
        currentQuestion = -1;
        correctCount = 0;
        incorrectCount = 0;
        resultLayout.setVisibility(View.GONE);
        quizLayout.setVisibility(View.GONE);
        titleLayout.setVisibility(View.VISIBLE);
        startBtn.setVisibility(View.VISIBLE);
        startPromptText.setVisibility(View.VISIBLE);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnectedOrConnecting();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (questionTimer != null) questionTimer.cancel();
    }
}