package com.nathba.go4lunch.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nathba.go4lunch.models.NotificationData;

import java.util.Calendar;
import java.util.Date;

/**
 * Repository to fetch notification-related data from Firebase Firestore.
 * <p>
 * This repository retrieves the user's selected restaurant for today and provides
 * data about the colleagues joining them for lunch. The data is exposed as {@link LiveData}
 * for observation in the UI layer.
 */
public class NotificationRepository {

    /** Tag for logging purposes. */
    private static final String TAG = "NotificationRepository";

    /** Instance of Firebase Firestore for accessing the database. */
    private final FirebaseFirestore firestore;

    /**
     * Constructor for {@link NotificationRepository}.
     * <p>
     * Initializes the Firestore instance used to query the "lunches" collection.
     *
     * @param firestore The {@link FirebaseFirestore} instance for database operations.
     */
    public NotificationRepository(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Fetches the notification data for a given user for today's date.
     * <p>
     * The query checks the "lunches" collection in Firestore for a lunch scheduled today
     * for the specified user ID. If successful, the data is mapped to {@link NotificationData}
     * and returned as {@link LiveData}.
     *
     * @param userId The unique ID of the user for whom to fetch the notification data.
     * @return A {@link LiveData} object containing {@link NotificationData} with restaurant
     * and colleague details, or {@code null} if no data is found.
     */
    public LiveData<NotificationData> getNotificationData(String userId) {
        MutableLiveData<NotificationData> notificationData = new MutableLiveData<>();

        firestore.collection("lunches")
                .whereEqualTo("workmateId", userId)
                .whereEqualTo("date", getToday())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        NotificationData data = document.toObject(NotificationData.class);
                        notificationData.setValue(data);
                    } else {
                        notificationData.setValue(null);
                    }
                });

        return notificationData;
    }

    /**
     * Gets today's date reset to midnight (00:00:00).
     * <p>
     * This method ensures that the time component is set to 00:00:00 so that Firestore queries
     * based on the date field return accurate results.
     *
     * @return A {@link Date} object representing today's date at midnight.
     */
    private Date getToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}