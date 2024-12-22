package com.example.navixpassanger.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.navixpassanger.R;
import com.example.navixpassanger.auth.LoginActivity;
import com.example.navixpassanger.auth.SignUpActivity;
import com.example.navixpassanger.onboard.NavigationActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ParentActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private MaterialButton login, signup;
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String FIRST_TIME_KEY = "my_first_time";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if it's first time launch
        if (isFirstTimeLaunch()) {
            // Start onboarding
            startActivity(new Intent(this, NavigationActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_parent);
        initializeViews();
        setupClickListeners();
    }

    @Override
    public void onStart() {
        super.onStart();
        checkUserLoginStatus();
    }

    private void clearAppData() {
        try {
            // Clear app data on fresh install
            String packageName = getApplicationContext().getPackageName();
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("pm clear " + packageName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isFirstTimeLaunch() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        if (settings.getBoolean(FIRST_TIME_KEY, true)) {
            // First time - mark it as launched
            settings.edit().putBoolean(FIRST_TIME_KEY, false).apply();
            return true;
        }
        return false;
    }

    private void checkUserLoginStatus() {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            startActivity(new Intent(this, HomeScreen.class));
            finish();
        }
    }

    private void initializeViews() {
        login = findViewById(R.id.login);
        signup = findViewById(R.id.signup);
    }

    private void setupClickListeners() {
        login.setOnClickListener(v ->
                startActivity(new Intent(ParentActivity.this, LoginActivity.class))
        );

        signup.setOnClickListener(v ->
                startActivity(new Intent(ParentActivity.this, SignUpActivity.class))
        );
    }

    // Add this method to manually clear preferences if needed
    private void clearPreferences() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        settings.edit().clear().apply();
    }

    // Add this method to manually sign out if needed
    private void signOut() {
        if (mAuth != null) {
            mAuth.signOut();
        }
    }
}