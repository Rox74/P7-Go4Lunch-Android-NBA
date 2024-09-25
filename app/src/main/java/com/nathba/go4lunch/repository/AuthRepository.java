package com.nathba.go4lunch.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthRepository {

    private final FirebaseAuth firebaseAuth;
    private final MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();

    public AuthRepository(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
        if (firebaseAuth.getCurrentUser() != null) {
            userLiveData.setValue(firebaseAuth.getCurrentUser());
        }
    }

    public LiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }

    public void signInWithCredential(AuthCredential credential) {
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userLiveData.setValue(firebaseAuth.getCurrentUser());
            } else {
                userLiveData.setValue(null);
            }
        });
    }

    public void signOut() {
        firebaseAuth.signOut();
        userLiveData.setValue(null);
    }
}