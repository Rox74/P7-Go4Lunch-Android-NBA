package com.nathba.go4lunch.application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthViewModel extends ViewModel {
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();

    public AuthViewModel() {
        if (mAuth.getCurrentUser() != null) {
            userLiveData.setValue(mAuth.getCurrentUser());
        }
    }

    public LiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }

    public void signInWithCredential(AuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userLiveData.setValue(mAuth.getCurrentUser());
            } else {
                userLiveData.setValue(null);
            }
        });
    }

    public void signOut() {
        mAuth.signOut();
        userLiveData.setValue(null);
    }
}
