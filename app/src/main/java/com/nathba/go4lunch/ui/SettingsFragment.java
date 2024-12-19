package com.nathba.go4lunch.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.work.WorkManager;

import com.nathba.go4lunch.R;
import com.nathba.go4lunch.notification.NotificationScheduler;

import java.util.Locale;

/**
 * Fragment for managing application settings.
 * Includes preferences for notifications and language selection.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        // Handle notifications preference
        findPreference("notifications_enabled").setOnPreferenceChangeListener((preference, newValue) -> {
            boolean isEnabled = (Boolean) newValue;
            if (isEnabled) {
                NotificationScheduler.scheduleDailyNotification(getContext());
            } else {
                WorkManager.getInstance(getContext()).cancelUniqueWork("DailyNotification");
            }
            return true; // Save the preference
        });

        // Handle language change preference
        ListPreference languagePreference = findPreference("app_language");
        if (languagePreference != null) {
            languagePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                updateLanguage((String) newValue);
                return true;
            });
        }
    }

    /**
     * Updates the application language based on the selected preference.
     *
     * @param languageCode The new language code, or "default" for system language.
     */
    private void updateLanguage(String languageCode) {
        // Use system language if "default" is selected
        if (languageCode.equals("default")) {
            languageCode = Resources.getSystem().getConfiguration().locale.getLanguage();
        }

        // Create and set a new Locale
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        // Apply the updated configuration
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.setLocale(locale);

        // Update the context with the new configuration
        Context context = getActivity().getBaseContext();
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());

        // Restart MainActivity to apply the language change
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}