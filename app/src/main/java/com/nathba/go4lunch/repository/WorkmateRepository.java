package com.nathba.go4lunch.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nathba.go4lunch.models.Workmate;

import java.util.ArrayList;
import java.util.List;

import com.google.firebase.firestore.CollectionReference;

/**
 * Repository class for managing workmate-related data in Firebase Firestore.
 * <p>
 * This class provides methods to retrieve, add, and query individual or all workmates
 * from the "workmates" collection in Firestore. Data is exposed using {@link LiveData}
 * for observation in the UI layer.
 */
public class WorkmateRepository {

    /** Reference to the "workmates" collection in Firestore. */
    private final CollectionReference workmatesCollection;

    /** Instance of Firebase Firestore for database operations. */
    private final FirebaseFirestore firestore;

    /**
     * Constructor for {@link WorkmateRepository}.
     * <p>
     * Initializes the Firestore collection reference for managing workmate data.
     *
     * @param firestore The {@link FirebaseFirestore} instance used for Firestore operations.
     */
    public WorkmateRepository(FirebaseFirestore firestore) {
        workmatesCollection = firestore.collection("workmates");
        this.firestore = firestore;
    }

    /**
     * Retrieves a list of all workmates from the Firestore "workmates" collection.
     * <p>
     * This method listens for real-time updates and returns the data as {@link LiveData}.
     *
     * @return A {@link LiveData} object containing a list of {@link Workmate} objects.
     */
    public LiveData<List<Workmate>> getWorkmates() {
        MutableLiveData<List<Workmate>> workmatesLiveData = new MutableLiveData<>();
        workmatesCollection.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                return; // Handle error silently for now
            }
            if (snapshots != null) {
                List<Workmate> workmates = new ArrayList<>();
                for (DocumentSnapshot document : snapshots.getDocuments()) {
                    Workmate workmate = document.toObject(Workmate.class);
                    if (workmate != null) {
                        workmates.add(workmate);
                    }
                }
                workmatesLiveData.setValue(workmates);
            }
        });
        return workmatesLiveData;
    }

    /**
     * Adds a workmate to the Firestore "workmates" collection.
     * <p>
     * The workmate's ID is used as the document ID to ensure uniqueness.
     *
     * @param workmate The {@link Workmate} object to be added.
     */
    public void addWorkmate(Workmate workmate) {
        workmatesCollection.document(workmate.getWorkmateId()).set(workmate);
    }

    /**
     * Retrieves a specific workmate by their ID from the Firestore "workmates" collection.
     *
     * @param workmateId The unique ID of the workmate to retrieve.
     * @return A {@link LiveData} object containing the requested {@link Workmate}, or {@code null} if not found.
     */
    public LiveData<Workmate> getWorkmateById(String workmateId) {
        MutableLiveData<Workmate> workmateLiveData = new MutableLiveData<>();

        firestore.collection("workmates")
                .document(workmateId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Workmate workmate = documentSnapshot.toObject(Workmate.class);
                        workmateLiveData.setValue(workmate);
                    } else {
                        workmateLiveData.setValue(null);
                    }
                })
                .addOnFailureListener(e -> workmateLiveData.setValue(null));

        return workmateLiveData;
    }
}