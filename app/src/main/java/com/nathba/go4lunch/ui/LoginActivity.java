package com.nathba.go4lunch.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.nathba.go4lunch.R;

import com.google.firebase.auth.FirebaseUser;
import com.nathba.go4lunch.application.AuthViewModel;

/**
 * LoginActivity handles the login functionality of the application,
 * including Google Sign-In integration.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private GoogleSignInClient googleSignInClient;
    private AuthViewModel authViewModel;

    /**
     * Called when the activity is first created. This is where you should do all of your normal static set up:
     * create views, bind data to lists, etc. This method also provides you with a Bundle containing the activity's
     * previously frozen state, if there was one.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle). Note: Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize the AuthViewModel to handle authentication operations
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Configure Google Sign-In options
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Initialize the GoogleSignInClient with the options specified by gso
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Hide the ActionBar if it is present
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize the sign-in launcher to handle the result of the sign-in intent
        ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Handle the result of the sign-in activity
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        GoogleSignIn.getSignedInAccountFromIntent(result.getData())
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // Get the signed-in Google account
                                        GoogleSignInAccount account = task.getResult();
                                        if (account != null) {
                                            // Get an AuthCredential for Firebase authentication using the Google account token
                                            AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                                            // Use the AuthViewModel to sign in with the credential
                                            authViewModel.signInWithCredential(firebaseCredential);
                                        }
                                    } else {
                                        // Log a warning if Google Sign-In failed
                                        Log.w(TAG, "Google Sign-In failed", task.getException());
                                    }
                                });
                    }
                });

        // Set up the sign-in button click listener to launch the Google Sign-In intent
        findViewById(R.id.google_sign_in_button).setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            signInLauncher.launch(signInIntent);
        });

        // Observe the authentication state and update UI when the user is signed in
        authViewModel.getUserLiveData().observe(this, firebaseUser -> {
            if (firebaseUser != null) {
                updateUI(firebaseUser);
            }
        });
    }

    /**
     * Updates the UI and starts the MainActivity when the user is authenticated.
     *
     * @param user The authenticated Firebase user.
     */
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // Start the MainActivity and finish the LoginActivity
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }
}