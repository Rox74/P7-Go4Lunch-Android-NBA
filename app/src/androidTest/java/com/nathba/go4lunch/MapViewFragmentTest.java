package com.nathba.go4lunch;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.os.SystemClock;

import androidx.test.espresso.NoMatchingViewException;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import com.nathba.go4lunch.ui.MainActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MapViewFragmentTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp() throws InterruptedException {
        // Vérifier si l'utilisateur est sur la page de connexion au démarrage
        if (isOnLoginScreen()) {
            // Simuler la connexion Google
            performGoogleSignIn();
        }
    }

    private boolean isOnLoginScreen() {
        // Vérifier si l'écran de connexion est affiché
        try {
            onView(withId(R.id.google_sign_in_button)).check(matches(isDisplayed()));
            return true;
        } catch (NoMatchingViewException e) {
            // Si l'élément n'est pas trouvé, cela signifie que nous ne sommes pas sur la page de connexion
            return false;
        }
    }

    private void performGoogleSignIn() throws InterruptedException {
        // Simuler le clic sur le bouton de connexion Google
        onView(withId(R.id.google_sign_in_button)).perform(click());

        // Attendre 2 secondes pour la fenêtre de sélection du compte Google
        Thread.sleep(2000);

        // Simuler un clic au centre de l'écran pour sélectionner le compte Google
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        int x = device.getDisplayWidth() / 2;
        int y = (int) (device.getDisplayHeight() / 1.8);

        device.click(x, y);  // Clic au centre de l'écran

        // Attendre 5 secondes pour la fenêtre de localisation
        Thread.sleep(5000);

        // Simuler un clic au centre de l'écran pour la validation de la localisation
        device.click(x, y);
    }

    @Test
    public void testMapViewFragment_DisplaysCorrectly() {
        // Ouvrir le fragment MapView via la navigation
        onView(withId(R.id.bottom_navigation)).perform(click());
        onView(withId(R.id.nav_map_view)).perform(click());

        // Vérifier que les éléments de la carte sont bien affichés
        onView(withId(R.id.mapview)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_geolocate)).check(matches(isDisplayed()));
    }

    @Test
    public void testGeolocationButton_UpdatesUserLocation() {
        // Ouvrir le fragment MapView
        onView(withId(R.id.bottom_navigation)).perform(click());
        onView(withId(R.id.nav_map_view)).perform(click());

        // Simuler un clic sur le bouton de géolocalisation
        onView(withId(R.id.btn_geolocate)).perform(click());

        // Ajouter un délai pour laisser le temps à la carte de se mettre à jour
        SystemClock.sleep(2000); // Attendre 2 secondes

        // Vérifier que la position de l'utilisateur est mise à jour
        onView(withText("Vous êtes ici")).check(matches(isDisplayed()));
    }

    @Test
    public void testMarkerClick_OpensRestaurantDetails() {
        // Ouvrir le fragment MapView
        onView(withId(R.id.bottom_navigation)).perform(click());
        onView(withId(R.id.nav_map_view)).perform(click());

        // Ajouter un délai pour laisser le temps aux marqueurs d'apparaître
        SystemClock.sleep(3000); // Attendre 3 secondes

        // Simuler un clic sur un marqueur de restaurant
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        int x = device.getDisplayWidth() / 2;
        int y = device.getDisplayHeight() / 2;
        device.click(x, y);  // Clic au centre de l'écran sur un marqueur

        // Ajouter un autre délai pour laisser le temps au fragment de détails de s'afficher
        SystemClock.sleep(5000); // Attendre 5 secondes

        // Vérifier que les détails du restaurant s'affichent
        onView(withId(R.id.restaurant_name)).check(matches(isDisplayed()));
        onView(withId(R.id.restaurant_address)).check(matches(isDisplayed()));
        onView(withId(R.id.restaurant_image)).check(matches(isDisplayed()));
    }
}