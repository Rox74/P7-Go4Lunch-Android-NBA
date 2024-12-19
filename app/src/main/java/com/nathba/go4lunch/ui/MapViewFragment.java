package com.nathba.go4lunch.ui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.nathba.go4lunch.R;
import com.nathba.go4lunch.application.MapViewModel;
import com.nathba.go4lunch.application.ViewModelFactory;
import com.nathba.go4lunch.di.AppInjector;
import com.nathba.go4lunch.models.Lunch;
import com.nathba.go4lunch.models.Restaurant;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;

import android.location.LocationListener;
import android.widget.Button;
import android.widget.Toast;

import android.graphics.drawable.Drawable;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays a map view using OpenStreetMap and handles user location updates.
 * Implements the {@link Searchable} interface for handling search functionality.
 */
public class MapViewFragment extends Fragment implements LocationListener, Searchable {

    private static final String TAG = "MapViewFragment";
    private static final int ZOOM_LEVEL = 16;
    private static final float MIN_ACCURACY = 100; // Minimum required accuracy for location updates
    private static final long MIN_TIME_BETWEEN_UPDATES = 10000; // Minimum time between location updates in milliseconds
    private static final long MIN_UPDATE_INTERVAL = 5000; // Minimum interval between updates in milliseconds
    private long lastUpdateTime = 0;

    private MapView mapView;
    private MapViewModel mapViewModel;
    private ViewModelFactory viewModelFactory;
    private LocationManager locationManager;
    private Marker userLocationMarker; // Marker indicating the user's current location
    private List<Marker> restaurantMarkers = new ArrayList<>(); // List of restaurant markers on the map
    private List<Restaurant> allRestaurants = new ArrayList<>(); // Complete list of restaurants

    /**
     * Called to have the fragment instantiate its user interface view.
     * Initializes the map view and its settings.
     *
     * @param inflater The LayoutInflater object used to inflate the views.
     * @param container The parent view that this fragment's UI should be attached to, or null.
     * @param savedInstanceState The saved instance state of the fragment, if any.
     * @return The root view for the fragment's UI.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_view, container, false);

        // Initialize the MapView
        mapView = view.findViewById(R.id.mapview);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        return view;
    }

    /**
     * Called when the fragment's view has been created.
     * Sets up ViewModel observers, initializes the map, and handles geolocation updates.
     *
     * @param view The view returned by {@link #onCreateView}.
     * @param savedInstanceState If non-null, this fragment is being re-created from a previous saved state.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        viewModelFactory = AppInjector.getInstance().getViewModelFactory();
        mapViewModel = new ViewModelProvider(this, viewModelFactory).get(MapViewModel.class);

        // Observe lunches for today
        mapViewModel.loadLunchesToday();
        mapViewModel.getLunchesToday().observe(getViewLifecycleOwner(), lunches -> {
            Log.d("MapViewFragment", "Lunches Today Size: " + (lunches != null ? lunches.size() : 0));
            List<Restaurant> detailedRestaurants = mapViewModel.getDetailedRestaurants().getValue();
            displayRestaurants(detailedRestaurants, lunches);
        });

        // Observe detailed restaurant data
        mapViewModel.getDetailedRestaurants().observe(getViewLifecycleOwner(), restaurants -> {
            List<Lunch> lunches = mapViewModel.getLunchesToday().getValue();
            displayRestaurants(restaurants, lunches);
        });

        // Observe the selected restaurant
        mapViewModel.getSelectedRestaurant().observe(getViewLifecycleOwner(), restaurant -> {
            if (restaurant != null) {
                openRestaurantDetailFragment(restaurant);
            }
        });

        // Initialize the map
        initializeMap();

        // Set up geolocation button
        Button btnGeolocate = view.findViewById(R.id.btn_geolocate);
        btnGeolocate.setOnClickListener(v -> geolocationAndUpdateMap());

        // Simulate geolocation button click on startup
        geolocationAndUpdateMap();
    }

    /**
     * Handles geolocation updates and refreshes the map with the user's location.
     * Requests location updates if no recent accurate location is available.
     */
    private void geolocationAndUpdateMap() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        if (locationManager == null) {
            locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
        }

        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnownLocation == null) {
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        // If no recent accurate location is found, request updates
        if (lastKnownLocation == null || !isLocationAccurate(lastKnownLocation)) {
            requestLocationUpdates();
        } else {
            onLocationChanged(lastKnownLocation);
        }
    }

    /**
     * Initializes the map view settings.
     * Configures the tile source, enables multi-touch controls, sets default zoom level,
     * and centers the map at a default location.
     */
    private void initializeMap() {
        Configuration.getInstance().setUserAgentValue(requireActivity().getPackageName());
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(ZOOM_LEVEL);

        GeoPoint startPoint = new GeoPoint(47.3123, 5.0914); // Default start point
        mapView.getController().setCenter(startPoint);
    }

    /**
     * Requests location updates from the LocationManager.
     * Checks for location permissions and listens for updates from both GPS and network providers.
     */
    private void requestLocationUpdates() {
        locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        // Listen for location updates from both GPS and network providers
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BETWEEN_UPDATES, 0, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BETWEEN_UPDATES, 0, this);
    }

    /**
     * Called when the user's location changes.
     * Updates the ViewModel and the map if the location is accurate and enough time has elapsed
     * since the last update.
     *
     * @param location The new location of the user.
     */
    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (isLocationAccurate(location)) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastUpdateTime > MIN_UPDATE_INTERVAL) {
                lastUpdateTime = currentTime;

                // Update location in ViewModel and on the map
                mapViewModel.setUserLocation(location);
                updateUserLocationOnMap(location);
                fetchAndDisplayRestaurants(location);
            }
        }
    }

    /**
     * Checks if the location's accuracy is within the acceptable range.
     *
     * @param location The location to check.
     * @return True if the location's accuracy is acceptable, false otherwise.
     */
    private boolean isLocationAccurate(Location location) {
        return location.getAccuracy() <= MIN_ACCURACY;
    }

    /**
     * Fetches and displays restaurants based on the user's location.
     * Initiates a detailed fetch for restaurants if needed.
     *
     * @param location The user's current location.
     */
    private void fetchAndDisplayRestaurants(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        mapViewModel.loadRestaurants(latitude, longitude);

        // Observe the list of restaurants and trigger bulk fetch for details if necessary
        mapViewModel.getRestaurants().observe(getViewLifecycleOwner(), restaurants -> {
            mapViewModel.fetchRestaurantsDetailsIfNeeded(restaurants);  // Load details if needed
        });
    }

    /**
     * Updates the user's position on the map.
     * Adds or moves a marker to indicate the user's location and centers the map on it.
     *
     * @param location The user's current location.
     */
    private void updateUserLocationOnMap(Location location) {
        if (mapView != null) {
            GeoPoint userLocation = new GeoPoint(location.getLatitude(), location.getLongitude());

            // If the user's marker already exists, update its position
            if (userLocationMarker == null) {
                userLocationMarker = new Marker(mapView);
                userLocationMarker.setTitle("Vous êtes ici");
                mapView.getOverlays().add(userLocationMarker);
            }

            userLocationMarker.setPosition(userLocation);
            userLocationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapView.getController().setCenter(userLocation);
            mapView.invalidate(); // Refresh the map
        } else {
            Log.e(TAG, "MapView is null, cannot update user location");
        }
    }

    /**
     * Displays the list of restaurants on the map with appropriate markers.
     * Clears existing markers and updates the map with the new list.
     *
     * @param restaurants The list of restaurants to display.
     * @param lunches The list of lunches for today, used to indicate if a restaurant is selected.
     */
    private void displayRestaurants(List<Restaurant> restaurants, @Nullable List<Lunch> lunches) {
        if (restaurants == null) return;

        // Clear existing restaurant markers
        clearRestaurantMarkers();
        restaurantMarkers.clear();
        allRestaurants.clear();

        allRestaurants.addAll(restaurants); // Store the full list of restaurants

        for (Restaurant restaurant : restaurants) {
            boolean hasLunch = lunches != null && hasLunch(lunches, restaurant);
            Marker marker = setupRestaurantMarker(restaurant, hasLunch);
            restaurantMarkers.add(marker); // Add the marker to the list
        }

        mapView.invalidate(); // Refresh the map
    }

    /**
     * Clears all restaurant markers from the map, keeping the user's location marker.
     */
    private void clearRestaurantMarkers() {
        List<Overlay> overlays = mapView.getOverlays();
        for (int i = overlays.size() - 1; i >= 0; i--) {
            if (overlays.get(i) instanceof Marker) {
                Marker marker = (Marker) overlays.get(i);
                if (!"Vous êtes ici".equals(marker.getTitle())) {
                    overlays.remove(i);
                }
            }
        }
    }

    /**
     * Checks if a restaurant is selected for lunch by any user.
     *
     * @param lunches The list of lunches for today.
     * @param restaurant The restaurant to check.
     * @return True if the restaurant is selected, false otherwise.
     */
    private boolean hasLunch(List<Lunch> lunches, Restaurant restaurant) {
        if (lunches == null || restaurant == null) return false;

        for (Lunch lunch : lunches) {
            if (lunch.getRestaurantId() != null && lunch.getRestaurantId().equals(restaurant.getRestaurantId())) {
                Log.d("MapViewFragment", "Lunch found for restaurant: " + restaurant.getName());
                return true;
            }
        }

        Log.d("MapViewFragment", "No lunch for restaurant: " + restaurant.getName());
        return false;
    }

    /**
     * Sets up a marker for a restaurant on the map.
     *
     * @param restaurant The restaurant to display.
     * @param hasLunch Indicates if the restaurant is selected for lunch.
     * @return The configured Marker for the restaurant.
     */
    private Marker setupRestaurantMarker(Restaurant restaurant, boolean hasLunch) {
        Marker restaurantMarker = new Marker(mapView);
        restaurantMarker.setPosition(restaurant.getLocation());
        restaurantMarker.setTitle(restaurant.getName());

        int markerResource = hasLunch ? R.drawable.ic_restaurant_marker_green : R.drawable.ic_restaurant_marker_red;
        Drawable markerIcon = ContextCompat.getDrawable(requireContext(), markerResource);
        if (markerIcon != null) {
            restaurantMarker.setIcon(markerIcon);
        }

        restaurantMarker.setOnMarkerClickListener((marker, mapView) -> {
            fetchYelpDetails(restaurant.getRestaurantId(), restaurant.getLocation(), restaurant.getName());
            return true;
        });

        mapView.getOverlays().add(restaurantMarker);
        return restaurantMarker; // Return the marker
    }

    /**
     * Fetches detailed restaurant information from Yelp.
     *
     * @param restaurantId The ID of the restaurant.
     * @param location The location of the restaurant.
     * @param restaurantName The name of the restaurant.
     */
    private void fetchYelpDetails(String restaurantId, GeoPoint location, String restaurantName) {
        mapViewModel.fetchRestaurantDetails(restaurantId, location, restaurantName);
    }

    /**
     * Opens the RestaurantDetailFragment and passes the restaurant details as arguments.
     *
     * @param restaurant The restaurant to display in detail.
     */
    private void openRestaurantDetailFragment(Restaurant restaurant) {
        Bundle bundle = new Bundle();
        bundle.putString("restaurantId", restaurant.getRestaurantId());
        bundle.putString("restaurantName", restaurant.getName());
        bundle.putString("restaurantAddress", restaurant.getAddress());
        bundle.putString("restaurantPhotoUrl", restaurant.getPhotoUrl());
        bundle.putDouble("restaurantRating", restaurant.getRating());
        bundle.putDouble("latitude", restaurant.getLocation().getLatitude());
        bundle.putDouble("longitude", restaurant.getLocation().getLongitude());
        bundle.putString("restaurantPhoneNumber", restaurant.getPhoneNumber());
        bundle.putString("restaurantWebsite", restaurant.getYelpUrl());

        RestaurantDetailFragment fragment = new RestaurantDetailFragment();
        fragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Filters the markers displayed on the map based on a search query.
     *
     * @param query The search query entered by the user.
     */
    private void filterMarkers(String query) {
        if (query == null || query.trim().isEmpty()) {
            // Show all markers if the search query is empty
            for (Marker marker : restaurantMarkers) {
                marker.setEnabled(true);
                marker.setVisible(true);
            }
        } else {
            // Filter markers based on the query
            for (int i = 0; i < allRestaurants.size(); i++) {
                Marker marker = restaurantMarkers.get(i);
                Restaurant restaurant = allRestaurants.get(i);

                if (restaurant.getName().toLowerCase().contains(query.toLowerCase())) {
                    marker.setEnabled(true);
                    marker.setVisible(true);
                } else {
                    marker.setEnabled(false);
                    marker.setVisible(false);
                }
            }
        }
        mapView.invalidate(); // Refresh the map
    }

    /**
     * Sorts restaurants based on the given criterion.
     *
     * @param criterion The sorting criterion (e.g., distance, rating).
     */
    @Override
    public void onSort(String criterion) {
        // No sorting logic needed for this
    }

    /**
     * Filters markers based on a search query entered by the user.
     *
     * @param query The search query.
     */
    @Override
    public void onSearch(String query) {
        filterMarkers(query);
    }

    /**
     * Called when the fragment is resumed. Resumes the map view.
     */
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * Called when the fragment is paused. Pauses the map view and stops location updates.
     */
    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    /**
     * Called when the fragment is destroyed. Detaches the map view.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDetach();
    }
}