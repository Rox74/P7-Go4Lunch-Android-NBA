package com.nathba.go4lunch.application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * ViewModel class for handling authentication-related tasks.
 * It manages Firebase authentication operations and provides
 * live data for observing authentication state changes.
 */
public class AuthViewModel extends ViewModel {

    // Firebase Authentication instance
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    // LiveData to hold the current authenticated user
    private final MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();

    /**
     * Constructor for AuthViewModel.
     * Initializes the ViewModel and checks if there's a currently logged-in user.
     * If so, it updates the LiveData with the current user.
     */
    public AuthViewModel() {
        if (mAuth.getCurrentUser() != null) {
            userLiveData.setValue(mAuth.getCurrentUser());
        }
    }

    /**
     * Returns LiveData object that holds the current authenticated user.
     * Observers can subscribe to this LiveData to receive updates on the authentication state.
     *
     * @return LiveData<FirebaseUser> - LiveData containing the current user or null if not authenticated.
     */
    public LiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }

    /**
     * Signs in a user with the provided authentication credential.
     * On successful sign-in, updates the LiveData with the authenticated user.
     * On failure, sets the LiveData value to null.
     *
     * @param credential The AuthCredential used for signing in the user.
     */
    public void signInWithCredential(AuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Sign-in was successful, update LiveData with the current user
                userLiveData.setValue(mAuth.getCurrentUser());
            } else {
                // Sign-in failed, clear the LiveData
                userLiveData.setValue(null);
            }
        });
    }

    /**
     * Signs out the current user and clears the LiveData.
     * The LiveData will be set to null, indicating that no user is authenticated.
     */
    public void signOut() {
        mAuth.signOut();
        userLiveData.setValue(null);
    }
}