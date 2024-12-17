package com.nathba.go4lunch.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nathba.go4lunch.R;

/**
 * Repository class for managing authentication-related operations using Firebase Authentication.
 * <p>
 * This class handles user sign-in with credentials, revoking access, and maintaining the authentication
 * state via LiveData, allowing the UI to observe changes reactively.
 */
public class AuthRepository {

    /** Instance of {@link FirebaseAuth} for handling authentication operations. */
    private final FirebaseAuth firebaseAuth;

    /** LiveData object that holds the current authenticated user. */
    private final MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();

    /**
     * Constructor for {@link AuthRepository}.
     * <p>
     * Initializes Firebase Authentication and sets the current user in {@link #userLiveData}
     * if a user is already signed in.
     *
     * @param firebaseAuth The {@link FirebaseAuth} instance used for authentication operations.
     */
    public AuthRepository(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
        if (firebaseAuth.getCurrentUser() != null) {
            userLiveData.setValue(firebaseAuth.getCurrentUser());
        }
    }

    /**
     * Returns a {@link LiveData} object that holds the current authenticated user.
     * <p>
     * The UI can observe this LiveData to reactively update when the authentication state changes.
     *
     * @return LiveData containing the current {@link FirebaseUser}, or {@code null} if no user is signed in.
     */
    public LiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }

    /**
     * Signs in a user using the provided {@link AuthCredential}.
     * <p>
     * Upon successful sign-in, the authenticated user is posted to {@link #userLiveData}.
     * If the sign-in fails, {@code null} is posted to indicate failure.
     *
     * @param credential The {@link AuthCredential} used for signing in (e.g., Google credentials).
     */
    public void signInWithCredential(AuthCredential credential) {
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                userLiveData.postValue(user);
            } else {
                userLiveData.postValue(null);
            }
        });
    }

    /**
     * Revokes Google access and signs the user out from Firebase Authentication.
     * <p>
     * This method first revokes the user's Google account access using {@link GoogleSignInClient}
     * and then signs the user out from Firebase. It updates {@link #userLiveData} to reflect
     * that no user is currently signed in.
     *
     * @param context The application context required to initialize the GoogleSignInClient.
     */
    public void revokeAccessAndSignOut(Context context) {
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(context,
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(context.getString(R.string.default_web_client_id)) // Web client ID from resources
                        .requestEmail()
                        .build()
        );

        // Revoke Google access and sign out from Firebase
        googleSignInClient.revokeAccess().addOnCompleteListener(task -> {

            // Sign out from Firebase
            firebaseAuth.signOut();

            // Update LiveData to notify observers
            userLiveData.postValue(null);
        });
    }
}