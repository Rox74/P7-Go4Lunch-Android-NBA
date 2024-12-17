package com.nathba.go4lunch.application;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.nathba.go4lunch.repository.AuthRepository;
import com.nathba.go4lunch.repository.MainRepository;

public class MainViewModel extends ViewModel {

    private final MainRepository mainRepository;
    private final AuthRepository authRepository;
    private final LiveData<FirebaseUser> currentUser;
    private final MutableLiveData<Integer> selectedNavigationItem = new MutableLiveData<>();

    public MainViewModel(MainRepository mainRepository, AuthRepository authRepository) {
        this.mainRepository = mainRepository;
        this.authRepository = authRepository;
        this.currentUser = mainRepository.getCurrentUser();
    }

    public LiveData<FirebaseUser> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Integer> getSelectedNavigationItem() {
        return selectedNavigationItem;
    }

    public void setSelectedNavigationItem(int itemId) {
        selectedNavigationItem.setValue(itemId);
    }

    public void checkLoginState() {
        Log.d("MainViewModel", "Checking login state...");
        mainRepository.checkLoginState();
    }

    public void addWorkmateToFirestore(FirebaseUser firebaseUser) {
        mainRepository.addWorkmateToFirestore(firebaseUser);
    }

    public void signOut(Context context) {
        Log.d("MainViewModel", "Triggering full sign-out...");
        authRepository.revokeAccessAndSignOut(context);
    }
}