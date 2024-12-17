package com.nathba.go4lunch.application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseUser;
import com.nathba.go4lunch.repository.AuthRepository;

/**
 * ViewModel class responsible for managing user authentication data and operations.
 * <p>
 * It communicates with the {@link AuthRepository} to perform authentication-related tasks
 * and exposes the authentication state to the UI as {@link LiveData}.
 */
public class AuthViewModel extends ViewModel {

    /** Repository for handling authentication-related logic and Firebase operations. */
    private final AuthRepository authRepository;

    /** LiveData object containing the current authenticated user, if any. */
    private final LiveData<FirebaseUser> userLiveData;

    /**
     * Constructor for {@link AuthViewModel}.
     * <p>
     * Initializes the ViewModel with an instance of {@link AuthRepository} and retrieves the
     * current user LiveData for observation.
     *
     * @param authRepository The repository responsible for handling authentication operations.
     */
    public AuthViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
        this.userLiveData = authRepository.getUserLiveData();
    }

    /**
     * Returns a {@link LiveData} object containing the current authenticated Firebase user.
     * <p>
     * Observers can subscribe to this LiveData to receive updates when the authentication state changes.
     *
     * @return LiveData containing the current authenticated {@link FirebaseUser}.
     */
    public LiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }

    /**
     * Initiates the sign-in process using the provided {@link AuthCredential}.
     * <p>
     * This method delegates the authentication operation to the {@link AuthRepository}, which
     * handles Firebase authentication logic.
     *
     * @param credential The {@link AuthCredential} obtained from a sign-in provider (e.g., Google).
     */
    public void signInWithCredential(AuthCredential credential) {
        authRepository.signInWithCredential(credential);
    }
}