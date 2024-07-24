package com.nathba.go4lunch.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.nathba.go4lunch.R;

import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign-In options
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize the sign-in launcher
        ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        GoogleSignIn.getSignedInAccountFromIntent(result.getData())
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        GoogleSignInAccount account = task.getResult();
                                        if (account != null) {
                                            AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                                            mAuth.signInWithCredential(firebaseCredential)
                                                    .addOnCompleteListener(this, authTask -> {
                                                        if (authTask.isSuccessful()) {
                                                            Log.d(TAG, "signInWithCredential:success");
                                                            updateUI(mAuth.getCurrentUser());
                                                        } else {
                                                            Log.w(TAG, "signInWithCredential:failure", authTask.getException());
                                                            updateUI(null);
                                                        }
                                                    });
                                        }
                                    } else {
                                        Log.w(TAG, "Google Sign-In failed", task.getException());
                                    }
                                });
                    }
                });

        // Set up the sign-in button click listener
        findViewById(R.id.google_sign_in_button).setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            signInLauncher.launch(signInIntent);
        });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }
}