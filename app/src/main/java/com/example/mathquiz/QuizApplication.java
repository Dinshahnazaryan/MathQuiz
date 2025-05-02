package com.example.mathquiz;

import android.app.Application;
import android.util.Log;
import com.google.firebase.FirebaseApp;

public class QuizApplication extends Application {
    private static final String TAG = "QuizApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            FirebaseApp.initializeApp(this);
            Log.d(TAG, "Firebase initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Firebase: " + e.getMessage(), e);
        }
    }
}