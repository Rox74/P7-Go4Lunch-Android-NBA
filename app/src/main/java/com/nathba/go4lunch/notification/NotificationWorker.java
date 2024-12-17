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

public class NotificationWorker extends Worker {
    private static final String TAG = "NotificationWorker";
    private final FirebaseFirestore firestore;

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        firestore = FirebaseFirestore.getInstance();
    }

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

    private void sendNotification(String restaurantName, String restaurantAddress, List<String> colleagueNames) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Permission POST_NOTIFICATIONS denied. Cannot send notification.");
                return; // Arrêter l'exécution si la permission est refusée
            }
        }

        // Concaténer les noms des collègues dans une chaîne
        String colleagueNamesString = TextUtils.join(", ", colleagueNames);

        // Récupérer les chaînes localisées depuis les ressources
        Context context = getApplicationContext();
        String title = context.getString(R.string.notification_title);
        String content = context.getString(R.string.notification_content, restaurantName);
        String bigText = context.getString(R.string.notification_big_text, restaurantName, restaurantAddress, colleagueNamesString);

        // Construire la notification avec les chaînes localisées
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "LunchChannel")
                .setSmallIcon(R.drawable.ic_lunch)
                .setContentTitle(title)  // Titre localisé
                .setContentText(content) // Contenu localisé
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText)) // Texte complet localisé
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Envoyer la notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.notify(1, builder.build());
            Log.d(TAG, "Notification sent for restaurant: " + restaurantName);
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException: Permission POST_NOTIFICATIONS is required but not granted.", e);
        }
    }

    private Date getToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    // Interfaces pour les callbacks
    private interface OnLunchFetchedListener {
        void onLunchFetched(Lunch lunch);
    }

    private interface OnWorkmateNameFetchedListener {
        void onNameFetched(String name);
    }
}