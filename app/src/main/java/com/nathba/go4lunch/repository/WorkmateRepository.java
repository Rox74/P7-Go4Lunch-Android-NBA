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

    public WorkmateRepository(FirebaseFirestore firestore) {
        workmatesCollection = firestore.collection("workmates");
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
}