package com.nathba.go4lunch.ui;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.work.WorkManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nathba.go4lunch.R;
import com.nathba.go4lunch.application.LunchViewModel;
import com.nathba.go4lunch.application.MainViewModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.nathba.go4lunch.application.ViewModelFactory;
import com.nathba.go4lunch.di.AppInjector;
import com.nathba.go4lunch.notification.NotificationScheduler;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.security.ProviderInstaller;

import java.util.Locale;

/**
 * MainActivity class that serves as the main entry point of the application.
 * Manages navigation between different fragments, handles authentication state,
 * and updates the UI based on the current user.
 */
public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private BottomNavigationView bottomNavigationView;
    private NavigationView navigationView;
    private MainViewModel mainViewModel;
    private ViewModelFactory viewModelFactory;
    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 1;

    /**
     * Called when the activity is first created. Sets up the UI, initializes ViewModels,
     * and manages navigation and authentication state.
     *
     * @param savedInstanceState The saved instance state of the activity, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Install security provider
        installSecurityProvider();

        // Obtain ViewModelFactory from AppInjector
        viewModelFactory = AppInjector.getInstance().getViewModelFactory();

        // Initialize MainViewModel using ViewModelFactory
        mainViewModel = new ViewModelProvider(this, viewModelFactory).get(MainViewModel.class);

        // Initialize UI components
        initializeViews();

        // Setup DrawerLayout, BottomNavigationView, and NavigationView
        setupDrawerLayout();
        setupBottomNavigation();
        setupNavigationView();

        // Observe ViewModel for changes
        observeViewModel();

        // If there is no saved instance state, check login state and load the default fragment
        if (savedInstanceState == null) {
            mainViewModel.checkLoginState();
            if (mainViewModel.getCurrentUser().getValue() != null) {
                navigateToFragment(R.id.nav_map_view); // Load the map view by default
            }
        }

        // Observe changes in the current user and update the UI accordingly
        mainViewModel.getCurrentUser().observe(this, firebaseUser -> {
            if (firebaseUser != null) {
                Log.d("MainActivity", "User reconnected, restoring main content...");
                showMainContent(); // Restore menus and main content
            } else {
                Log.d("MainActivity", "User is null, showing login...");
                showLoginFragment();
            }
        });

        // Create notification channel for Android 8.0 and above
        createNotificationChannel();

        // Request POST_NOTIFICATIONS permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_POST_NOTIFICATIONS);
            }
        }

        // Schedule daily notifications
        NotificationScheduler.scheduleDailyNotification(this);
    }

    /**
     * Creates the notification channel for lunch reminders.
     * This is required for devices running Android 8.0 (API 26) and above.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Lunch Notifications";
            String description = "Notifications for lunch selections";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("LunchChannel", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d("MainActivity", "Notification channel 'LunchChannel' created.");
            } else {
                Log.e("MainActivity", "NotificationManager is null, channel not created.");
            }
        }
    }

    /**
     * Installs the security provider asynchronously to ensure secure network communication.
     * Displays an error dialog if the installation fails.
     */
    private void installSecurityProvider() {
        ProviderInstaller.installIfNeededAsync(this, new ProviderInstaller.ProviderInstallListener() {
            @Override
            public void onProviderInstalled() {
                Log.d("ProviderInstaller", "Security provider installed successfully.");
            }

            @Override
            public void onProviderInstallFailed(int errorCode, Intent recoveryIntent) {
                Log.e("ProviderInstaller", "Failed to install security provider: " + errorCode);
                GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
                if (apiAvailability.isUserResolvableError(errorCode)) {
                    apiAvailability.getErrorDialog(MainActivity.this, errorCode, 0).show();
                }
            }
        });
    }

    /**
     * Initializes the main UI components of the activity.
     * Sets up references to the DrawerLayout, BottomNavigationView, and NavigationView.
     */
    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        navigationView = findViewById(R.id.navigation_view);
    }

    /**
     * Configures the DrawerLayout and its toggle for navigation.
     * Enables the navigation button in the action bar.
     */
    private void setupDrawerLayout() {
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Enable navigation button in the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    /**
     * Sets up the BottomNavigationView and assigns a listener to handle item selection events.
     * Updates the selected navigation item in the ViewModel.
     */
    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            mainViewModel.setSelectedNavigationItem(item.getItemId());
            return true;
        });
    }

    /**
     * Sets up the NavigationView and assigns a listener to handle item selection events.
     * Handles navigation to different fragments such as lunch details, settings, or logout functionality.
     */
    private void setupNavigationView() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_your_lunch) {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                Log.d("MainActivity", "User clicked on 'Your Lunch', fetching lunch for userId: " + userId);

                LunchViewModel lunchViewModel = new ViewModelProvider(this, viewModelFactory).get(LunchViewModel.class);
                lunchViewModel.getUserLunchForToday(userId).observe(this, lunch -> {
                    if (lunch != null) {
                        Log.d("MainActivity", "Lunch data found: " +
                                "restaurantId=" + lunch.getRestaurantId() +
                                ", restaurantName=" + lunch.getRestaurantName() +
                                ", restaurantAddress=" + lunch.getRestaurantAddress());

                        // Pass lunch details to RestaurantDetailFragment
                        Bundle bundle = new Bundle();
                        bundle.putString("restaurantId", lunch.getRestaurantId());
                        bundle.putString("restaurantName", lunch.getRestaurantName());
                        bundle.putString("restaurantAddress", lunch.getRestaurantAddress());

                        RestaurantDetailFragment fragment = new RestaurantDetailFragment();
                        fragment.setArguments(bundle);

                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .addToBackStack(null)
                                .commit();
                    } else {
                        Log.w("MainActivity", "No lunch data found for userId: " + userId);
                        Toast.makeText(this, "No lunch selected for today", Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (itemId == R.id.nav_settings) {
                // Navigate to settings fragment
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new SettingsFragment())
                        .addToBackStack(null)
                        .commit();
            } else if (itemId == R.id.nav_logout) {
                    // Handle logout action
                    Log.d("MainActivity", "Logout initiated...");
                    mainViewModel.signOut(this);
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    /**
     * Observes changes in the ViewModel and updates the UI accordingly.
     * Monitors the authentication state and selected navigation item.
     */
    private void observeViewModel() {
        mainViewModel.getCurrentUser().observe(this, this::updateUI);
        mainViewModel.getSelectedNavigationItem().observe(this, this::navigateToFragment);
    }

    /**
     * Updates the UI based on the current user's authentication state.
     * Displays the main content if the user is authenticated, or the login screen otherwise.
     *
     * @param user The currently authenticated FirebaseUser, or null if no user is signed in.
     */
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            showMainContent();
            updateNavigationHeader(user);

            // Add the user to Firestore as a Workmate
            mainViewModel.addWorkmateToFirestore(user);

            // Ensure that the map fragment is displayed
            navigateToFragment(R.id.nav_map_view);
        } else {
            showLoginFragment();
        }
    }

    /**
     * Displays the main content of the activity, including the navigation components.
     * Replaces the LoginFragment with the default MapViewFragment.
     */
    private void showMainContent() {
        Log.d("MainActivity", "Restoring main content...");

        // Make the navigation components visible
        bottomNavigationView.setVisibility(View.VISIBLE);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        if (getSupportActionBar() != null) {
            getSupportActionBar().show(); // Show the action bar
        }

        // Reset the menu for the navigation drawer
        navigationView.setVisibility(View.VISIBLE);

        // Replace the LoginFragment with the default MapViewFragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new MapViewFragment()) // Default map view
                .commit();

        // Invalidate and rebuild the options menu
        invalidateOptionsMenu();
    }

    /**
     * Displays the LoginFragment and hides the main content.
     * Locks the navigation drawer and hides the action bar.
     */
    private void showLoginFragment() {
        bottomNavigationView.setVisibility(View.GONE);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        // Replace any existing fragment with LoginFragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new LoginFragment())
                .commit();
    }

    /**
     * Updates the NavigationView header with the current user's information.
     * Displays the user's name, email, and profile picture in the header.
     *
     * @param user The currently authenticated FirebaseUser.
     */
    private void updateNavigationHeader(FirebaseUser user) {
        View headerView = navigationView.getHeaderView(0);
        TextView navHeaderTitle = headerView.findViewById(R.id.nav_header_title);
        TextView navHeaderSubtitle = headerView.findViewById(R.id.nav_header_subtitle);
        ImageView navHeaderImage = headerView.findViewById(R.id.nav_header_image);

        navHeaderTitle.setText(user.getDisplayName());
        navHeaderSubtitle.setText(user.getEmail());
        Glide.with(this)
                .load(user.getPhotoUrl())
                .transform(new CircleCrop())
                .into(navHeaderImage);
    }

    /**
     * Navigates to a specified fragment based on the selected item ID.
     * Updates the UI by replacing the current fragment with the selected fragment.
     *
     * @param itemId The ID of the selected navigation item.
     */
    private void navigateToFragment(int itemId) {
        Fragment selectedFragment = null;
        String fragmentTag = null;

        if (itemId == R.id.nav_map_view) {
            selectedFragment = new MapViewFragment();
            fragmentTag = "MAP_VIEW_FRAGMENT";
        } else if (itemId == R.id.nav_list_view) {
            selectedFragment = new RestaurantListFragment();
            fragmentTag = "RESTAURANT_LIST_FRAGMENT";
        } else if (itemId == R.id.nav_workmates) {
            selectedFragment = new WorkmateFragment();
            fragmentTag = "WORKMATES_FRAGMENT";
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment, fragmentTag)
                    .commit();

            // Delay menu invalidation to ensure the fragment is ready
            new Handler(Looper.getMainLooper()).post(this::invalidateOptionsMenu);
        }
    }

    /**
     * Handles item selection in the options menu.
     * Passes sorting actions to the current fragment if it implements the Searchable interface.
     *
     * @param item The selected menu item.
     * @return true if the event was handled, false otherwise.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if (toggle.onOptionsItemSelected(item)) { // Handles the navigation menu toggle
            return true;
        }

        if (currentFragment instanceof Searchable) {
            if (item.getItemId() == R.id.sort_by_distance) {
                ((Searchable) currentFragment).onSort("distance");
                return true;
            } else if (item.getItemId() == R.id.sort_by_stars) {
                ((Searchable) currentFragment).onSort("stars");
                return true;
            } else if (item.getItemId() == R.id.sort_a_to_z) {
                ((Searchable) currentFragment).onSort("a_to_z");
                return true;
            } else if (item.getItemId() == R.id.sort_z_to_a) {
                ((Searchable) currentFragment).onSort("z_to_a");
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Called after {@link #onCreate(Bundle)} to perform final initialization.
     * Ensures the DrawerToggle state is synchronized with the drawer layout.
     *
     * @param savedInstanceState The saved instance state of the activity, if any.
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (toggle != null) {
            toggle.syncState(); // Synchronize after creation
        }
    }

    /**
     * Initializes the options menu and configures its visibility and behavior
     * based on the active fragment.
     *
     * @param menu The options menu in which items are placed.
     * @return true if the menu should be displayed, false otherwise.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        // Get the active fragment
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        // Check if the active fragment is valid
        if (currentFragment == null) {
            Log.e("MenuError", "Active fragment not found");
            return true; // No menu to display
        }

        // Configure menu icons based on the active fragment
        MenuItem searchItem = menu.findItem(R.id.action_search); // Search icon
        MenuItem sortItem = menu.findItem(R.id.action_sort);     // Sort icon

        if (currentFragment instanceof RestaurantListFragment) {
            searchItem.setVisible(true);
            sortItem.setVisible(true);
        } else if (currentFragment instanceof MapViewFragment) {
            searchItem.setVisible(true);
            sortItem.setVisible(false);
        } else {
            searchItem.setVisible(false);
            sortItem.setVisible(false);
        }

        // Configure SearchView for fragments implementing the Searchable interface
        if (currentFragment instanceof Searchable) {
            SearchView searchView = (SearchView) searchItem.getActionView();
            assert searchView != null;
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    ((Searchable) currentFragment).onSearch(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    ((Searchable) currentFragment).onSearch(newText);
                    return true;
                }
            });
        }

        return true;
    }

    /**
     * Passes the search query to the currently visible fragment, if it implements the Searchable interface.
     *
     * @param query The search text entered by the user.
     */
    private void handleSearchQuery(String query) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if (currentFragment instanceof Searchable) {
            ((Searchable) currentFragment).onSearch(query); // Calls the Searchable interface method in the fragment
        }
    }

    /**
     * Configures notification preferences based on user settings.
     * Enables or disables daily notifications for the application.
     */
    private void setupNotificationPreference() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Observe the preference for enabling/disabling notifications
        boolean notificationsEnabled = prefs.getBoolean("notifications_enabled", true);
        if (notificationsEnabled) {
            NotificationScheduler.scheduleDailyNotification(this);
            Log.d("MainActivity", "Notifications enabled");
        } else {
            WorkManager.getInstance(this).cancelUniqueWork("DailyNotification");
            Log.d("MainActivity", "Notifications disabled");
        }
    }

    /**
     * Attaches a new base context with the preferred application language.
     * Applies the selected language to the app's configuration.
     *
     * @param newBase The new base context for the activity.
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(newBase);
        String languageCode = prefs.getString("app_language", "default");

        // Handle the system default language
        if (languageCode.equals("default")) {
            languageCode = Resources.getSystem().getConfiguration().locale.getLanguage();
        }

        // Apply the new language
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        android.content.res.Configuration config = new android.content.res.Configuration();
        config.setLocale(locale);

        Context context = newBase.createConfigurationContext(config);
        super.attachBaseContext(context);
    }

    /**
     * Called when the activity is resumed.
     * Ensures notification preferences are applied.
     */
    @Override
    protected void onResume() {
        super.onResume();
        setupNotificationPreference(); // Checks notification preferences
    }

    /**
     * Called when the activity is destroyed.
     * Removes observers from the ViewModel to prevent memory leaks.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainViewModel.getCurrentUser().removeObservers(this);
        Log.d("MainActivity", "Observers detached in onDestroy.");
    }
}