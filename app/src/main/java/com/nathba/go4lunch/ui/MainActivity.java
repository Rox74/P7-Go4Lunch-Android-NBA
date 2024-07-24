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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nathba.go4lunch.R;
import com.nathba.go4lunch.application.MainViewModel;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private BottomNavigationView bottomNavigationView;
    private MainViewModel mainViewModel;
    private FirebaseAuth firebaseAuth;
    private TextView navHeaderTitle;
    private TextView navHeaderSubtitle;

    private static final int NAV_MAP_VIEW = R.id.nav_map_view;
    private static final int NAV_LIST_VIEW = R.id.nav_list_view;
    private static final int NAV_WORKMATES = R.id.nav_workmates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
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
                // TODO : handle your lunch
            } else if (itemId == R.id.nav_settings) {
                // TODO : handle settings
            } else if (itemId == R.id.nav_logout) {
                firebaseAuth.signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Get the header view and initialize the TextViews
        navHeaderTitle = navigationView.getHeaderView(0).findViewById(R.id.nav_header_title);
        navHeaderSubtitle = navigationView.getHeaderView(0).findViewById(R.id.nav_header_subtitle);

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

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        updateUI(firebaseAuth.getCurrentUser());
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            navHeaderTitle.setText(user.getDisplayName());
            navHeaderSubtitle.setText(user.getEmail());
        }
    }
}