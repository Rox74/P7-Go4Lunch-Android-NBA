package com.nathba.go4lunch.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.nathba.go4lunch.R;
import com.nathba.go4lunch.application.AuthViewModel;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        TextView welcomeTextView = findViewById(R.id.welcome_text_view);
        Button signOutButton = findViewById(R.id.sign_out_button);

        authViewModel.getUserLiveData().observe(this, firebaseUser -> {
            if (firebaseUser != null) {
                String welcomeMessage = "Welcome, " + firebaseUser.getDisplayName();
                welcomeTextView.setText(welcomeMessage);
            } else {
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                finish();
            }
        });

        signOutButton.setOnClickListener(v -> {
            authViewModel.signOut();
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        });
    }
}