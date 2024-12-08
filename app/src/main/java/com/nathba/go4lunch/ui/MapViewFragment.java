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
    private boolean isInitialFetchDone = false;  // Indicateur pour le chargement initial des détails
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

        // Observer pour les restaurants détaillés
        mapViewModel.getDetailedRestaurants().observe(getViewLifecycleOwner(), restaurants -> {
            List<Lunch> lunches = mapViewModel.getLunches().getValue();
            displayRestaurants(restaurants, lunches);
        });

        // Observer le restaurant sélectionné pour ouvrir les détails
        mapViewModel.getSelectedRestaurant().observe(getViewLifecycleOwner(), restaurant -> {
            if (restaurant != null) {
                openRestaurantDetailFragment(restaurant);
            }
        });

        // Setup the map view
        initializeMap();

        // Bouton de géolocalisation
        Button btnGeolocate = view.findViewById(R.id.btn_geolocate);
        btnGeolocate.setOnClickListener(v -> geolocationAndUpdateMap());

        // Simuler un clic sur le bouton géolocaliser à l'ouverture
        geolocationAndUpdateMap();
    }

    /**
     * Récupère la position de l'utilisateur, met à jour la carte et charge les restaurants.
     */
    private void geolocationAndUpdateMap() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        LocationManager locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnownLocation == null) {
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (lastKnownLocation != null && isLocationAccurate(lastKnownLocation)) {
            updateUserLocationOnMap(lastKnownLocation); // Met à jour la carte
            fetchAndDisplayRestaurants(lastKnownLocation); // Charge les restaurants
        } else {
            Toast.makeText(requireContext(), "Impossible de récupérer votre position.", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (isLocationAccurate(location)) {
            mapViewModel.setUserLocation(location);

            // Charger les restaurants pour la localisation de l'utilisateur
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            mapViewModel.loadRestaurants(latitude, longitude);

            if (!isInitialFetchDone) {
                fetchAndDisplayRestaurants(location);  // Charger les restaurants avec détails la première fois
                isInitialFetchDone = true;
            }

            if (!isInitialCenteringDone) {
                updateUserLocationOnMap(location);
                isInitialCenteringDone = true;  // Indiquer que le recentrage initial est effectué
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

    /**
     * Affiche les restaurants avec des marqueurs sur la carte.
     * @param restaurants La liste des restaurants.
     * @param lunches La liste des lunchs pour vérifier les associations avec les restaurants.
     */
    private void displayRestaurants(List<Restaurant> restaurants, @Nullable List<Lunch> lunches) {
        if (restaurants == null) return;

        // Effacer les anciens marqueurs, sauf celui de la position de l'utilisateur
        List<Overlay> overlays = mapView.getOverlays();
        for (int i = overlays.size() - 1; i >= 0; i--) {
            if (overlays.get(i) instanceof Marker) {
                Marker marker = (Marker) overlays.get(i);
                if (!"Vous êtes ici".equals(marker.getTitle())) {
                    overlays.remove(i);
                }
            }
        }

        // Ajouter les nouveaux marqueurs
        for (Restaurant restaurant : restaurants) {
            boolean hasLunch = lunches != null && hasLunch(lunches, restaurant);
            setupRestaurantMarker(restaurant, hasLunch);
        }

        mapView.invalidate();  // Actualiser la carte
    }

    private boolean hasLunch(List<Lunch> lunches, Restaurant restaurant) {
        if (lunches == null) return false;

        for (Lunch lunch : lunches) {
            if (lunch.getRestaurantId() != null && lunch.getRestaurantId().equals(restaurant.getRestaurantId())) {
                return true;
            }
        }
        return false;
    }

    private void setupRestaurantMarker(Restaurant restaurant, boolean hasLunch) {
        Marker restaurantMarker = new Marker(mapView);
        restaurantMarker.setPosition(restaurant.getLocation());
        restaurantMarker.setTitle(restaurant.getName());

        Drawable markerIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_restaurant_marker);
        if (markerIcon != null) {
            int color = hasLunch ? android.graphics.Color.GREEN : android.graphics.Color.RED;
            markerIcon.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
            restaurantMarker.setIcon(markerIcon);
        }

        restaurantMarker.setOnMarkerClickListener((marker, mapView) -> {
            GeoPoint location = restaurant.getLocation();
            String restaurantName = restaurant.getName();
            fetchYelpDetails(restaurant.getRestaurantId(), location, restaurantName);
            return true;
        });

        mapView.getOverlays().add(restaurantMarker);
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