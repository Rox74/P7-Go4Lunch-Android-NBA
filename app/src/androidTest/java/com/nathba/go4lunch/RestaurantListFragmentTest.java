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

/**
 * Instrumented test class for the RestaurantListFragment.
 * Verifies UI behavior and interactions within the restaurant list view.
 */
@RunWith(AndroidJUnit4.class)
public class RestaurantListFragmentTest {

    /**
     * Rule to launch the MainActivity for testing scenarios.
     */
    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    /**
     * Setup method executed before each test.
     * Ensures the user is logged in by simulating the Google sign-in process if necessary.
     *
     * @throws InterruptedException if the thread sleep is interrupted.
     */
    @Before
    public void setUp() throws InterruptedException {
        if (isOnLoginScreen()) {
            performGoogleSignIn();
        }
    }

    /**
     * Checks if the current screen is the login screen.
     *
     * @return True if the login screen is displayed, otherwise false.
     */
    private boolean isOnLoginScreen() {
        try {
            onView(withId(R.id.google_sign_in_button)).check(matches(isDisplayed()));
            return true;
        } catch (NoMatchingViewException e) {
            return false; // Not on the login screen
        }
    }

    /**
     * Simulates the Google sign-in process by interacting with the Google account selection screen.
     *
     * @throws InterruptedException if the thread sleep is interrupted.
     */
    private void performGoogleSignIn() throws InterruptedException {
        onView(withId(R.id.google_sign_in_button)).perform(click());

        // Wait for the Google account selection screen
        Thread.sleep(2000);

        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        int x = device.getDisplayWidth() / 2;
        int y = (int) (device.getDisplayHeight() / 2.1);
        int y2 = (int) (device.getDisplayHeight() / 1.7);

        // Simulate clicking on the account selection
        device.click(x, y);

        // Wait for location permission dialog
        Thread.sleep(5000);

        // Simulate clicking to confirm location permissions
        device.click(x, y2);
    }

    /**
     * Tests that the RestaurantListFragment displays correctly.
     * Verifies the RecyclerView and its first restaurant item are displayed properly.
     *
     * @throws InterruptedException if the thread sleep is interrupted.
     */
    @Test
    public void testRestaurantListFragment_DisplaysCorrectly() throws InterruptedException {

        // Open the MapView fragment
        onView(withId(R.id.bottom_navigation)).perform(click());
        onView(withId(R.id.nav_map_view)).perform(click());

        // Wait for the MapView to load
        Thread.sleep(1000);

        // Navigate to the restaurant list view
        onView(withId(R.id.bottom_navigation)).perform(click());
        onView(withId(R.id.nav_list_view)).perform(click());

        // Wait for the restaurant list to load
        Thread.sleep(3000);

        // Verify that the RecyclerView is displayed
        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()));

        // Verify that the first item in the restaurant list is displayed
        onView(withId(R.id.recyclerView))
                .perform(RecyclerViewActions.scrollToPosition(0))
                .check(matches(hasDescendant(withId(R.id.restaurantName))));
    }
}