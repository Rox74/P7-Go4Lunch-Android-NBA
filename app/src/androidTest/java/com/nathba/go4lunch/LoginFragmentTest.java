package com.nathba.go4lunch;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import com.nathba.go4lunch.ui.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LoginFragmentTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testLoginFragment_DisplaysCorrectly() {
        // Vérifier que les éléments de l'interface utilisateur sont correctement affichés
        onView(withId(R.id.logo)).check(matches(isDisplayed()));
        onView(withId(R.id.google_sign_in_button)).check(matches(isDisplayed()));
    }

    @Test
    public void testSignInFlow_Success_RedirectsToMainContent() throws InterruptedException {
        // Simuler le lancement de l'activité et la connexion réussie
        ActivityScenario<MainActivity> scenario = activityScenarioRule.getScenario();

        // Vérifier que le bouton de connexion est visible et simuler une connexion réussie
        onView(withId(R.id.google_sign_in_button)).perform(click());

        // Ajouter un délai pour laisser le temps à la fenêtre de sélection du compte Google de s'afficher
        Thread.sleep(2000);  // Attendre 2 secondes pour l'écran Google

        // Simuler un clic au centre de l'écran pour la sélection du compte Google
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        int x = device.getDisplayWidth() / 2;
        int y = (int) (device.getDisplayHeight() / 2.1);
        int y2 = (int) (device.getDisplayHeight() / 1.7);
        device.click(x, y);  // Clic au centre de l'écran pour le compte Google

        // Ajouter un autre délai pour laisser le temps à la fenêtre de localisation de s'afficher
        Thread.sleep(3200);  // Attendre 3,2 secondes pour la fenêtre de localisation

        // Simuler un clic au centre de l'écran pour la fenêtre de localisation
        device.click(x, y2);  // Clic au centre de l'écran pour la localisation

        // Observer le changement d'interface après connexion et autorisation de localisation
        onView(withId(R.id.drawer_layout)).check(matches(isDisplayed()));
        onView(withId(R.id.bottom_navigation)).check(matches(isDisplayed()));
    }
}