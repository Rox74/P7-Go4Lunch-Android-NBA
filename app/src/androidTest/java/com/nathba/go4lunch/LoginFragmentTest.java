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

/**
 * Instrumented test class for the LoginFragment.
 * Verifies UI behavior and the login flow using Espresso and UI Automator.
 */
@RunWith(AndroidJUnit4.class)
public class LoginFragmentTest {

    /**
     * Rule to launch the MainActivity for testing scenarios.
     */
    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    /**
     * Test to verify that the LoginFragment displays the correct UI elements.
     * Checks the visibility of the logo and the Google sign-in button.
     */
    @Test
    public void testLoginFragment_DisplaysCorrectly() {
        onView(withId(R.id.logo)).check(matches(isDisplayed())); // Check logo is displayed
        onView(withId(R.id.google_sign_in_button)).check(matches(isDisplayed())); // Check sign-in button is displayed
    }

    /**
     * Test the login flow for a successful sign-in.
     * Simulates user interactions with Google account selection and checks redirection to the main content.
     *
     * @throws InterruptedException if thread sleep is interrupted.
     */
    @Test
    public void testSignInFlow_Success_RedirectsToMainContent() throws InterruptedException {
        // Simulate launching the activity and performing a successful login
        ActivityScenario<MainActivity> scenario = activityScenarioRule.getScenario();

        // Simulate clicking the Google sign-in button
        onView(withId(R.id.google_sign_in_button)).perform(click());

        // Add a delay for the Google account selection screen to appear
        Thread.sleep(2000); // Wait for 2 seconds for the Google account screen

        // Simulate clicking at the center of the screen for account selection
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        int x = device.getDisplayWidth() / 2;
        int y = (int) (device.getDisplayHeight() / 2.1);
        int y2 = (int) (device.getDisplayHeight() / 1.7);
        device.click(x, y); // Click center of the screen for Google account

        // Add another delay for the location permission dialog to appear
        Thread.sleep(3200); // Wait for 3.2 seconds for the location dialog

        // Simulate clicking at the center of the screen for location permission
        device.click(x, y2); // Click center of the screen for location permission

        // Verify the UI changes after login and location permission
        onView(withId(R.id.drawer_layout)).check(matches(isDisplayed())); // Check drawer layout is displayed
        onView(withId(R.id.bottom_navigation)).check(matches(isDisplayed())); // Check bottom navigation is displayed
    }
}