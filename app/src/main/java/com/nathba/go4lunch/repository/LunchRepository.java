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

/**
 * Repository class for managing lunch-related data with Firebase Firestore.
 * <p>
 * This class provides methods to retrieve, add, and delete lunch data, as well as utility
 * methods to fetch lunches for specific conditions like "today" or "expired".
 */
public class LunchRepository {

    /** Reference to the "lunches" collection in Firestore. */
    private final CollectionReference lunchesCollection;

    /**
     * Constructor for {@link LunchRepository}.
     * <p>
     * Initializes the Firestore collection reference for lunches.
     *
     * @param firestore The {@link FirebaseFirestore} instance used for database operations.
     */
    public LunchRepository(FirebaseFirestore firestore) {
        lunchesCollection = firestore.collection("lunches");
    }

    /**
     * Retrieves all lunches from Firestore.
     *
     * @return A {@link LiveData} object containing a list of {@link Lunch} objects.
     */
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

    /**
     * Adds a lunch to Firestore.
     *
     * @param lunch The {@link Lunch} object to be added.
     */
    public void addLunch(Lunch lunch) {
        lunchesCollection.document(lunch.getLunchId()).set(lunch);
    }

    /**
     * Retrieves lunches scheduled for a specific restaurant today.
     *
     * @param restaurantId The ID of the restaurant.
     * @return A {@link LiveData} object containing a list of lunches for the specified restaurant.
     */
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

    /**
     * Deletes a user's lunch for a specific date to avoid duplicates.
     *
     * @param workmateId The ID of the workmate (user).
     * @param date       The date for which the lunch should be deleted.
     * @return A {@link Task} representing the completion of the deletion operation.
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
     * Deletes all lunches that are scheduled before today.
     *
     * @return A {@link Task} representing the completion of the deletion operation.
     */
    public Task<Void> deleteExpiredLunches() {
        Date today = getToday();

        return lunchesCollection
                .whereLessThan("date", today)
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
     * Retrieves the lunch scheduled for the current user today.
     *
     * @param userId The ID of the current user.
     * @return A {@link LiveData} object containing the user's lunch for today, or {@code null} if not found.
     */
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

    /**
     * Retrieves all lunches scheduled for today.
     *
     * @return A {@link LiveData} object containing a list of lunches scheduled for today.
     */
    public LiveData<List<Lunch>> getLunchesToday() {
        MutableLiveData<List<Lunch>> lunchesTodayLiveData = new MutableLiveData<>();

        lunchesCollection
                .whereGreaterThanOrEqualTo("date", getToday())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Lunch> lunchesToday = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Lunch lunch = document.toObject(Lunch.class);
                            lunchesToday.add(lunch);
                        }
                        lunchesTodayLiveData.setValue(lunchesToday);
                    } else {
                        lunchesTodayLiveData.setValue(new ArrayList<>());
                    }
                });

        return lunchesTodayLiveData;
    }

    /**
     * Utility method to retrieve today's date, reset to midnight.
     * <p>
     * If the current time is past 12 PM, it returns the date for tomorrow.
     *
     * @return A {@link Date} object representing today's or tomorrow's date.
     */
    private Date getToday() {
        Calendar calendar = Calendar.getInstance();

        // If it's past 12 PM, move to the next day
        if (calendar.get(Calendar.HOUR_OF_DAY) >= 12) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Reset time to midnight
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }
}