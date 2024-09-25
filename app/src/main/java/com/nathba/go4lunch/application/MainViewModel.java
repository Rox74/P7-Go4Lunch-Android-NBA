package com.nathba.go4lunch.application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nathba.go4lunch.models.Workmate;
import com.nathba.go4lunch.repository.MainRepository;

public class MainViewModel extends ViewModel {

    private final MainRepository mainRepository;
    private final LiveData<FirebaseUser> currentUser;
    private final MutableLiveData<Integer> selectedNavigationItem = new MutableLiveData<>();

    public MainViewModel(MainRepository mainRepository) {
        this.mainRepository = mainRepository;
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

    public void signOut() {
        mainRepository.signOut();
    }

    public void checkLoginState() {
        mainRepository.checkLoginState();
    }

    public void addWorkmateToFirestore(FirebaseUser firebaseUser) {
        mainRepository.addWorkmateToFirestore(firebaseUser);
    }
}