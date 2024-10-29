package com.nathba.go4lunch;

import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import com.nathba.go4lunch.ui.MainActivity;

@RunWith(AndroidJUnit4.class)
public class RestaurantListFragmentTest {

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

        // Attendre 5 secondes pour la validation de la localisation
        Thread.sleep(5000);

        // Simuler un clic au centre de l'écran pour valider la localisation
        device.click(x, y);
    }

    @Test
    public void testRestaurantListFragment_DisplaysCorrectly() throws InterruptedException {
        // Naviguer vers la vue liste de restaurants
        onView(withId(R.id.bottom_navigation)).perform(click());
        onView(withId(R.id.nav_list_view)).perform(click());

        // Vérifier que la RecyclerView des restaurants est affichée
        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()));

        // Vérifier que le premier élément de la liste des restaurants est affiché
        onView(withId(R.id.recyclerView))
                .perform(RecyclerViewActions.scrollToPosition(0))
                .check(matches(hasDescendant(withId(R.id.restaurantName))));
    }
}