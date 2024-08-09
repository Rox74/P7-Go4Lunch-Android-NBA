package com.nathba.go4lunch.ui;

import android.os.Bundle;
import android.view.View;
import android.view.MenuItem;
import android.widget.ImageView;
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
import com.nathba.go4lunch.application.MainViewModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private BottomNavigationView bottomNavigationView;
    private NavigationView navigationView;
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        initializeViews();
        setupDrawerLayout();
        setupBottomNavigation();
        setupNavigationView();
        observeViewModel();

        if (savedInstanceState == null) {
            // Check login state and load the default fragment
            viewModel.checkLoginState();
            if (viewModel.getCurrentUser().getValue() != null) {
                navigateToFragment(R.id.nav_map_view); // Load the map view by default
            }
        }
    }

    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        navigationView = findViewById(R.id.navigation_view);
    }

    private void setupDrawerLayout() {
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            viewModel.setSelectedNavigationItem(item.getItemId());
            return true;
        });
    }

    private void setupNavigationView() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_your_lunch) {
                // Handle "Your Lunch" option
            } else if (itemId == R.id.nav_settings) {
                // Handle "Settings" option
            } else if (itemId == R.id.nav_logout) {
                viewModel.signOut();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void observeViewModel() {
        viewModel.getCurrentUser().observe(this, this::updateUI);
        viewModel.getSelectedNavigationItem().observe(this, this::navigateToFragment);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            showMainContent();
            updateNavigationHeader(user);
            // Ensure that the map fragment is displayed
            navigateToFragment(R.id.nav_map_view);
        } else {
            showLoginFragment();
        }
    }

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

    private void navigateToFragment(int itemId) {
        Fragment selectedFragment = null;
        String fragmentTag = null;

        if (itemId == R.id.nav_map_view) {
            selectedFragment = new MapViewFragment();
            fragmentTag = "MAP_VIEW_FRAGMENT";
        } else if (itemId == R.id.nav_list_view) {
            selectedFragment = new ListViewFragment();
            fragmentTag = "LIST_VIEW_FRAGMENT";
        } else if (itemId == R.id.nav_workmates) {
            selectedFragment = new WorkmatesFragment();
            fragmentTag = "WORKMATES_FRAGMENT";
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment, fragmentTag)
                    .commit();
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