package com.example.mathquiz;

import android.app.Application;
import com.google.firebase.FirebaseApp;

public class QuizApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
}