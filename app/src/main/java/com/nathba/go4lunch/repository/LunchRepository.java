package com.nathba.go4lunch.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
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

    // Méthode mise à jour pour obtenir la date cible en fonction de l'heure
    private Date getToday() {
        Calendar calendar = Calendar.getInstance();

        // Vérifier l'heure actuelle
        if (calendar.get(Calendar.HOUR_OF_DAY) >= 12) {
            // Si après 12h, obtenir la date de demain
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Réinitialiser l'heure pour obtenir uniquement la date sans l'heure
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    /**
     * Supprime les lunchs de l'utilisateur pour une date donnée afin d'éviter les doublons.
     *
     * @param workmateId L'identifiant de l'utilisateur.
     * @param date       La date du lunch à supprimer.
     * @return Une tâche qui complète la suppression.
     */
    public Task<Void> deleteUserLunchForDate(String workmateId, Date date) {
        return lunchesCollection
                .whereEqualTo("workmateId", workmateId)
                .whereEqualTo("date", date)
                .get()
                .continueWithTask(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        WriteBatch batch = lunchesCollection.getFirestore().batch();
                        for (DocumentSnapshot document : task.getResult()) {
                            batch.delete(document.getReference());
                        }
                        return batch.commit();
                    }
                    return Tasks.forResult(null);
                });
    }

    /**
     * Supprime tous les lunchs dont la date est antérieure à aujourd'hui.
     *
     * @return Une tâche qui complète la suppression des lunchs périmés.
     */
    public Task<Void> deleteExpiredLunches() {
        Date today = getToday(); // Utiliser la méthode existante pour obtenir la date d'aujourd'hui

        return lunchesCollection
                .whereLessThan("date", today) // Rechercher les lunchs avec une date avant aujourd'hui
                .get()
                .continueWithTask(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        WriteBatch batch = lunchesCollection.getFirestore().batch();
                        for (DocumentSnapshot document : task.getResult()) {
                            batch.delete(document.getReference());
                        }
                        return batch.commit(); // Effectuer la suppression en lot
                    }
                    return Tasks.forResult(null); // Aucun lunch à supprimer
                });
    }

    public LiveData<Lunch> getUserLunchForToday(String userId) {
        MutableLiveData<Lunch> lunchLiveData = new MutableLiveData<>();
        Date today = getToday();

        Log.d("LunchRepository", "Fetching lunch for userId: " + userId + " for today: " + today);

        lunchesCollection
                .whereEqualTo("workmateId", userId)
                .whereGreaterThanOrEqualTo("date", today)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            Lunch lunch = task.getResult().getDocuments().get(0).toObject(Lunch.class);
                            Log.d("LunchRepository", "Lunch found: " + lunch.getRestaurantName());
                            lunchLiveData.setValue(lunch);
                        } else {
                            Log.w("LunchRepository", "No lunch found for userId: " + userId);
                            lunchLiveData.setValue(null);
                        }
                    } else {
                        Log.e("LunchRepository", "Error fetching lunch for userId: " + userId, task.getException());
                        lunchLiveData.setValue(null);
                    }
                });

        return lunchLiveData;
    }
}