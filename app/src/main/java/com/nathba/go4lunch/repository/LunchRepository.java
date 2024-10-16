package com.nathba.go4lunch.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nathba.go4lunch.models.Lunch;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class LunchRepository {

    private final CollectionReference lunchesCollection;

    public LunchRepository(FirebaseFirestore firestore) {
        lunchesCollection = firestore.collection("lunches");
    }

    // Récupérer les lunchs depuis Firebase
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

    // Ajouter un lunch dans Firebase
    public void addLunch(Lunch lunch) {
        lunchesCollection.document(lunch.getLunchId()).set(lunch);
    }

    // Récupérer les lunchs pour un restaurant spécifique aujourd'hui
    public LiveData<List<Lunch>> getLunchesForRestaurantToday(String restaurantId) {
        MutableLiveData<List<Lunch>> lunchesLiveData = new MutableLiveData<>();
        Date today = getToday();

        lunchesCollection
                .whereEqualTo("restaurantId", restaurantId)
                .whereGreaterThanOrEqualTo("date", today)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Lunch> lunches = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Lunch lunch = document.toObject(Lunch.class);
                            lunches.add(lunch);
                        }
                        lunchesLiveData.setValue(lunches);
                    } else {
                        lunchesLiveData.setValue(null);
                    }
                });

        return lunchesLiveData;
    }

    // Obtenir la date d'aujourd'hui sans l'heure
    private Date getToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}