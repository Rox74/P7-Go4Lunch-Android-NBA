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
 */
public class MapViewFragment extends Fragment implements LocationListener, Searchable {

    private static final String TAG = "MapViewFragment";
    private static final int ZOOM_LEVEL = 16;
    private static final float MIN_ACCURACY = 100;
    private static final long MIN_TIME_BETWEEN_UPDATES = 10000;
    private static final long MIN_UPDATE_INTERVAL = 5000; // 5 secondes
    private long lastUpdateTime = 0;

    private MapView mapView;
    private MapViewModel mapViewModel;
    private ViewModelFactory viewModelFactory;
    private LocationManager locationManager;
    private Marker userLocationMarker;
    private List<Marker> restaurantMarkers = new ArrayList<>(); // Liste des marqueurs des restaurants
    private List<Restaurant> allRestaurants = new ArrayList<>(); // Liste complète des restaurants

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

        // Initialize ViewModel
        viewModelFactory = AppInjector.getInstance().getViewModelFactory();
        mapViewModel = new ViewModelProvider(this, viewModelFactory).get(MapViewModel.class);

        // Observer for lunches today
        mapViewModel.loadLunchesToday();
        mapViewModel.getLunchesToday().observe(getViewLifecycleOwner(), lunches -> {
            Log.d("MapViewFragment", "Lunches Today Size: " + (lunches != null ? lunches.size() : 0));
            List<Restaurant> detailedRestaurants = mapViewModel.getDetailedRestaurants().getValue();
            displayRestaurants(detailedRestaurants, lunches);
        });

        // Observer for detailed restaurants
        mapViewModel.getDetailedRestaurants().observe(getViewLifecycleOwner(), restaurants -> {
            List<Lunch> lunches = mapViewModel.getLunchesToday().getValue();
            displayRestaurants(restaurants, lunches);
        });

        // Observer for selected restaurant
        mapViewModel.getSelectedRestaurant().observe(getViewLifecycleOwner(), restaurant -> {
            if (restaurant != null) {
                openRestaurantDetailFragment(restaurant);
            }
        });

        // Setup map
        initializeMap();

        // Geolocation button
        Button btnGeolocate = view.findViewById(R.id.btn_geolocate);
        btnGeolocate.setOnClickListener(v -> geolocationAndUpdateMap());

        // Simulate geolocate button click on startup
        geolocationAndUpdateMap();
    }

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

        // Si aucune localisation récente, demander des mises à jour
        if (lastKnownLocation == null || !isLocationAccurate(lastKnownLocation)) {
            requestLocationUpdates();
        } else {
            onLocationChanged(lastKnownLocation);
        }
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

    /**
     * Requests location updates from the LocationManager.
     */
    private void requestLocationUpdates() {
        locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        // Écouter à la fois le GPS et le réseau pour des mises à jour fréquentes
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BETWEEN_UPDATES, 0, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BETWEEN_UPDATES, 0, this);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (isLocationAccurate(location)) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastUpdateTime > MIN_UPDATE_INTERVAL) {
                lastUpdateTime = currentTime;

                // Mettre à jour la localisation dans ViewModel et sur la carte
                mapViewModel.setUserLocation(location);
                updateUserLocationOnMap(location);
                fetchAndDisplayRestaurants(location);
            }
        }
    }

    /**
     * Vérifie si la précision de la localisation est dans la plage acceptable.
     * @param location La localisation à vérifier.
     * @return True si la précision de la localisation est acceptable, sinon false.
     */
    private boolean isLocationAccurate(Location location) {
        return location.getAccuracy() <= MIN_ACCURACY;
    }

    /**
     * Récupère et affiche les restaurants en fonction de la localisation avec des détails complets.
     * @param location La localisation de l'utilisateur.
     */
    private void fetchAndDisplayRestaurants(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        mapViewModel.loadRestaurants(latitude, longitude);

        // Observer la liste des restaurants et déclencher le fetch en bulk si nécessaire
        mapViewModel.getRestaurants().observe(getViewLifecycleOwner(), restaurants -> {
            mapViewModel.fetchRestaurantsDetailsIfNeeded(restaurants);  // Charger les détails si besoin
        });
    }

    /**
     * Met à jour la position de l'utilisateur sur la carte.
     * @param location La localisation de l'utilisateur.
     */
    private void updateUserLocationOnMap(Location location) {
        if (mapView != null) {
            GeoPoint userLocation = new GeoPoint(location.getLatitude(), location.getLongitude());

            // Si le marqueur de l'utilisateur existe déjà, mettre à jour sa position
            if (userLocationMarker == null) {
                userLocationMarker = new Marker(mapView);
                userLocationMarker.setTitle("Vous êtes ici");
                mapView.getOverlays().add(userLocationMarker);
            }

            userLocationMarker.setPosition(userLocation);
            userLocationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapView.getController().setCenter(userLocation);
            mapView.invalidate(); // Actualiser la carte
        } else {
            Log.e(TAG, "MapView is null, cannot update user location");
        }
    }

    private void displayRestaurants(List<Restaurant> restaurants, @Nullable List<Lunch> lunches) {
        if (restaurants == null) return;

        // Vider les anciens marqueurs (sauf localisation utilisateur)
        clearRestaurantMarkers();
        restaurantMarkers.clear();
        allRestaurants.clear();

        allRestaurants.addAll(restaurants); // Stocker la liste complète

        for (Restaurant restaurant : restaurants) {
            boolean hasLunch = lunches != null && hasLunch(lunches, restaurant);
            Marker marker = setupRestaurantMarker(restaurant, hasLunch);
            restaurantMarkers.add(marker); // Ajouter le marqueur à la liste
        }

        mapView.invalidate(); // Rafraîchir la carte
    }

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
        return restaurantMarker; // Retourner le marqueur
    }

    private void fetchYelpDetails(String restaurantId, GeoPoint location, String restaurantName) {
        mapViewModel.fetchRestaurantDetails(restaurantId, location, restaurantName);
    }

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

    private void filterMarkers(String query) {
        if (query == null || query.trim().isEmpty()) {
            // Afficher tous les marqueurs si la recherche est vide
            for (Marker marker : restaurantMarkers) {
                marker.setEnabled(true);
                marker.setVisible(true);
            }
        } else {
            // Filtrer les marqueurs en fonction de la requête
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
        mapView.invalidate(); // Rafraîchir la carte
    }

    @Override
    public void onSort(String criterion) {
        // Tri des restaurants si nécessaire
    }

    @Override
    public void onSearch(String query) {
        filterMarkers(query);
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