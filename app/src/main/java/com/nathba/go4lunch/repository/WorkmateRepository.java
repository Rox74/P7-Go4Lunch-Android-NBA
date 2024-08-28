package com.nathba.go4lunch.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nathba.go4lunch.models.Workmate;

import java.util.ArrayList;
import java.util.List;

import com.google.firebase.firestore.CollectionReference;

/**
 * Repository class for managing workmate data in Firestore.
 * Provides methods to add and retrieve workmate records.
 */
public class WorkmateRepository {

    // Name of the Firestore collection where workmates are stored
    private static final String COLLECTION_WORKMATES = "workmates";

    // Firestore instance and reference to the "workmates" collection
    private final FirebaseFirestore firestore;
    private final CollectionReference workmatesCollection;

    /**
     * Constructs a WorkmateRepository instance.
     * Initializes the Firestore database reference for the "workmates" collection.
     */
    public WorkmateRepository() {
        firestore = FirebaseFirestore.getInstance();
        workmatesCollection = firestore.collection(COLLECTION_WORKMATES);
    }

    /**
     * Adds a workmate record to Firestore.
     * The workmate record is stored under its unique ID.
     *
     * @param workmate the Workmate object to be added
     */
    public void addWorkmate(Workmate workmate) {
        workmatesCollection.document(workmate.getWorkmateId())
                .set(workmate)
                .addOnSuccessListener(aVoid -> {
                    // Log success message when the workmate is added successfully
                    Log.d("WorkmateRepository", "Workmate ajouté avec succès");
                })
                .addOnFailureListener(e -> {
                    // Log error message if there's an issue adding the workmate
                    Log.e("WorkmateRepository", "Erreur lors de l'ajout du workmate", e);
                });
    }

    /**
     * Gets a LiveData object containing a list of workmates from Firestore.
     * Listens for real-time updates to the "workmates" collection, excluding the current user.
     *
     * @return LiveData containing a list of Workmate objects
     */
    public LiveData<List<Workmate>> getWorkmates() {
        MutableLiveData<List<Workmate>> workmatesLiveData = new MutableLiveData<>();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Add a snapshot listener to listen for changes in the "workmates" collection
        workmatesCollection.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                // Log error if there's an issue with the snapshot retrieval
                Log.e("WorkmateRepository", "Erreur lors de la récupération des workmates", e);
                return;
            }
            if (snapshots != null) {
                List<Workmate> workmates = new ArrayList<>();
                // Iterate over each document in the snapshot
                for (DocumentSnapshot document : snapshots.getDocuments()) {
                    Workmate workmate = document.toObject(Workmate.class);
                    // Add workmate to the list if it's not the current user
                    if (workmate != null && !workmate.getWorkmateId().equals(currentUser.getUid())) {
                        workmates.add(workmate);
                    }
                }
                // Update the LiveData with the list of workmates
                workmatesLiveData.setValue(workmates);
            }
        });

        return workmatesLiveData;
    }
}