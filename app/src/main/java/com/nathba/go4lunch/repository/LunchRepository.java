package com.nathba.go4lunch.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nathba.go4lunch.models.Lunch;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for managing lunch data in Firestore.
 * Provides methods to retrieve and add lunch records.
 */
public class LunchRepository {

    // Reference to the "lunches" collection in Firestore
    private final CollectionReference lunchesCollection;

    /**
     * Constructs a LunchRepository instance.
     * Initializes the Firestore database reference for the "lunches" collection.
     */
    public LunchRepository() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        lunchesCollection = db.collection("lunches");
    }

    /**
     * Gets a LiveData object containing a list of lunches from Firestore.
     * Listens for real-time updates to the "lunches" collection.
     *
     * @return LiveData containing a list of Lunch objects
     */
    public LiveData<List<Lunch>> getLunches() {
        MutableLiveData<List<Lunch>> lunchesLiveData = new MutableLiveData<>();

        // Add a snapshot listener to listen for changes in the "lunches" collection
        lunchesCollection.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                // Log error if there's an issue with the snapshot retrieval
                Log.e("LunchRepository", "Error retrieving lunches", e);
                return;
            }
            if (snapshots != null) {
                List<Lunch> lunches = new ArrayList<>();
                // Iterate over each document in the snapshot
                for (DocumentSnapshot document : snapshots.getDocuments()) {
                    // Convert the document to a Lunch object
                    Lunch lunch = document.toObject(Lunch.class);
                    if (lunch != null) {
                        lunches.add(lunch);
                    }
                }
                // Update the LiveData with the list of lunches
                lunchesLiveData.setValue(lunches);
            }
        });

        return lunchesLiveData;
    }

    /**
     * Adds a lunch record to Firestore.
     * The lunch record is stored under its unique ID.
     *
     * @param lunch the Lunch object to be added
     */
    public void addLunch(Lunch lunch) {
        // Set the lunch document in the Firestore collection using its ID
        lunchesCollection.document(lunch.getLunchId()).set(lunch);
    }
}