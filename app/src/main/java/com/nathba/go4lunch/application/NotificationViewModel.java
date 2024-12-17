package com.nathba.go4lunch.application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.nathba.go4lunch.models.NotificationData;
import com.nathba.go4lunch.repository.NotificationRepository;

/**
 * ViewModel for managing notification data related to the user’s selected restaurant and colleagues.
 * <p>
 * This class interacts with {@link NotificationRepository} to retrieve notification-related data
 * from Firestore and exposes it as {@link LiveData} for observation by the UI.
 */
public class NotificationViewModel extends ViewModel {

    /** Tag used for logging purposes. */
    private static final String TAG = "NotificationViewModel";

    /** Repository responsible for fetching notification data from Firestore. */
    private final NotificationRepository notificationRepository;

    /** LiveData object to hold the notification data fetched from Firestore. */
    private LiveData<NotificationData> notificationData;

    /**
     * Constructor for {@link NotificationViewModel}.
     * <p>
     * Initializes the ViewModel with an instance of {@link NotificationRepository}.
     *
     * @param notificationRepository The repository responsible for retrieving notification data.
     */
    public NotificationViewModel(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Retrieves notification data for a specific user.
     * <p>
     * This method fetches notification information such as the selected restaurant
     * and the list of colleagues who are going to the same restaurant.
     * The data is fetched lazily, ensuring the repository is queried only when necessary.
     *
     * @param userId The unique ID of the user whose notification data is to be fetched.
     * @return A {@link LiveData} object containing {@link NotificationData}, which includes
     * restaurant details and colleagues' information.
     */
    public LiveData<NotificationData> getNotificationData(String userId) {
        if (notificationData == null) {
            notificationData = notificationRepository.getNotificationData(userId);
        }
        return notificationData;
    }
}