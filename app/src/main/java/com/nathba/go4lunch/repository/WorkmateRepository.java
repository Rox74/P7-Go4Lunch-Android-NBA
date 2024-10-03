package com.nathba.go4lunch.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nathba.go4lunch.models.Workmate;

import java.util.ArrayList;
import java.util.List;

import com.google.firebase.firestore.CollectionReference;

public class WorkmateRepository {

    private final CollectionReference workmatesCollection;
    private final FirebaseFirestore firestore;

    public WorkmateRepository(FirebaseFirestore firestore) {
        workmatesCollection = firestore.collection("workmates");
        this.firestore = firestore;
    }

    public LiveData<List<Workmate>> getWorkmates() {
        MutableLiveData<List<Workmate>> workmatesLiveData = new MutableLiveData<>();
        workmatesCollection.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                return;
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

    public void addWorkmate(Workmate workmate) {
        workmatesCollection.document(workmate.getWorkmateId()).set(workmate);
    }

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