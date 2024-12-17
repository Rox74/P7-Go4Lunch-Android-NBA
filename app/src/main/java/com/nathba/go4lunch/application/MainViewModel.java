package com.nathba.go4lunch.application;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.nathba.go4lunch.repository.AuthRepository;
import com.nathba.go4lunch.repository.MainRepository;

/**
 * ViewModel class for managing user authentication state and navigation selection.
 * <p>
 * It acts as a bridge between the UI layer and the data layer, specifically handling
 * user authentication via {@link AuthRepository} and managing the current user data
 * through {@link MainRepository}.
 */
public class MainViewModel extends ViewModel {

    /** Repository for handling main app-related data, such as current user state. */
    private final MainRepository mainRepository;

    /** Repository for handling user authentication-related operations. */
    private final AuthRepository authRepository;

    /** LiveData object holding the current authenticated user. */
    private final LiveData<FirebaseUser> currentUser;

    /** MutableLiveData to hold the currently selected navigation item ID. */
    private final MutableLiveData<Integer> selectedNavigationItem = new MutableLiveData<>();

    /**
     * Constructor for {@link MainViewModel}.
     * <p>
     * Initializes the ViewModel with instances of {@link MainRepository} and {@link AuthRepository}.
     * Retrieves the current user state from {@link MainRepository}.
     *
     * @param mainRepository The repository responsible for managing main application data.
     * @param authRepository The repository responsible for user authentication actions.
     */
    public MainViewModel(MainRepository mainRepository, AuthRepository authRepository) {
        this.mainRepository = mainRepository;
        this.authRepository = authRepository;
        this.currentUser = mainRepository.getCurrentUser();
    }

    /**
     * Returns the current authenticated Firebase user.
     * <p>
     * Observers can subscribe to this {@link LiveData} to be notified when the user changes.
     *
     * @return LiveData containing the currently authenticated {@link FirebaseUser}.
     */
    public LiveData<FirebaseUser> getCurrentUser() {
        return currentUser;
    }

    /**
     * Returns the currently selected navigation item ID.
     * <p>
     * This value is updated when a navigation item is selected by the user.
     *
     * @return LiveData containing the selected navigation item ID.
     */
    public LiveData<Integer> getSelectedNavigationItem() {
        return selectedNavigationItem;
    }

    /**
     * Updates the selected navigation item ID.
     * <p>
     * This method updates the value in {@link MutableLiveData} to reflect the user's current selection.
     *
     * @param itemId The ID of the selected navigation item.
     */
    public void setSelectedNavigationItem(int itemId) {
        selectedNavigationItem.setValue(itemId);
    }

    /**
     * Checks the current login state of the user.
     * <p>
     * This method delegates to {@link MainRepository} to determine whether a user is authenticated.
     */
    public void checkLoginState() {
        mainRepository.checkLoginState();
    }

    /**
     * Adds the authenticated user to Firestore as a workmate.
     * <p>
     * This method uploads the user details, such as ID, name, and email, to the Firestore database.
     *
     * @param firebaseUser The authenticated {@link FirebaseUser} to be added to Firestore.
     */
    public void addWorkmateToFirestore(FirebaseUser firebaseUser) {
        mainRepository.addWorkmateToFirestore(firebaseUser);
    }

    /**
     * Signs out the current user and revokes access to the Google account.
     * <p>
     * This method delegates the sign-out logic to {@link AuthRepository}, ensuring the user
     * is fully signed out and their access is revoked.
     *
     * @param context The context required for performing sign-out operations.
     */
    public void signOut(Context context) {
        authRepository.revokeAccessAndSignOut(context);
    }
}