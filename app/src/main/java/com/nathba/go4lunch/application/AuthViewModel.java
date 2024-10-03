package com.nathba.go4lunch.application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseUser;
import com.nathba.go4lunch.repository.AuthRepository;

public class AuthViewModel extends ViewModel {

    private final AuthRepository authRepository;
    private final LiveData<FirebaseUser> userLiveData;

    public AuthViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
        this.userLiveData = authRepository.getUserLiveData();
    }

    public LiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }

    public void signInWithCredential(AuthCredential credential) {
        authRepository.signInWithCredential(credential);
    }

    public void signOut() {
        authRepository.signOut();
    }
}