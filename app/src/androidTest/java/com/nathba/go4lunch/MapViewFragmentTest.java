package com.nathba.go4lunch;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

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

/**
 * Instrumented test class for the MapViewFragment.
 * Verifies UI behavior and interactions within the map view, including navigation and marker clicks.
 */
@RunWith(AndroidJUnit4.class)
public class MapViewFragmentTest {

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

        // Wait for the location permission dialog
        Thread.sleep(5000);

        // Simulate clicking to confirm location permissions
        device.click(x, y2);
    }

    /**
     * Tests that the MapViewFragment displays correctly.
     * Verifies the presence of the map view and geolocation button.
     */
    @Test
    public void testMapViewFragment_DisplaysCorrectly() {
        onView(withId(R.id.bottom_navigation)).perform(click());
        onView(withId(R.id.nav_map_view)).perform(click());

        // Verify map elements are displayed
        onView(withId(R.id.mapview)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_geolocate)).check(matches(isDisplayed()));
    }

    /**
     * Tests that clicking on a restaurant marker opens the restaurant details screen.
     * Simulates user interaction with a marker and checks the details fragment.
     */
    @Test
    public void testMarkerClick_OpensRestaurantDetails() {
        onView(withId(R.id.bottom_navigation)).perform(click());
        onView(withId(R.id.nav_map_view)).perform(click());

        // Wait for markers to appear on the map
        SystemClock.sleep(3000);

        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        int x = device.getDisplayWidth() / 2;
        int y = device.getDisplayHeight() / 2;

        // Simulate clicking a marker
        device.click(x, y);

        // Wait for the restaurant details screen to load
        SystemClock.sleep(5000);

        // Verify restaurant details elements are displayed
        onView(withId(R.id.restaurant_name)).check(matches(isDisplayed()));
        onView(withId(R.id.restaurant_address)).check(matches(isDisplayed()));
        onView(withId(R.id.restaurant_image)).check(matches(isDisplayed()));
    }
}