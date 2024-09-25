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

public class LunchRepository {
    private final CollectionReference lunchesCollection;

    public LunchRepository(FirebaseFirestore firestore) {
        lunchesCollection = firestore.collection("lunches");
    }

    public LiveData<List<Lunch>> getLunches() {
        MutableLiveData<List<Lunch>> lunchesLiveData = new MutableLiveData<>();
        lunchesCollection.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                return;
            }
            if (snapshots != null) {
                List<Lunch> lunches = new ArrayList<>();
                for (DocumentSnapshot document : snapshots.getDocuments()) {
                    Lunch lunch = document.toObject(Lunch.class);
                    if (lunch != null) {
                        lunches.add(lunch);
                    }
                }
                lunchesLiveData.setValue(lunches);
            }
        });
        return lunchesLiveData;
    }

    public void addLunch(Lunch lunch) {
        lunchesCollection.document(lunch.getLunchId()).set(lunch);
    }
}