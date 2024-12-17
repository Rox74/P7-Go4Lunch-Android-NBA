package com.nathba.go4lunch.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nathba.go4lunch.models.Workmate;

public class MainRepository {

    private final FirebaseAuth firebaseAuth;
    private final MutableLiveData<FirebaseUser> currentUser = new MutableLiveData<>();

    public MainRepository(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;

        // Observer les changements d'état de connexion
        firebaseAuth.addAuthStateListener(auth -> {
            FirebaseUser user = auth.getCurrentUser();
            currentUser.postValue(user); // Met à jour le LiveData
            Log.d("MainRepository", "Auth state changed. User: " + (user != null ? user.getEmail() : "null"));
        });

        // Initialiser la valeur actuelle
        this.currentUser.setValue(firebaseAuth.getCurrentUser());
    }

    public LiveData<FirebaseUser> getCurrentUser() {
        return currentUser;
    }

    public void checkLoginState() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        currentUser.setValue(user);
    }

    public void addWorkmateToFirestore(FirebaseUser firebaseUser) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = firebaseUser.getUid();
        String name = firebaseUser.getDisplayName();
        String email = firebaseUser.getEmail();
        String photoUrl = firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null;

        Workmate workmate = new Workmate(userId, name, email, photoUrl);

        db.collection("workmates")
                .document(userId)
                .set(workmate)
                .addOnSuccessListener(aVoid -> Log.d("MainRepository", "Workmate added to Firestore"))
                .addOnFailureListener(e -> Log.e("MainRepository", "Error adding workmate to Firestore", e));
    }
}