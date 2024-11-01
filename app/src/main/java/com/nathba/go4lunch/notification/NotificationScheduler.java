package com.nathba.go4lunch.notification;

import android.content.Context;
import android.util.Log;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.nathba.go4lunch.R;

import java.util.List;

/**
 * Gère l'envoi immédiat des notifications quand un utilisateur choisit un restaurant.
 */
public class NotificationScheduler {
    private static final String TAG = "NotificationScheduler";

    /**
     * Envoie une notification immédiate avec les détails du déjeuner sélectionné.
     *
     * @param context            Contexte de l'application.
     * @param restaurantName     Nom du restaurant sélectionné.
     * @param restaurantAddress  Adresse du restaurant.
     * @param colleagues         Liste des collègues qui vont déjeuner ensemble.
     */
    public static void sendImmediateNotification(Context context, String restaurantName, String restaurantAddress, List<String> colleagues) {
        String colleaguesNames = String.join(", ", colleagues);  // Joindre les noms des collègues en une chaîne
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "LunchChannel")
                .setSmallIcon(R.drawable.ic_lunch)  // Remplacer par l'icône réelle de l'application
                .setContentTitle("Lunch Reminder")
                .setContentText("Today’s Lunch: " + restaurantName + " at " + restaurantAddress + " with " + colleaguesNames)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        // Vérifier et demander la permission POST_NOTIFICATIONS sur Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Missing POST_NOTIFICATIONS permission; notification not sent.");
                return;
            }
        }

        // Envoyer la notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, builder.build());
        Log.d(TAG, "Immediate notification sent for lunch at " + restaurantName);
    }
}