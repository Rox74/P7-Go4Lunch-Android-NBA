package com.nathba.go4lunch.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseUser;
import com.nathba.go4lunch.R;
import com.nathba.go4lunch.application.AuthViewModel;
import com.nathba.go4lunch.application.MainViewModel;

/**
 * MainActivity is the primary activity of the application that handles
 * navigation between different views and displays user information.
 */
public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private BottomNavigationView bottomNavigationView;
    private MainViewModel mainViewModel;
    private AuthViewModel authViewModel;
    private TextView navHeaderTitle;
    private TextView navHeaderSubtitle;

    private static final int NAV_MAP_VIEW = R.id.nav_map_view;
    private static final int NAV_LIST_VIEW = R.id.nav_list_view;
    private static final int NAV_WORKMATES = R.id.nav_workmates;

    /**
     * Called when the activity is first created. This is where you should do all of your normal static set up:
     * create views, bind data to lists, etc. This method also provides you with a Bundle containing the activity's
     * previously frozen state, if there was one.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle). Note: Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize drawer layout and toggle for navigation drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Set up the navigation view and its item selection listener
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_your_lunch) {
                // TODO: handle your lunch
            } else if (itemId == R.id.nav_settings) {
                // TODO: handle settings
            } else if (itemId == R.id.nav_logout) {
                authViewModel.signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Get the header view and initialize the TextViews for user information
        navHeaderTitle = navigationView.getHeaderView(0).findViewById(R.id.nav_header_title);
        navHeaderSubtitle = navigationView.getHeaderView(0).findViewById(R.id.nav_header_subtitle);

        // Set up the bottom navigation view and its item selection listener
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == NAV_MAP_VIEW) {
                selectedFragment = new MapViewFragment();
            } else if (itemId == NAV_LIST_VIEW) {
                selectedFragment = new ListViewFragment();
            } else if (itemId == NAV_WORKMATES) {
                selectedFragment = new WorkmatesFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            }
            return true;
        });

        // Initialize ViewModels
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Observe authentication state and update UI accordingly
        authViewModel.getUserLiveData().observe(this, this::updateUI);

        // Optionally observe data from MainViewModel
        mainViewModel.getRestaurants().observe(this, restaurants -> {
            // TODO: Handle restaurant list update
        });

        mainViewModel.getWorkmates().observe(this, workmates -> {
            // TODO: Handle workmate list update
        });
    }

    /**
     * Called when the activity is becoming visible to the user.
     */
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = authViewModel.getUserLiveData().getValue();
        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
    }

    /**
     * Called whenever a menu item is selected.
     *
     * @param item The selected menu item.
     * @return true if the event was handled, false otherwise.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Updates the UI with the user's information.
     *
     * @param user The Firebase user.
     */
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            navHeaderTitle.setText(user.getDisplayName());
            navHeaderSubtitle.setText(user.getEmail());
        }
    }
}