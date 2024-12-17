package com.nathba.go4lunch.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.work.Configuration;
import androidx.work.WorkManager;

import com.nathba.go4lunch.R;
import com.nathba.go4lunch.notification.NotificationScheduler;

import java.util.Locale;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        // Gestion des notifications
        findPreference("notifications_enabled").setOnPreferenceChangeListener((preference, newValue) -> {
            boolean isEnabled = (Boolean) newValue;
            if (isEnabled) {
                NotificationScheduler.scheduleDailyNotification(getContext());
            } else {
                WorkManager.getInstance(getContext()).cancelUniqueWork("DailyNotification");
            }
            return true; // Sauvegarde la préférence
        });

        // Gestion du changement de langue
        ListPreference languagePreference = findPreference("app_language");
        if (languagePreference != null) {
            languagePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                updateLanguage((String) newValue);
                return true;
            });
        }
    }

    private void updateLanguage(String languageCode) {
        // Si "default" est sélectionné, utiliser la langue du système
        if (languageCode.equals("default")) {
            languageCode = Resources.getSystem().getConfiguration().locale.getLanguage();
        }

        // Créer une nouvelle Locale
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        // Appliquer la configuration mise à jour
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.setLocale(locale);

        // Mettre à jour le contexte avec la nouvelle configuration
        Context context = getActivity().getBaseContext();
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());

        // Redémarrer MainActivity pour appliquer la langue
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}