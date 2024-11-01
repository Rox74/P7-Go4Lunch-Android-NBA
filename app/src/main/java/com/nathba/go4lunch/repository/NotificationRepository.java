package com.nathba.go4lunch.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nathba.go4lunch.models.NotificationData;

import java.util.Calendar;
import java.util.Date;

/**
 * Repository to fetch data from Firebase Firestore required for notifications.
 * Retrieves information on the user's selected restaurant and colleagues joining them.
 */
public class NotificationRepository {
    private static final String TAG = "NotificationRepository";
    private final FirebaseFirestore firestore;

    /**
     * Constructor initializes Firebase Firestore instance.
     *
     * @param firestore FirebaseFirestore instance for accessing Firestore database.
     */
    public NotificationRepository(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Fetches the notification data for a given user on the current date.
     *
     * @param userId The ID of the user.
     * @return LiveData containing NotificationData with restaurant and colleague information.
     */
    public LiveData<NotificationData> getNotificationData(String userId) {
        MutableLiveData<NotificationData> notificationData = new MutableLiveData<>();
        Log.d(TAG, "Fetching notification data for user: " + userId);

        firestore.collection("lunches")
                .whereEqualTo("workmateId", userId)
                .whereEqualTo("date", getToday())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        NotificationData data = document.toObject(NotificationData.class);
                        Log.d(TAG, "Notification data found: " + data);
                        notificationData.setValue(data);
                    } else {
                        Log.d(TAG, "No notification data found for user: " + userId);
                        notificationData.setValue(null);
                    }
                });
        return notificationData;
    }

    /**
     * Gets today's date without the time component.
     *
     * @return Date object representing the current date at 00:00.
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