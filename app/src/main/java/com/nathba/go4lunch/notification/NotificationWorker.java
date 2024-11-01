package com.nathba.go4lunch.notification;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nathba.go4lunch.R;
import com.nathba.go4lunch.models.NotificationData;
import com.nathba.go4lunch.repository.NotificationRepository;

public class NotificationWorker extends Worker {
    private static final String TAG = "NotificationWorker";
    private final NotificationRepository notificationRepository;

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        notificationRepository = new NotificationRepository(FirebaseFirestore.getInstance());
    }

    @NonNull
    @Override
    public Result doWork() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d(TAG, "Executing NotificationWorker for user: " + userId);

        // Run observation on the main thread
        new Handler(Looper.getMainLooper()).post(() -> {
            LiveData<NotificationData> liveData = notificationRepository.getNotificationData(userId);
            liveData.observeForever(new Observer<NotificationData>() {
                @Override
                public void onChanged(NotificationData notificationData) {
                    liveData.removeObserver(this);  // Remove observer after first update
                    if (notificationData != null) {
                        Log.d(TAG, "Notification data received: " + notificationData);
                        sendNotification(notificationData);
                    } else {
                        Log.d(TAG, "No notification data found, notification not sent.");
                    }
                }
            });
        });

        return Result.success();
    }

    /**
     * Sends a notification to the user with restaurant and colleague details.
     * Checks the POST_NOTIFICATIONS permission for Android 13+.
     *
     * @param data The notification data containing restaurant and colleague details.
     */
    private void sendNotification(NotificationData data) {
        // Check if we have permission to post notifications on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Missing POST_NOTIFICATIONS permission; notification not sent.");
                return;
            }
        }

        Log.d(TAG, "Sending notification with data: " + data);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "LunchChannel")
                .setSmallIcon(R.drawable.ic_lunch)
                .setContentTitle("Lunch Reminder")
                .setContentText("Today’s Lunch: " + data.getRestaurantName() + " with colleagues: " + data.getColleaguesNames())
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        notificationManager.notify(1, builder.build());
        Log.d(TAG, "Notification sent.");
    }
}