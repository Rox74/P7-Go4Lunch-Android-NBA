package com.nathba.go4lunch.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.nathba.go4lunch.R;

import com.nathba.go4lunch.application.AuthViewModel;
import com.nathba.go4lunch.application.MainViewModel;
import com.nathba.go4lunch.application.ViewModelFactory;
import com.nathba.go4lunch.di.AppInjector;

/**
 * Fragment responsible for handling the Google Sign-In process.
 * Manages authentication through Google and updates the authentication state.
 */
public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";

    private GoogleSignInClient googleSignInClient;
    private AuthViewModel authViewModel;
    private MainViewModel mainViewModel;
    private ViewModelFactory viewModelFactory;

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater  The LayoutInflater object that can be used to inflate views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Obtain ViewModelFactory from AppInjector
        viewModelFactory = AppInjector.getInstance().getViewModelFactory();

        // Initialize AuthViewModel and MainViewModel using ViewModelProvider with the ViewModelFactory
        authViewModel = new ViewModelProvider(this, viewModelFactory).get(AuthViewModel.class);
        mainViewModel = new ViewModelProvider(this, viewModelFactory).get(MainViewModel.class);

        // Configure Google Sign-In options
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // The client ID obtained from Google Cloud Console
                .requestEmail() // Request email address
                .build();

        // Initialize GoogleSignInClient with the configured options
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        // Register activity result launcher for handling Google Sign-In activity results
        ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        // Handle the result of Google Sign-In
                        GoogleSignIn.getSignedInAccountFromIntent(result.getData())
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        GoogleSignInAccount account = task.getResult();
                                        if (account != null) {
                                            // Obtain Firebase authentication credentials from the Google Sign-In account
                                            AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                                            // Authenticate with Firebase
                                            authViewModel.signInWithCredential(firebaseCredential);
                                        }
                                    } else {
                                        // Log error if Google Sign-In fails
                                        Log.w(TAG, "Google Sign-In failed", task.getException());
                                    }
                                });
                    }
                });

        // Set up click listener for Google Sign-In button
        view.findViewById(R.id.google_sign_in_button).setOnClickListener(v -> {
            // Create sign-in intent and launch the sign-in activity
            Intent signInIntent = googleSignInClient.getSignInIntent();
            signInLauncher.launch(signInIntent);
        });

        // Observe the authentication state from AuthViewModel
        authViewModel.getUserLiveData().observe(getViewLifecycleOwner(), firebaseUser -> {
            if (firebaseUser != null) {
                Log.d(TAG, "User is signed in: " + firebaseUser.getEmail());

                // Navigate to the main fragment or another primary screen
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new MapViewFragment()) // Replace with your primary fragment
                        .commit();

                // Optionally clear the back stack to prevent returning to the login screen
                requireActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            } else {
                Log.d(TAG, "User is not signed in.");
            }
        });

        return view;
    }
}