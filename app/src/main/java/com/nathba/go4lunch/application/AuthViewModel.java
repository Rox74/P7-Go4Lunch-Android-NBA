package com.nathba.go4lunch.application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * AuthViewModel is responsible for managing user authentication state
 * using Firebase Authentication. It provides methods for signing in and
 * signing out, and exposes the current user as LiveData.
 */
public class AuthViewModel extends ViewModel {

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();

    /**
     * Constructor initializes the AuthViewModel and sets the current user
     * if already authenticated.
     */
    public AuthViewModel() {
        // Check if there is a current authenticated user and set it to userLiveData
        if (mAuth.getCurrentUser() != null) {
            userLiveData.setValue(mAuth.getCurrentUser());
        }
    }

    /**
     * Returns a LiveData object that contains the current authenticated FirebaseUser.
     *
     * @return LiveData<FirebaseUser> representing the current authenticated user.
     */
    public LiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }

    /**
     * Signs in a user using the provided AuthCredential. Updates the userLiveData
     * with the authenticated user or null if the sign-in fails.
     *
     * @param credential The AuthCredential object containing the user's sign-in credentials.
     */
    public void signInWithCredential(AuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Sign-in succeeded, update userLiveData with the current user
                userLiveData.setValue(mAuth.getCurrentUser());
            } else {
                // Sign-in failed, update userLiveData with null
                userLiveData.setValue(null);
            }
        });
    }

    /**
     * Signs out the current authenticated user and updates the userLiveData with null.
     */
    public void signOut() {
        // Sign out the current user from FirebaseAuth
        mAuth.signOut();
        // Update userLiveData with null as the user is signed out
        userLiveData.setValue(null);
    }
}