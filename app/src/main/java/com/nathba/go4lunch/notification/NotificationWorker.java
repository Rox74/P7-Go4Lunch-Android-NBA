package com.nathba.go4lunch.notification;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nathba.go4lunch.R;
import com.nathba.go4lunch.models.Lunch;
import com.nathba.go4lunch.models.Workmate;
import com.nathba.go4lunch.repository.LunchRepository;
import com.nathba.go4lunch.repository.WorkmateRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Worker class responsible for sending daily lunch notifications to the user.
 * <p>
 * It retrieves the user's scheduled lunch, fetches the list of colleagues joining the same restaurant,
 * and sends a notification to the user with the lunch details.
 * This worker runs in the background and interacts with Firebase Firestore.
 */
public class NotificationWorker extends Worker {

    /** Tag used for logging purposes. */
    private static final String TAG = "NotificationWorker";

    /** Instance of Firestore for database operations. */
    private final FirebaseFirestore firestore;

    /**
     * Constructs a new {@link NotificationWorker}.
     *
     * @param context The application context.
     * @param params  Worker parameters provided by WorkManager.
     */
    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        firestore = FirebaseFirestore.getInstance();
    }

    /**
     * Executes the background work for sending a lunch notification.
     * <p>
     * Fetches the user's lunch for today, retrieves colleagues attending the same restaurant,
     * and sends a notification if data is found.
     *
     * @return The result of the work, always {@link Result#success()}.
     */
    @NonNull
    @Override
    public Result doWork() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d(TAG, "Executing NotificationWorker for user: " + userId);

        fetchUserLunch(userId, lunch -> {
            if (lunch != null) {
                fetchColleaguesAndNotify(lunch);
            } else {
                Log.d(TAG, "No lunch found for today.");
            }
        });

        return Result.success();
    }

    /**
     * Fetches the user's lunch scheduled for today from Firestore.
     *
     * @param userId   The ID of the user.
     * @param listener Callback invoked with the {@link Lunch} object or {@code null} if no lunch is found.
     */
    private void fetchUserLunch(String userId, OnLunchFetchedListener listener) {
        Date today = getToday();

        firestore.collection("lunches")
                .whereEqualTo("workmateId", userId)
                .whereGreaterThanOrEqualTo("date", today)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        Lunch lunch = task.getResult().getDocuments().get(0).toObject(Lunch.class);
                        listener.onLunchFetched(lunch);
                    } else {
                        listener.onLunchFetched(null);
                    }
                });
    }

    /**
     * Fetches colleagues attending the same restaurant and sends a notification.
     *
     * @param lunch The user's lunch details.
     */
    private void fetchColleaguesAndNotify(Lunch lunch) {
        firestore.collection("lunches")
                .whereEqualTo("restaurantId", lunch.getRestaurantId())
                .whereGreaterThanOrEqualTo("date", getToday())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> colleagueNames = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String workmateId = document.getString("workmateId");
                            fetchWorkmateName(workmateId, name -> {
                                if (name != null) {
                                    colleagueNames.add(name);
                                    if (colleagueNames.size() == task.getResult().size()) {
                                        sendNotification(lunch.getRestaurantName(), lunch.getRestaurantAddress(), colleagueNames);
                                    }
                                }
                            });
                        }
                    }
                });
    }

    /**
     * Fetches the name of a workmate using their ID from Firestore.
     *
     * @param workmateId The workmate's unique identifier.
     * @param listener   Callback invoked with the workmate's name or {@code null} if not found.
     */
    private void fetchWorkmateName(String workmateId, OnWorkmateNameFetchedListener listener) {
        firestore.collection("workmates")
                .document(workmateId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        listener.onNameFetched(task.getResult().getString("name"));
                    } else {
                        listener.onNameFetched(null);
                    }
                });
    }

    /**
     * Sends a notification to the user with the lunch and colleagues' details.
     *
     * @param restaurantName    The name of the restaurant.
     * @param restaurantAddress The address of the restaurant.
     * @param colleagueNames    The list of colleagues' names attending the lunch.
     */
    private void sendNotification(String restaurantName, String restaurantAddress, List<String> colleagueNames) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Permission POST_NOTIFICATIONS denied. Cannot send notification.");
                return;
            }
        }

        String colleagueNamesString = TextUtils.join(", ", colleagueNames);
        Context context = getApplicationContext();
        String title = context.getString(R.string.notification_title);
        String content = context.getString(R.string.notification_content, restaurantName);
        String bigText = context.getString(R.string.notification_big_text, restaurantName, restaurantAddress, colleagueNamesString);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "LunchChannel")
                .setSmallIcon(R.drawable.ic_lunch)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.notify(1, builder.build());
            Log.d(TAG, "Notification sent for restaurant: " + restaurantName);
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException: Permission POST_NOTIFICATIONS is required but not granted.", e);
        }
    }

    /**
     * Gets the current date with time set to 00:00:00 for comparison purposes.
     *
     * @return The current date with time reset to midnight.
     */
    private Date getToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /** Callback interface for fetching the user's lunch. */
    private interface OnLunchFetchedListener {
        void onLunchFetched(Lunch lunch);
    }

    /** Callback interface for fetching a workmate name. */
    private interface OnWorkmateNameFetchedListener {
        void onNameFetched(String name);
    }
}