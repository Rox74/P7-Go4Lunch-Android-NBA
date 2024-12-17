package com.nathba.go4lunch.notification;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for scheduling a daily notification at a specific time (12:00 PM).
 * <p>
 * It uses the {@link WorkManager} API to schedule a {@link PeriodicWorkRequest} that triggers
 * a {@link NotificationWorker} every 24 hours. The initial delay is calculated to ensure the notification
 * starts at the next occurrence of the specified time.
 */
public class NotificationScheduler {

    /**
     * Schedules a daily notification using WorkManager.
     * <p>
     * This method sets up a periodic work request that triggers every 24 hours.
     * It calculates the initial delay to ensure the notification starts at exactly 12:00 PM,
     * and replaces any existing work with the same name to avoid duplication.
     *
     * @param context The application context required to interact with {@link WorkManager}.
     */
    public static void scheduleDailyNotification(Context context) {
        // Build a periodic work request for 24-hour intervals
        PeriodicWorkRequest notificationWork = new PeriodicWorkRequest.Builder(NotificationWorker.class, 24, TimeUnit.HOURS)
                .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
                .build();

        // Enqueue the work request with a unique name to avoid duplication
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "DailyNotification",                    // Unique name for the work request
                ExistingPeriodicWorkPolicy.REPLACE,     // Replace any existing work with the same name
                notificationWork
        );
    }

    /**
     * Calculates the initial delay to ensure the notification starts at 12:00 PM.
     * <p>
     * If the current time is after 12:00 PM, the delay is calculated for the next day at 12:00 PM.
     * Otherwise, the delay is calculated for the same day at 12:00 PM.
     *
     * @return The initial delay in milliseconds from the current time to the next notification time.
     */
    private static long calculateInitialDelay() {
        Calendar now = Calendar.getInstance();  // Current time
        Calendar nextNotification = Calendar.getInstance();

        // Set the notification time to 12:00 PM
        nextNotification.set(Calendar.HOUR_OF_DAY, 12);
        nextNotification.set(Calendar.MINUTE, 0);
        nextNotification.set(Calendar.SECOND, 0);
        nextNotification.set(Calendar.MILLISECOND, 0);

        // If it's already past 12:00 PM, schedule for the next day
        if (now.after(nextNotification)) {
            nextNotification.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Return the delay in milliseconds
        return nextNotification.getTimeInMillis() - now.getTimeInMillis();
    }
}