package com.nathba.go4lunch.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Configure the ActionBarDrawerToggle
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_your_lunch) {
                // Handle "Your Lunch" option
            } else if (itemId == R.id.nav_settings) {
                // Handle "Settings" option
            } else if (itemId == R.id.nav_logout) {
                authViewModel.signOut();
                displayLoginFragment();
                hideMenusAndActionBar(); // Hide menus and ActionBar on logout
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        navHeaderTitle = navigationView.getHeaderView(0).findViewById(R.id.nav_header_title);
        navHeaderSubtitle = navigationView.getHeaderView(0).findViewById(R.id.nav_header_subtitle);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_map_view) {
                selectedFragment = new MapViewFragment();
            } else if (itemId == R.id.nav_list_view) {
                selectedFragment = new ListViewFragment();
            } else if (itemId == R.id.nav_workmates) {
                selectedFragment = new WorkmatesFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            }
            return true;
        });

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        authViewModel.getUserLiveData().observe(this, this::updateUI);

        if (savedInstanceState == null) {
            checkLoginState();
        }
    }

    private void checkLoginState() {
        FirebaseUser currentUser = authViewModel.getUserLiveData().getValue();
        if (currentUser == null) {
            displayLoginFragment();
            hideMenusAndActionBar();
        } else {
            onUserLoggedIn();
        }
    }

    public void onUserLoggedIn() {
        Log.d("MainActivity", "User logged in");
        FirebaseUser currentUser = authViewModel.getUserLiveData().getValue();
        if (currentUser != null) {
            updateUI(currentUser);
        }

        // Show default fragment after login
        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof LoginFragment) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new MapViewFragment())
                    .commit();
            showMenusAndActionBar();
        }

        bottomNavigationView.setSelectedItemId(R.id.nav_workmates);
    }

    private void hideMenusAndActionBar() {
        Log.d("MainActivity", "Hiding menus and action bar");
        bottomNavigationView.setVisibility(View.GONE);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    private void showMenusAndActionBar() {
        Log.d("MainActivity", "Showing menus and action bar");
        bottomNavigationView.setVisibility(View.VISIBLE);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        if (getSupportActionBar() != null) {
            getSupportActionBar().show();
        }
    }

    private void displayLoginFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new LoginFragment())
                .commit();
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            navHeaderTitle.setText(user.getDisplayName());
            navHeaderSubtitle.setText(user.getEmail());
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}