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
import androidx.navigation.fragment.NavHostFragment;

import com.nathba.go4lunch.R;
import com.nathba.go4lunch.application.MapViewModel;
import com.nathba.go4lunch.application.ViewModelFactory;
import com.nathba.go4lunch.di.AppInjector;
import com.nathba.go4lunch.models.Lunch;
import com.nathba.go4lunch.models.Restaurant;
import com.nathba.go4lunch.repository.RepositoryCallback;
import com.nathba.go4lunch.repository.RestaurantRepository;

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

import java.util.List;

/**
 * Fragment that displays a map view using OpenStreetMap and handles user location updates.
 */
public class MapViewFragment extends Fragment implements LocationListener {

    private static final String TAG = "MapViewFragment";
    private static final int ZOOM_LEVEL = 16;
    private static final float MIN_ACCURACY = 100;
    private static final long MIN_TIME_BETWEEN_UPDATES = 10000;

    private MapView mapView;
    private MapViewModel mapViewModel;
    private ViewModelFactory viewModelFactory;
    private LocationManager locationManager;
    private Marker userLocationMarker;
    private boolean isInitialCenteringDone = false;

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Obtain ViewModelFactory from AppInjector
        viewModelFactory = AppInjector.getInstance().getViewModelFactory();

        // Initialize MapViewModel using ViewModelFactory
        mapViewModel = new ViewModelProvider(this, viewModelFactory).get(MapViewModel.class);

        // Setup the map view
        initializeMap();

        // Observe changes in the ViewModel
        observeViewModel();

        // Request location updates from the system
        requestLocationUpdates();

        // Setup geolocation button
        Button btnGeolocate = view.findViewById(R.id.btn_geolocate);
        btnGeolocate.setOnClickListener(v -> {
            Location location = mapViewModel.getUserLocation().getValue();
            if (location != null) {
                updateUserLocationOnMap(location); // Recentre la carte sur la position de l'utilisateur
            } else {
                Toast.makeText(requireContext(), "La position de l'utilisateur est inconnue.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Initializes the map view settings.
     */
    private void initializeMap() {
        Configuration.getInstance().setUserAgentValue(requireActivity().getPackageName());
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(ZOOM_LEVEL);

        GeoPoint startPoint = new GeoPoint(47.3123, 5.0914); // Default
        mapView.getController().setCenter(startPoint);
    }

    private void observeViewModel() {
        mapViewModel.getRestaurants().observe(getViewLifecycleOwner(), this::displayRestaurants);
        mapViewModel.getLunches().observe(getViewLifecycleOwner(), lunches -> {
            List<Restaurant> restaurants = mapViewModel.getRestaurants().getValue();
            if (restaurants != null) {
                displayRestaurants(restaurants, lunches); // Appel correct avec restaurants et lunches
            }
        });
    }

    /**
     * Requests location updates from the LocationManager.
     */
    private void requestLocationUpdates() {
        locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BETWEEN_UPDATES, 0, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BETWEEN_UPDATES, 0, this);

        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnownLocation == null) {
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if (lastKnownLocation != null && isLocationAccurate(lastKnownLocation)) {
            onLocationChanged(lastKnownLocation);
        }
    }

    /**
     * Called when the location changes. Updates the ViewModel and map with the new location.
     * @param location The updated location.
     */
    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (isLocationAccurate(location) && mapView != null) {
            mapViewModel.setUserLocation(location);
            if (!isInitialCenteringDone) {
                updateUserLocationOnMap(location);
                isInitialCenteringDone = true; // Indicate that initial centering is done
            }
        }
    }

    /**
     * Checks if the location accuracy is within the acceptable range.
     * @param location The location to check.
     * @return True if the location accuracy is acceptable, otherwise false.
     */
    private boolean isLocationAccurate(Location location) {
        return location.getAccuracy() <= MIN_ACCURACY;
    }

    private void updateUserLocationOnMap(Location location) {
        if (mapView != null) {
            GeoPoint userLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
            Marker marker = new Marker(mapView);
            marker.setPosition(userLocation);
            marker.setTitle("You are here");
            mapView.getOverlays().add(marker);
            mapView.invalidate(); // Refresh the map
        } else {
            Log.e("MapViewFragment", "MapView is null, cannot update user location");
        }
    }

    /**
     * Checks if there is a lunch associated with the given restaurant.
     * @param lunches The list of lunches to check.
     * @param restaurant The restaurant to check.
     * @return True if there is a lunch associated with the restaurant, otherwise false.
     */
    private boolean hasLunch(List<Lunch> lunches, Restaurant restaurant) {
        if (lunches == null) return false;

        for (Lunch lunch : lunches) {
            if (lunch.getRestaurantId() != null && lunch.getRestaurantId().equals(restaurant.getRestaurantId())) {
                return true;
            }
        }
        return false;
    }

    private void displayRestaurants(List<Restaurant> restaurants) {
        for (Restaurant restaurant : restaurants) {
            if (restaurant.getRestaurantId() == null) {
                Log.e("MapViewFragment", "Restaurant ID is null for restaurant: " + restaurant.getName());
                continue;
            }

            Marker restaurantMarker = new Marker(mapView);
            restaurantMarker.setPosition(restaurant.getLocation());
            restaurantMarker.setTitle(restaurant.getName());

            restaurantMarker.setOnMarkerClickListener((marker, mapView) -> {
                Log.d("MapViewFragment", "Restaurant selected: " + restaurant.getName());

                // Récupérer les coordonnées GPS et le nom du restaurant
                GeoPoint location = restaurant.getLocation();
                String restaurantName = restaurant.getName();

                // Appel à Yelp pour obtenir plus de détails
                fetchYelpDetails(restaurant.getRestaurantId(), location, restaurantName);

                return true;
            });

            mapView.getOverlays().add(restaurantMarker);
        }
    }

    private void fetchYelpDetails(String restaurantId, GeoPoint location, String restaurantName) {
        RestaurantRepository restaurantRepository = new RestaurantRepository();
        restaurantRepository.getRestaurantDetails(restaurantId, location, restaurantName, new RepositoryCallback<Restaurant>() {
            @Override
            public void onSuccess(Restaurant restaurant) {
                Log.d("MapViewFragment", "Yelp details retrieved successfully for: " + restaurant.getName() + " ," + restaurant.getAddress() + " ," + restaurant.getPhoneNumber() + " ," + restaurant.getRating() + " ," + restaurant.getPhotoUrl());
            }

            @Override
            public void onError(Throwable t) {
                Log.e("MapViewFragment", "Failed to retrieve Yelp details: " + t.getMessage());
            }
        });
    }

    /**
     * Displays the restaurants on the map, applying different colors to markers based on whether
     * the restaurant is associated with a lunch.
     * @param restaurants The list of restaurants to display.
     * @param lunches The list of lunches.
     */
    private void displayRestaurants(List<Restaurant> restaurants, List<Lunch> lunches) {
        if (restaurants == null) return;

        // Remove all markers except for the user's location marker
        List<Overlay> overlays = mapView.getOverlays();
        for (int i = overlays.size() - 1; i >= 0; i--) {
            if (overlays.get(i) instanceof Marker) {
                Marker marker = (Marker) overlays.get(i);
                if (!"Vous êtes ici".equals(marker.getTitle())) {
                    overlays.remove(i);
                }
            }
        }

        for (Restaurant restaurant : restaurants) {
            Marker restaurantMarker = new Marker(mapView);
            restaurantMarker.setPosition(restaurant.getLocation());
            restaurantMarker.setTitle(restaurant.getName());
            restaurantMarker.setSnippet("Restaurant");

            boolean hasLunch = hasLunch(lunches, restaurant);

            // Set marker icon with color filter based on lunch association
            Drawable markerIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_restaurant_marker);

            if (markerIcon != null) {
                int color = hasLunch ? android.graphics.Color.GREEN : android.graphics.Color.RED;
                markerIcon.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
                restaurantMarker.setIcon(markerIcon);
            }

            restaurantMarker.setOnMarkerClickListener((marker, mapView) -> {
                Toast.makeText(requireContext(), "Restaurant sélectionné : " + marker.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            });

            mapView.getOverlays().add(restaurantMarker);
        }
        mapView.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDetach();
    }
}