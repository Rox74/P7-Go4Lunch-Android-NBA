package com.nathba.go4lunch.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.nathba.go4lunch.R;
import com.nathba.go4lunch.application.RestaurantViewModel;
import com.nathba.go4lunch.application.ViewModelFactory;
import com.nathba.go4lunch.di.AppInjector;
import com.nathba.go4lunch.models.Restaurant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Fragment displaying a list of restaurants.
 * Implements search and sorting functionality for the restaurant list.
 */
public class RestaurantListFragment extends Fragment implements Searchable {

    private RestaurantViewModel restaurantViewModel;
    private RecyclerView recyclerView;
    private RestaurantAdapter adapter;
    private FusedLocationProviderClient fusedLocationClient;

    private List<Restaurant> fullRestaurantList = new ArrayList<>();
    private List<Restaurant> filteredRestaurantList = new ArrayList<>();
    private Location userLocation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant_list, container, false);

        // Initialize ViewModel
        ViewModelFactory viewModelFactory = AppInjector.getInstance().getViewModelFactory();
        restaurantViewModel = new ViewModelProvider(this, viewModelFactory).get(RestaurantViewModel.class);

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        adapter = new RestaurantAdapter(restaurantViewModel);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Observe restaurant data
        observeDetailedRestaurants();
        fetchUserLocationDirectly();

        return view;
    }

    /**
     * Observes detailed restaurant data from the ViewModel and updates the RecyclerView.
     */
    private void observeDetailedRestaurants() {
        if (restaurantViewModel != null) {
            restaurantViewModel.getDetailedRestaurants().observe(getViewLifecycleOwner(), detailedRestaurants -> {
                if (detailedRestaurants != null) {
                    fullRestaurantList.clear();
                    fullRestaurantList.addAll(detailedRestaurants);

                    filteredRestaurantList.clear();
                    filteredRestaurantList.addAll(detailedRestaurants);

                    adapter.submitList(filteredRestaurantList);
                } else {
                    Log.e("RestaurantListFragment", "No detailed restaurants available");
                }
            });
        } else {
            Log.e("RestaurantListFragment", "RestaurantViewModel is null");
        }
    }

    /**
     * Fetches the user's current location directly using FusedLocationProviderClient.
     * Requests location permission if not already granted.
     */
    private void fetchUserLocationDirectly() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        userLocation = location;
                        Log.d("RestaurantListFragment", "User location updated: " + location.getLatitude() + ", " + location.getLongitude());
                    } else {
                        Log.e("RestaurantListFragment", "Failed to retrieve location");
                    }
                })
                .addOnFailureListener(e -> Log.e("RestaurantListFragment", "Error getting location", e));
    }

    /**
     * Filters the restaurant list based on the search query and updates the RecyclerView.
     *
     * @param query The search query entered by the user.
     */
    @Override
    public void onSearch(String query) {
        filteredRestaurantList.clear();

        if (query == null || query.isEmpty()) {
            filteredRestaurantList.addAll(fullRestaurantList);
        } else {
            for (Restaurant restaurant : fullRestaurantList) {
                if (restaurant.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredRestaurantList.add(restaurant);
                }
            }
        }

        adapter.submitList(filteredRestaurantList);
    }

    /**
     * Sorts the restaurant list based on the given criterion and updates the RecyclerView.
     *
     * @param criterion The sorting criterion, such as "distance", "stars", "a_to_z", or "z_to_a".
     */
    @Override
    public void onSort(String criterion) {
        switch (criterion) {
            case "distance":
                Log.d("SortDebug", "User location: " + userLocation);
                if (userLocation != null) {
                    // Sort by distance
                    Log.d("SortDebug", "User location: Lat=" + userLocation.getLatitude() + ", Lon=" + userLocation.getLongitude());
                    Collections.sort(filteredRestaurantList, new Comparator<Restaurant>() {
                        @Override
                        public int compare(Restaurant r1, Restaurant r2) {
                            float[] result1 = new float[1];
                            float[] result2 = new float[1];

                            Location.distanceBetween(
                                    userLocation.getLatitude(), userLocation.getLongitude(),
                                    r1.getLocation().getLatitude(), r1.getLocation().getLongitude(),
                                    result1
                            );

                            Location.distanceBetween(
                                    userLocation.getLatitude(), userLocation.getLongitude(),
                                    r2.getLocation().getLatitude(), r2.getLocation().getLongitude(),
                                    result2
                            );

                            Log.d("SortDebug", "Restaurant: " + r1.getName() + " - Location: " + r1.getLocation());
                            Log.d("SortDebug", "Restaurant: " + r2.getName() + " - Location: " + r2.getLocation());

                            return Float.compare(result1[0], result2[0]); // Sort by ascending distance
                        }
                    });
                } else {
                    Log.e("SortDebug", "User location is NULL");
                }
                break;
            case "stars":
                // Sort by rating (descending)
                Collections.sort(filteredRestaurantList, new Comparator<Restaurant>() {
                    @Override
                    public int compare(Restaurant r1, Restaurant r2) {
                        return Double.compare(r2.getRating(), r1.getRating());
                    }
                });
                break;
            case "a_to_z":
                // Sort alphabetically (A-Z)
                Collections.sort(filteredRestaurantList, new Comparator<Restaurant>() {
                    @Override
                    public int compare(Restaurant r1, Restaurant r2) {
                        return r1.getName().compareTo(r2.getName());
                    }
                });
                break;
            case "z_to_a":
                // Sort alphabetically (Z-A)
                Collections.sort(filteredRestaurantList, new Comparator<Restaurant>() {
                    @Override
                    public int compare(Restaurant r1, Restaurant r2) {
                        return r2.getName().compareTo(r1.getName());
                    }
                });
                break;
        }

        adapter.submitList(new ArrayList<>(filteredRestaurantList)); // Refresh the list
    }
}