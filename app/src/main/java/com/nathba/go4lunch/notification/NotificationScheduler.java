package com.nathba.go4lunch.notification;

import android.content.Context;
import android.util.Log;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.nathba.go4lunch.R;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NotificationScheduler {
    public static void scheduleDailyNotification(Context context) {
        // Version normale (à décommenter pour la production)
         PeriodicWorkRequest notificationWork = new PeriodicWorkRequest.Builder(NotificationWorker.class, 24, TimeUnit.HOURS)
                 .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
                 .build();
         WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                 "DailyNotification",
                 ExistingPeriodicWorkPolicy.REPLACE,
                 notificationWork);

        // Version de test avec un délai court (10 secondes pour tester rapidement les notifications)
/*        OneTimeWorkRequest notificationTestWork = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .setInitialDelay(10, TimeUnit.SECONDS)
                .build();
        WorkManager.getInstance(context).enqueue(notificationTestWork);*/
    }

    private static long calculateInitialDelay() {
        Calendar now = Calendar.getInstance();
        Calendar nextNotification = Calendar.getInstance();
        nextNotification.set(Calendar.HOUR_OF_DAY, 12);
        nextNotification.set(Calendar.MINUTE, 0);
        nextNotification.set(Calendar.SECOND, 0);

        if (now.after(nextNotification)) {
            nextNotification.add(Calendar.DAY_OF_MONTH, 1);
        }
        return nextNotification.getTimeInMillis() - now.getTimeInMillis();
    }
}