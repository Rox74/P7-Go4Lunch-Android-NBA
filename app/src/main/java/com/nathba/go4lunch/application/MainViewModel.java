package com.nathba.go4lunch.application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainViewModel extends ViewModel {
    private final MutableLiveData<FirebaseUser> currentUser = new MutableLiveData<>();
    private final MutableLiveData<Integer> selectedNavigationItem = new MutableLiveData<>();
    private final FirebaseAuth firebaseAuth;

    public MainViewModel() {
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser.setValue(firebaseAuth.getCurrentUser());
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

    public void signOut() {
        firebaseAuth.signOut();
        currentUser.setValue(null);
    }

    public void checkLoginState() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        currentUser.setValue(user);
    }
}