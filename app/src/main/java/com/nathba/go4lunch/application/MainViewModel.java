package com.nathba.go4lunch.application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nathba.go4lunch.models.Workmate;

/**
 * ViewModel class for managing main activities in the application.
 * It handles user authentication state, navigation selection, and interaction with Firestore.
 */
public class MainViewModel extends ViewModel {

    // LiveData to hold the current authenticated user
    private final MutableLiveData<FirebaseUser> currentUser = new MutableLiveData<>();

    // LiveData to hold the currently selected navigation item
    private final MutableLiveData<Integer> selectedNavigationItem = new MutableLiveData<>();

    // Firebase Authentication instance
    private final FirebaseAuth firebaseAuth;

    /**
     * Constructor for MainViewModel.
     * Initializes FirebaseAuth and sets the current user LiveData based on the currently authenticated user.
     */
    public MainViewModel() {
        firebaseAuth = FirebaseAuth.getInstance(); // Initialize FirebaseAuth instance
        currentUser.setValue(firebaseAuth.getCurrentUser()); // Set current user if logged in
    }

    /**
     * Returns LiveData object containing the current authenticated user.
     * Observers can subscribe to this LiveData to receive updates on the authentication state.
     *
     * @return LiveData<FirebaseUser> - LiveData containing the current user or null if not authenticated.
     */
    public LiveData<FirebaseUser> getCurrentUser() {
        return currentUser;
    }

    /**
     * Returns LiveData object containing the currently selected navigation item ID.
     * Observers can subscribe to this LiveData to receive updates on the selected navigation item.
     *
     * @return LiveData<Integer> - LiveData containing the ID of the currently selected navigation item.
     */
    public LiveData<Integer> getSelectedNavigationItem() {
        return selectedNavigationItem;
    }

    /**
     * Updates the currently selected navigation item ID.
     *
     * @param itemId The ID of the navigation item to select.
     */
    public void setSelectedNavigationItem(int itemId) {
        selectedNavigationItem.setValue(itemId);
    }

    /**
     * Signs out the current user and updates the LiveData to reflect the sign-out state.
     * The current user LiveData will be set to null after sign-out.
     */
    public void signOut() {
        firebaseAuth.signOut(); // Sign out the current user
        currentUser.setValue(null); // Update LiveData to reflect sign-out
    }

    /**
     * Checks the current login state and updates the LiveData with the current user.
     * If no user is authenticated, the LiveData will be set to null.
     */
    public void checkLoginState() {
        FirebaseUser user = firebaseAuth.getCurrentUser(); // Get current user
        currentUser.setValue(user); // Update LiveData with current user or null
    }

    /**
     * Adds the given FirebaseUser to Firestore as a workmate.
     * The workmate's details are stored in the "workmates" collection with the user's ID as the document ID.
     *
     * @param firebaseUser The FirebaseUser to be added to Firestore.
     */
    public void addWorkmateToFirestore(FirebaseUser firebaseUser) {
        FirebaseFirestore db = FirebaseFirestore.getInstance(); // Get Firestore instance

        // Extract user details
        String userId = firebaseUser.getUid();
        String name = firebaseUser.getDisplayName();
        String email = firebaseUser.getEmail();
        String photoUrl = (firebaseUser.getPhotoUrl() != null) ? firebaseUser.getPhotoUrl().toString() : null;

        // Create a Workmate object
        Workmate workmate = new Workmate(userId, name, email, photoUrl);

        // Add the Workmate to Firestore
        db.collection("workmates")
                .document(userId)
                .set(workmate)
                .addOnSuccessListener(aVoid -> {
                    // Handle success (e.g., notify user or log success)
                })
                .addOnFailureListener(e -> {
                    // Handle failure (e.g., notify user or log error)
                });
    }
}