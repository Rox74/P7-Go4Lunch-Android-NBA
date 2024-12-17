package com.nathba.go4lunch.ui;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
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
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.work.Configuration;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        // Configurer le canal de notification
        createNotificationChannel();

        // Request POST_NOTIFICATIONS permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_POST_NOTIFICATIONS);
            }
        }

        // Planification des notifications quotidiennes
        NotificationScheduler.scheduleDailyNotification(this);
    }

    /**
     * Crée le canal de notification pour les déjeuners.
     * Nécessaire pour Android 8.0 (API 26) et versions ultérieures.
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
     * Initializes the UI components.
     */
    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        navigationView = findViewById(R.id.navigation_view);
    }

    /**
     * Sets up the DrawerLayout and its toggle.
     */
    private void setupDrawerLayout() {
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Activez le bouton de navigation dans la barre d'outils
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    /**
     * Sets up the BottomNavigationView and its item selection listener.
     */
    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            mainViewModel.setSelectedNavigationItem(item.getItemId());
            return true;
        });
    }

    /**
     * Sets up the NavigationView and its item selection listener.
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
                // Gestion des paramètres
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new SettingsFragment())
                        .addToBackStack(null)
                        .commit();
            } else if (itemId == R.id.nav_logout) {
                mainViewModel.signOut();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    /**
     * Observes changes in the ViewModel and updates the UI accordingly.
     */
    private void observeViewModel() {
        mainViewModel.getCurrentUser().observe(this, this::updateUI);
        mainViewModel.getSelectedNavigationItem().observe(this, this::navigateToFragment);
    }

    /**
     * Updates the UI based on the current user.
     * @param user The currently authenticated FirebaseUser.
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
     * Displays the main content of the activity and ensures that LoginFragment is removed.
     */
    private void showMainContent() {
        bottomNavigationView.setVisibility(View.VISIBLE);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        if (getSupportActionBar() != null) {
            getSupportActionBar().show();
        }
        // Ensure LoginFragment is removed
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof LoginFragment) {
            getSupportFragmentManager().beginTransaction()
                    .remove(currentFragment)
                    .commit();
        }
    }

    /**
     * Displays the LoginFragment and hides the main content.
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
     * Navigates to the specified fragment based on the selected item ID.
     * @param itemId The ID of the selected menu item.
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

            // Forcer la mise à jour du menu
            invalidateOptionsMenu();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if (toggle.onOptionsItemSelected(item)) { // Relie le clic au bouton de menu
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

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (toggle != null) {
            toggle.syncState(); // Synchronise après la création
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        // Récupérer le fragment actif
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        // Contrôler les items du menu
        MenuItem searchItem = menu.findItem(R.id.action_search); // Loupe
        MenuItem sortItem = menu.findItem(R.id.action_sort);     // Bouton de tri

        // Vérifiez si le fragment actif est valide
        if (currentFragment == null) {
            Log.e("MenuError", "Fragment actif non trouvé");
            searchItem.setVisible(false);
            sortItem.setVisible(false);
            return true;
        }

        // Contrôler la visibilité des items en fonction du fragment actif
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

        // Configurer le SearchView pour les fragments implémentant l'interface Searchable
        if (currentFragment instanceof Searchable) {
            androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();

            // Configurer les actions de recherche
            searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
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
     * Transmet la recherche au fragment visible.
     *
     * @param query Texte de la recherche.
     */
    private void handleSearchQuery(String query) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if (currentFragment instanceof Searchable) {
            ((Searchable) currentFragment).onSearch(query); // Appelle l'interface implémentée par les fragments
        }
    }

    private void setupNotificationPreference() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Observer la préférence pour activer/désactiver les notifications
        boolean notificationsEnabled = prefs.getBoolean("notifications_enabled", true);
        if (notificationsEnabled) {
            NotificationScheduler.scheduleDailyNotification(this);
            Log.d("MainActivity", "Notifications activées");
        } else {
            WorkManager.getInstance(this).cancelUniqueWork("DailyNotification");
            Log.d("MainActivity", "Notifications désactivées");
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(newBase);
        String languageCode = prefs.getString("app_language", "default");

        // Gérer la langue par défaut du système
        if (languageCode.equals("default")) {
            languageCode = Resources.getSystem().getConfiguration().locale.getLanguage();
        }

        // Appliquer la nouvelle langue
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        android.content.res.Configuration config = new android.content.res.Configuration();
        config.setLocale(locale);

        Context context = newBase.createConfigurationContext(config);
        super.attachBaseContext(context);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupNotificationPreference(); // Vérifie les préférences des notifications
    }
}