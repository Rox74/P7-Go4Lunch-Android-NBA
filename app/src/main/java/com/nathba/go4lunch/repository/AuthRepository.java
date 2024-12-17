package com.nathba.go4lunch.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nathba.go4lunch.R;

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
                FirebaseUser user = firebaseAuth.getCurrentUser();
                userLiveData.postValue(user);
            } else {
                userLiveData.postValue(null);
            }
        });
    }

    public void revokeAccessAndSignOut(Context context) {
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(context,
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(context.getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build()
        );

        // Révoquer complètement l'accès Google
        googleSignInClient.revokeAccess().addOnCompleteListener(task -> {

            // Déconnexion Firebase
            firebaseAuth.signOut();

            // Forcer la mise à jour de LiveData
            userLiveData.postValue(null);
        });
    }
}