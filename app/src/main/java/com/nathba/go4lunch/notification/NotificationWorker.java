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
import androidx.lifecycle.Observer;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
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
    private final LunchRepository lunchRepository;
    private final WorkmateRepository workmateRepository;

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        this.lunchRepository = new LunchRepository(firestore);
        this.workmateRepository = new WorkmateRepository(firestore);
    }

    @NonNull
    @Override
    public Result doWork() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d(TAG, "Executing NotificationWorker for user: " + userId);

        // Utiliser un Handler pour exécuter observeForever sur le thread principal
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(() -> {
            lunchRepository.getLunches().observeForever(lunches -> {
                Lunch userLunch = null;
                for (Lunch lunch : lunches) {
                    if (lunch.getWorkmateId().equals(userId) && isToday(lunch.getDate())) {
                        userLunch = lunch;
                        break;
                    }
                }

                if (userLunch != null) {
                    Log.d(TAG, "Lunch trouvé pour aujourd'hui : " + userLunch.getRestaurantName());
                    fetchColleaguesAndNotify(userLunch);
                } else {
                    Log.d(TAG, "Aucun lunch trouvé pour l'utilisateur aujourd'hui.");
                }
            });
        });

        return Result.success();
    }

    /**
     * Récupère la liste des collègues et envoie une notification.
     *
     * @param lunch Le lunch de l'utilisateur pour aujourd'hui.
     */
    private void fetchColleaguesAndNotify(Lunch lunch) {
        lunchRepository.getLunchesForRestaurantToday(lunch.getRestaurantId()).observeForever(colleagueLunches -> {
            List<String> colleagueNames = new ArrayList<>();
            AtomicInteger count = new AtomicInteger(0);  // Suivi du nombre de récupérations de noms

            for (Lunch colleagueLunch : colleagueLunches) {
                String workmateId = colleagueLunch.getWorkmateId();

                // Récupérer le nom du workmate en utilisant workmateId
                workmateRepository.getWorkmateById(workmateId).observeForever(new Observer<Workmate>() {
                    @Override
                    public void onChanged(Workmate workmate) {
                        if (workmate != null && workmate.getName() != null) {
                            colleagueNames.add(workmate.getName());  // Ajouter le nom du collègue
                        }

                        // Incrémenter le compteur une fois le nom récupéré
                        if (count.incrementAndGet() == colleagueLunches.size()) {
                            sendNotification(lunch.getRestaurantName(), lunch.getRestaurantAddress(), colleagueNames);
                        }

                        // Supprimer l'observateur pour éviter une fuite de mémoire
                        workmateRepository.getWorkmateById(workmateId).removeObserver(this);
                    }
                });
            }
        });
    }

    /**
     * Envoie une notification locale avec les informations du restaurant et des collègues.
     *
     * @param restaurantName    Nom du restaurant.
     * @param restaurantAddress Adresse du restaurant.
     * @param colleagueNames    Liste des noms des collègues.
     */
    private void sendNotification(String restaurantName, String restaurantAddress, List<String> colleagueNames) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, getApplicationContext().getString(R.string.permission_post_notifications_denied));
                return;
            }
        }

        // Concatène les noms des collègues dans une chaîne
        String colleagueNamesString = TextUtils.join(", ", colleagueNames);

        // Utilise BigTextStyle pour afficher plus de contenu
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "LunchChannel")
                .setSmallIcon(R.drawable.ic_lunch)
                .setContentTitle(getApplicationContext().getString(R.string.lunch_reminder_title))
                .setContentText(getApplicationContext().getString(R.string.lunch_notification_content, restaurantName))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(getApplicationContext().getString(R.string.lunch_notification_big_text, restaurantName, restaurantAddress, colleagueNamesString)))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        notificationManager.notify(1, builder.build());
        Log.d(TAG, getApplicationContext().getString(R.string.notification_sent_log, restaurantName));
    }

    /**
     * Vérifie si une date correspond à aujourd'hui.
     *
     * @param date La date à vérifier.
     * @return true si la date est aujourd'hui, false sinon.
     */
    private boolean isToday(Date date) {
        Calendar today = Calendar.getInstance();
        Calendar lunchDate = Calendar.getInstance();
        lunchDate.setTime(date);

        return today.get(Calendar.YEAR) == lunchDate.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == lunchDate.get(Calendar.DAY_OF_YEAR);
    }
}