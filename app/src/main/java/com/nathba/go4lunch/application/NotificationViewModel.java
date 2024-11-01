package com.nathba.go4lunch.application;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.nathba.go4lunch.models.NotificationData;
import com.nathba.go4lunch.repository.NotificationRepository;

/**
 * ViewModel for managing notification data related to user’s selected restaurant and colleagues.
 * Interacts with NotificationRepository to retrieve data from Firestore.
 */
public class NotificationViewModel extends ViewModel {
    private static final String TAG = "NotificationViewModel";
    private final NotificationRepository notificationRepository;
    private LiveData<NotificationData> notificationData;

    /**
     * Constructor for NotificationViewModel.
     *
     * @param notificationRepository The repository to fetch notification data from Firestore.
     */
    public NotificationViewModel(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Retrieves notification data for a specific user.
     *
     * @param userId The ID of the user.
     * @return LiveData containing NotificationData with restaurant and colleague information.
     */
    public LiveData<NotificationData> getNotificationData(String userId) {
        Log.d(TAG, "Requesting notification data for user: " + userId);
        if (notificationData == null) {
            notificationData = notificationRepository.getNotificationData(userId);
        }
        return notificationData;
    }
}