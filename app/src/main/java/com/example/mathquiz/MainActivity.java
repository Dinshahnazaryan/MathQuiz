package com.example.mathquiz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
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
import com.google.firebase.firestore.SetOptions;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ImageView feedbackIcon;

    private TextView questionProgressText, questionText, resultText, splashText, gradeText, startPromptText, titleText;
    private RadioGroup answerOptions;
    private RadioButton option1, option2, option3, option4;
    private Button submitAnswerBtn, startBtn, homeBtn;
    private ImageButton accountBtn, levelsBtn;
    private ProgressBar timerProgress, splashProgress;
    private LinearLayout splashLayout, quizLayout, resultLayout, titleLayout;
    private ImageView gradeEmoji;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CountDownTimer questionTimer;

    private List<Integer> questionOrder;
    private int currentQuestion = -1;
    private int correctCount = 0;
    private int incorrectCount = 0;
    private String currentCorrectAnswer;

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

    private final List<QuizItem> quizItems = Arrays.asList(
            new QuizItem("What is the sum of the first 3 odd numbers after 100?", new String[]{"309", "303", "315", "307"}, "303"),
            new QuizItem("If we minus 712 from 1500, how much do we get?", new String[]{"788", "700", "1000", "600"}, "788"),
            new QuizItem("Evaluate: (5² + 3²) × 4", new String[]{"208", "176", "144", "128"}, "208"),
            new QuizItem("110 divided by 10 is?", new String[]{"11", "12", "10", "9"}, "11"),
            new QuizItem("20 + (90 ÷ 2) is equal to?", new String[]{"65", "60", "80", "100"}, "65"),
            new QuizItem("The product of 82 and 5 is?", new String[]{"410", "400", "500", "300"}, "410"),
            new QuizItem("Find the missing terms in multiple of 3: 3, 6, 9, __, 15", new String[]{"12", "10", "15", "14"}, "12"),
            new QuizItem("Solve 24 ÷ 8 + 2.", new String[]{"5", "7", "3", "2"}, "5"),
            new QuizItem("Solve: 300 – (150 × 2)", new String[]{"0", "150", "100", "200"}, "0"),
            new QuizItem("The product of 121 × 0 × 200 × 25 is", new String[]{"0", "250", "500", "1000"}, "0"),
            new QuizItem("What is the next prime number after 5?", new String[]{"7", "9", "11", "13"}, "7"),
            new QuizItem("What is 7 + 6?", new String[]{"13", "14", "15", "12"}, "13"),
            new QuizItem("What is 12 × 3?", new String[]{"36", "24", "30", "40"}, "36"),
            new QuizItem("What is 144 ÷ 12?", new String[]{"12", "10", "11", "14"}, "12"),
            new QuizItem("What is the square root of 81?", new String[]{"9", "8", "7", "6"}, "9"),
            new QuizItem("What is 100 – 45?", new String[]{"55", "65", "45", "50"}, "55"),
            new QuizItem("What is 6 squared?", new String[]{"36", "30", "12", "18"}, "36"),
            new QuizItem("What is the next even number after 18?", new String[]{"20", "22", "19", "21"}, "20"),
            new QuizItem("What is 2³?", new String[]{"8", "6", "4", "9"}, "8"),
            new QuizItem("What is 9 × 9?", new String[]{"81", "72", "90", "99"}, "81"),
            new QuizItem("What is 15% of 200?", new String[]{"30", "25", "35", "20"}, "30"),
            new QuizItem("If x = 3, what is x² + 2x + 1?", new String[]{"16", "12", "18", "14"}, "16"),
            new QuizItem("What is 50% of 300?", new String[]{"150", "100", "200", "180"}, "150"),
            new QuizItem("What is 5 × 11?", new String[]{"55", "50", "60", "45"}, "55"),
            new QuizItem("What is 3 + 4 × 2?", new String[]{"11", "14", "10", "8"}, "11"),
            new QuizItem("What is 0.5 × 100?", new String[]{"50", "25", "75", "60"}, "50"),
            new QuizItem("What is the area of a square with side 4?", new String[]{"16", "8", "12", "10"}, "16"),
            new QuizItem("What is the perimeter of a rectangle 5×3?", new String[]{"16", "15", "18", "20"}, "16"),
            new QuizItem("What is the value of π (pi) approximately?", new String[]{"3.14", "3.12", "3.10", "3.16"}, "3.14"),
            new QuizItem("How many degrees in a triangle?", new String[]{"180", "360", "90", "270"}, "180"),
            new QuizItem("What is 0 divided by 5?", new String[]{"0", "1", "5", "Undefined"}, "0"),
            new QuizItem("What is 5 divided by 0?", new String[]{"Undefined", "0", "5", "1"}, "Undefined"),
            new QuizItem("What is the cube root of 27?", new String[]{"3", "2", "9", "4"}, "3"),
            new QuizItem("What is the result of 2 + 2 × 2?", new String[]{"6", "8", "10", "4"}, "6"),
            new QuizItem("How many sides does a hexagon have?", new String[]{"6", "5", "7", "8"}, "6"),
            new QuizItem("How many seconds are in 3.75 minutes?", new String[]{"225", "210", "230", "240"}, "225"),
            new QuizItem("Convert 5.25 hours into minutes.", new String[]{"315", "300", "320", "310"}, "315"),
            new QuizItem("If a workday is 8 hours long and you work 3 full days, how many hours have you worked?", new String[]{"24", "21", "18", "27"}, "24"),
            new QuizItem("If you work every weekday for 6 weeks, how many days have you worked?", new String[]{"30", "36", "28", "35"}, "30"),
            new QuizItem("What is the value of 7 + (6 × 5² + 3)?", new String[]{"160", "166", "155", "170"}, "160"),
            new QuizItem("What is the average of 2, 4, 6, 8, 10?", new String[]{"6", "5", "8", "4"}, "6"),
            new QuizItem("What is 1000 ÷ 10?", new String[]{"100", "10", "50", "150"}, "100"),
            new QuizItem("A rope is 3.5 meters long. How many centimeters is that?", new String[]{"350", "300", "400", "250"}, "350"),
            new QuizItem("A pen is 14 centimeters long. How many millimeters is that?", new String[]{"140", "130", "150", "120"}, "140"),
            new QuizItem("What is 8 × 7?", new String[]{"56", "64", "48", "72"}, "56"),
            new QuizItem("What is 72 ÷ 8?", new String[]{"9", "8", "7", "6"}, "9"),
            new QuizItem("What is the cube of the square root of 81?", new String[]{"729", "243", "81", "27"}, "729"),
            new QuizItem("What is 6 × 6?", new String[]{"36", "30", "40", "32"}, "36"),
            new QuizItem("What is 12 × 12?", new String[]{"144", "124", "132", "156"}, "144"),
            new QuizItem("What is the result of 25 + 25?", new String[]{"50", "45", "55", "60"}, "50"),
            new QuizItem("What is 60 – 15?", new String[]{"45", "40", "50", "55"}, "45"),
            new QuizItem("What is the value of 2⁴?", new String[]{"16", "8", "12", "10"}, "16"),
            new QuizItem("What is 10% of 500?", new String[]{"50", "40", "30", "60"}, "50"),
            new QuizItem("What is the result of 5²?", new String[]{"25", "20", "15", "30"}, "25"),
            new QuizItem("What is 11 × 11?", new String[]{"121", "111", "131", "100"}, "121"),
            new QuizItem("What is 13 + 14?", new String[]{"27", "26", "28", "25"}, "27"),
            new QuizItem("What is 20 × 5?", new String[]{"100", "90", "110", "95"}, "100"),
            new QuizItem("What is 8 + 15?", new String[]{"23", "22", "24", "25"}, "23"),
            new QuizItem("What is 17 – 9?", new String[]{"8", "7", "9", "6"}, "8"),
            new QuizItem("What is 30 ÷ 6?", new String[]{"5", "6", "4", "7"}, "5"),
            new QuizItem("What is 3³?", new String[]{"27", "9", "18", "36"}, "27"),
            new QuizItem("What is 14 × 2?", new String[]{"28", "24", "26", "30"}, "28"),
            new QuizItem("What is 90 ÷ 9?", new String[]{"10", "9", "8", "11"}, "10"),
            new QuizItem("What is 100 – 25?", new String[]{"75", "85", "70", "65"}, "75"),
            new QuizItem("What is 6 × 7?", new String[]{"42", "48", "36", "40"}, "42"),
            new QuizItem("What is 49 ÷ 7?", new String[]{"7", "6", "8", "9"}, "7"),
            new QuizItem("What is 81 ÷ 9?", new String[]{"9", "8", "7", "6"}, "9"),
            new QuizItem("What is 100 × 0?", new String[]{"0", "100", "1", "10"}, "0"),
            new QuizItem("What is 12 – 8?", new String[]{"4", "5", "3", "6"}, "4"),
            new QuizItem("What is 15 + 5?", new String[]{"20", "25", "30", "15"}, "20"),
            new QuizItem("What is 45 ÷ 5?", new String[]{"9", "8", "7", "10"}, "9"),
            new QuizItem("What is the product of 10 × 10?", new String[]{"100", "90", "110", "120"}, "100"),
            new QuizItem("What is 25% of 200?", new String[]{"50", "40", "60", "70"}, "50"),
            new QuizItem("What is 36 ÷ 6?", new String[]{"6", "5", "7", "4"}, "6"),
            new QuizItem("What is 4³?", new String[]{"64", "16", "32", "81"}, "64"),
            new QuizItem("What is the square root of 100?", new String[]{"10", "9", "11", "12"}, "10"),
            new QuizItem("What is 8 × 8?", new String[]{"64", "56", "72", "48"}, "64"),
            new QuizItem("What is 7 × 7?", new String[]{"49", "42", "36", "56"}, "49"),
            new QuizItem("What is 5 + 6 + 7?", new String[]{"18", "17", "19", "20"}, "18"),
            new QuizItem("What is 60 + 40?", new String[]{"100", "90", "110", "80"}, "100"),
            new QuizItem("What is 120 ÷ 10?", new String[]{"12", "10", "11", "13"}, "12"),
            new QuizItem("What is 11 + 22?", new String[]{"33", "34", "32", "35"}, "33"),
            new QuizItem("What is 88 ÷ 8?", new String[]{"11", "10", "9", "8"}, "11"),
            new QuizItem("What is 14 + 16?", new String[]{"30", "28", "31", "29"}, "30"),
            new QuizItem("What is 40 – 19?", new String[]{"21", "20", "22", "19"}, "21"),
            new QuizItem("What is 18 + 24?", new String[]{"42", "41", "40", "43"}, "42"),
            new QuizItem("What is 100 ÷ 4?", new String[]{"25", "20", "30", "24"}, "25")
    );

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
                    splashLayout.setVisibility(View.GONE);
                    titleLayout.setVisibility(View.VISIBLE);
                    startPromptText.setVisibility(View.VISIBLE);
                    startBtn.setVisibility(View.VISIBLE);
                    accountBtn.setVisibility(View.VISIBLE);
                    levelsBtn.setVisibility(View.VISIBLE);
                }
            }
        }.start();

        startBtn.setOnClickListener(v -> startQuiz());
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

        levelsBtn.setOnClickListener(v -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            } else {
                startActivity(new Intent(MainActivity.this, LevelSelectionActivity.class));
            }
        });

        answerOptions.setOnCheckedChangeListener((group, checkedId) -> {
            submitAnswerBtn.setEnabled(checkedId != -1);
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
        levelsBtn = findViewById(R.id.levelsBtn);
        resultText = findViewById(R.id.resultText);
        splashText = findViewById(R.id.splashText);
        startPromptText = findViewById(R.id.startPromptText);
        titleText = findViewById(R.id.titleText);
        timerProgress = findViewById(R.id.timerProgress);
        splashProgress = findViewById(R.id.splashProgress);
        gradeText = findViewById(R.id.gradeText);
        gradeEmoji = findViewById(R.id.gradeEmoji);
    }

    private void startQuiz() {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        int level = getIntent().getIntExtra("level", 1);
        currentQuestion = 0;
        correctCount = 0;
        incorrectCount = 0;

        questionOrder = new ArrayList<>();
        List<QuizItem> levelQuestions = new ArrayList<>();
        // Simple level-based filtering: split questions into 10 levels
        int questionsPerLevel = quizItems.size() / 10;
        int startIndex = (level - 1) * questionsPerLevel;
        int endIndex = Math.min(startIndex + questionsPerLevel, quizItems.size());
        for (int i = startIndex; i < endIndex; i++) {
            levelQuestions.add(quizItems.get(i));
        }

        for (int i = 0; i < levelQuestions.size(); i++) {
            questionOrder.add(i);
        }
        Collections.shuffle(questionOrder);
        questionOrder = questionOrder.subList(0, Math.min(10, levelQuestions.size()));

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
        submitAnswerBtn.setEnabled(false);
        resultText.setVisibility(View.GONE);

        showNextQuestion();
    }

    private void showNextQuestion() {
        if (currentQuestion >= questionOrder.size()) {
            showResults();
            return;
        }

        answerOptions.clearCheck();
        for (RadioButton rb : new RadioButton[]{option1, option2, option3, option4}) {
            rb.setEnabled(true);
            rb.setBackgroundResource(R.drawable.radiobutton_background);
            rb.setVisibility(View.VISIBLE);
        }

        submitAnswerBtn.setEnabled(false);

        QuizItem item = quizItems.get(questionOrder.get(currentQuestion));
        questionText.setText(item.question);
        questionProgressText.setText((currentQuestion + 1) + "/" + questionOrder.size());
        currentCorrectAnswer = item.correctAnswer;

        List<String> optionList = new ArrayList<>(Arrays.asList(item.options));
        Collections.shuffle(optionList);

        option1.setText(optionList.get(0));
        option2.setText(optionList.get(1));
        option3.setText(optionList.get(2));
        option4.setText(optionList.get(3));

        startQuestionTimer();
    }

    private long remainingTime;

    private void startQuestionTimer() {
        if (questionTimer != null) questionTimer.cancel();
        timerProgress.setProgress(0);
        timerProgress.setMax(100);
        remainingTime = 15000;
        questionTimer = new CountDownTimer(15000, 150) {
            public void onTick(long millisUntilFinished) {
                remainingTime = millisUntilFinished;
                timerProgress.setProgress((int) ((15000 - millisUntilFinished) / 150));
            }
            public void onFinish() {
                incorrectCount++;
                resultText.setText("Time's up! Correct: " + currentCorrectAnswer);
                resultText.setVisibility(View.VISIBLE);
                highlightCorrectAnswer();
                currentQuestion++;
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (feedbackIcon != null) {
                        ((LinearLayout) findViewById(R.id.quizLayout)).removeView(feedbackIcon);
                    }
                    showNextQuestion();
                }, 1000);
            }
        }.start();
    }

    private void submitAnswer() {
        feedbackIcon = new ImageView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(200, 200);
        params.topMargin = 20;
        params.gravity = android.view.Gravity.CENTER;
        feedbackIcon.setLayoutParams(params);
        ((LinearLayout) findViewById(R.id.quizLayout)).addView(feedbackIcon);

        int selectedId = answerOptions.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
            return;
        }

        if (questionTimer != null) questionTimer.cancel();

        RadioButton selectedOption = findViewById(selectedId);
        String selectedAnswer = selectedOption.getText().toString();

        if (selectedAnswer.equals(currentCorrectAnswer)) {
            int bonus = (int) (remainingTime / 3000);
            correctCount += 1 + bonus;
            feedbackIcon.setImageResource(R.drawable.good);
            resultText.setText("Correct!");
        } else {
            incorrectCount++;
            resultText.setText("Incorrect! Correct: " + currentCorrectAnswer);
            feedbackIcon.setImageResource(R.drawable.bad);
            selectedOption.setBackgroundColor(Color.RED);
        }

        resultText.setVisibility(View.VISIBLE);
        highlightCorrectAnswer();
        currentQuestion++;
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (feedbackIcon != null) {
                ((LinearLayout) findViewById(R.id.quizLayout)).removeView(feedbackIcon);
            }
            showNextQuestion();
        }, 2000);
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
        quizLayout.setVisibility(View.GONE);
        resultLayout.setVisibility(View.VISIBLE);
        resultText.setText("Correct: " + correctCount + ", Incorrect: " + incorrectCount);
        gradeText.setVisibility(View.VISIBLE);
        gradeEmoji.setVisibility(View.VISIBLE);
        homeBtn.setVisibility(View.VISIBLE);
        int percentage = (correctCount * 100) / questionOrder.size();
        gradeText.setText(percentage + "%");

        int level = getIntent().getIntExtra("level", 1);
        SharedPreferences prefs = getSharedPreferences("quizPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        if (percentage >= 80) {
            int passes = prefs.getInt("level" + level + "_passes", 0);
            editor.putInt("level" + level + "_passes", passes + 1);
            editor.apply();
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            Map<String, Object> update = new HashMap<>();
            update.put("totalScore", correctCount);
            update.put("level" + level + "_score", percentage);
            db.collection("users").document(userId).set(update, SetOptions.merge());
        }
    }

    private void returnToStart() {
        currentQuestion = -1;
        correctCount = 0;
        incorrectCount = 0;
        resultLayout.setVisibility(View.GONE);
        quizLayout.setVisibility(View.GONE);
        titleLayout.setVisibility(View.VISIBLE);
        startBtn.setVisibility(View.VISIBLE);
        startPromptText.setVisibility(View.VISIBLE);
        accountBtn.setVisibility(View.VISIBLE);
        levelsBtn.setVisibility(View.VISIBLE);
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