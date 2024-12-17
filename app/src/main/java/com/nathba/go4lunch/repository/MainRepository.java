package com.nathba.go4lunch.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nathba.go4lunch.models.Workmate;

/**
 * Repository class for managing the main user authentication state and workmate data.
 * <p>
 * This class observes Firebase authentication state changes, provides access to the current user,
 * and allows adding user information to Firestore when needed.
 */
public class MainRepository {

    /** Firebase Authentication instance for managing user login state. */
    private final FirebaseAuth firebaseAuth;

    /** LiveData object that holds the current authenticated user. */
    private final MutableLiveData<FirebaseUser> currentUser = new MutableLiveData<>();

    /**
     * Constructor for {@link MainRepository}.
     * <p>
     * Initializes the authentication listener to observe changes in the login state
     * and updates the {@link #currentUser} LiveData accordingly.
     *
     * @param firebaseAuth The {@link FirebaseAuth} instance for authentication operations.
     */
    public MainRepository(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;

        // Listen for authentication state changes
        firebaseAuth.addAuthStateListener(auth -> {
            FirebaseUser user = auth.getCurrentUser();
            currentUser.postValue(user); // Update the LiveData with the current user
            Log.d("MainRepository", "Auth state changed. User: " + (user != null ? user.getEmail() : "null"));
        });

        // Initialize the LiveData with the current user, if any
        this.currentUser.setValue(firebaseAuth.getCurrentUser());
    }

    /**
     * Retrieves the current authenticated user as a {@link LiveData}.
     * <p>
     * Observers can subscribe to this LiveData to receive updates when the authentication state changes.
     *
     * @return A {@link LiveData} object containing the current {@link FirebaseUser}, or {@code null} if no user is logged in.
     */
    public LiveData<FirebaseUser> getCurrentUser() {
        return currentUser;
    }

    /**
     * Checks the current login state and updates the {@link #currentUser} LiveData.
     * <p>
     * This method manually refreshes the authentication state and ensures observers
     * are notified of any changes.
     */
    public void checkLoginState() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        currentUser.setValue(user);
    }

    /**
     * Adds the authenticated user's information to the "workmates" collection in Firestore.
     * <p>
     * The method extracts user details (ID, name, email, and photo URL) from the {@link FirebaseUser}
     * and saves them as a {@link Workmate} object in Firestore.
     *
     * @param firebaseUser The currently authenticated {@link FirebaseUser}.
     */
    public void addWorkmateToFirestore(FirebaseUser firebaseUser) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = firebaseUser.getUid();
        String name = firebaseUser.getDisplayName();
        String email = firebaseUser.getEmail();
        String photoUrl = firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null;

        // Create a Workmate object with user details
        Workmate workmate = new Workmate(userId, name, email, photoUrl);

        // Add or update the workmate document in the "workmates" collection
        db.collection("workmates")
                .document(userId)
                .set(workmate)
                .addOnSuccessListener(aVoid -> Log.d("MainRepository", "Workmate added to Firestore"))
                .addOnFailureListener(e -> Log.e("MainRepository", "Error adding workmate to Firestore", e));
    }
}