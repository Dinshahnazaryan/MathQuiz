package com.example.mathquiz;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TopicExplanationActivity extends AppCompatActivity {
    private RecyclerView topicRecyclerView;
    private Button backButton;
    private TopicAdapter topicAdapter;
    private List<TopicItem> topicItems = new ArrayList<>();

    private static class TopicItem {
        String title;
        String explanation;
        String example;
        String difficulty;

        TopicItem(String title, String explanation, String example, String difficulty) {
            this.title = title;
            this.explanation = explanation;
            this.example = example;
            this.difficulty = difficulty;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_explanation);

        topicRecyclerView = findViewById(R.id.topicRecyclerView);
        backButton = findViewById(R.id.backButton);

        topicItems.add(new TopicItem(
                "1.1 Logarithms, Powers, Roots",
                "Logarithms are the inverse of exponentiation, used to solve for exponents. Powers involve raising a number to an exponent, and roots are the inverse of powers. These concepts are fundamental in algebra but require understanding their properties to solve efficiently.",
                "Example: Evaluate: log base 4 of 1024\nAnswer: 5 (since 4^5 = 1024)",
                "Why it's hard: Many users struggle with logarithmic properties (e.g., converting log_4(1024) to an exponent) or cannot quickly compute powers like 4^5."
        ));
        topicItems.add(new TopicItem(
                "1.2 Complex Equations and Expressions",
                "These involve solving equations with multiple terms or simplifying expressions with variables. They often require factoring, combining like terms, or applying algebraic identities.",
                "Example: Solve: (2x - 3)(x + 4) = 0\nAnswer: x = 3/2, -4",
                "Why it's hard: Users need to recall factoring techniques and solve for roots, which can be confusing if formulas are forgotten."
        ));
        topicItems.add(new TopicItem(
                "1.3 Mathematical Logic",
                "Mathematical logic involves reasoning through statements to deduce conclusions, often using principles like transitivity or set relationships. It tests analytical thinking rather than rote memorization.",
                "Example: If every A is B, and every B is C, then every A is...\nAnswer: C",
                "Why it's hard: Requires logical deduction and understanding relationships, not just applying formulas, which can be challenging for those unused to abstract thinking."
        ));
        topicItems.add(new TopicItem(
                "1.4 Probability and Combinatorics",
                "Probability measures the likelihood of events, while combinatorics deals with counting arrangements or selections. These topics use specific formulas like combinations (C) or permutations (P).",
                "Example: How many ways can you choose 2 items from 5?\nAnswer: 10",
                "Why it's hard: Users often confuse combinations (C) with permutations (P) or forget formulas like C(n,k) = n!/(k!(n-k)!)."
        ));
        topicItems.add(new TopicItem(
                "1.5 Timed Questions",
                "Simple questions become challenging under time pressure. The quiz uses a 15-second timer per question, forcing quick thinking and increasing the chance of errors.",
                "Example: Calculate: 15 * 3\nAnswer: 45",
                "Why it's hard: Even simple calculations are difficult when users panic or cannot think clearly due to the timer."
        ));

        topicRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        topicAdapter = new TopicAdapter(topicItems);
        topicRecyclerView.setAdapter(topicAdapter);

        backButton.setOnClickListener(v -> finish());
    }

    private static class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.TopicViewHolder> {
        private List<TopicItem> topicItems;

        TopicAdapter(List<TopicItem> topicItems) {
            this.topicItems = topicItems;
        }

        @Override
        public TopicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.topic_item, parent, false);
            return new TopicViewHolder(view);
        }

        @Override
        public void onBindViewHolder(TopicViewHolder holder, int position) {
            TopicItem item = topicItems.get(position);
            holder.titleText.setText(item.title);
            holder.explanationText.setText(item.explanation);
            holder.exampleText.setText(item.example);
            holder.difficultyText.setText(item.difficulty);
        }

        @Override
        public int getItemCount() {
            return topicItems.size();
        }

        static class TopicViewHolder extends RecyclerView.ViewHolder {
            TextView titleText, explanationText, exampleText, difficultyText;

            TopicViewHolder(View itemView) {
                super(itemView);
                titleText = itemView.findViewById(R.id.titleText);
                explanationText = itemView.findViewById(R.id.explanationText);
                exampleText = itemView.findViewById(R.id.exampleText);
                difficultyText = itemView.findViewById(R.id.difficultyText);
            }
        }
    }
}