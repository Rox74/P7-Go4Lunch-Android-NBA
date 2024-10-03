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

import java.util.ArrayList;
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
    private List<Restaurant> restaurantList = new ArrayList<>();

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

    /**
     * Observer les données des restaurants et la position de l'utilisateur.
     */
    private void observeViewModel() {
        // Observer les données des restaurants
        mapViewModel.getRestaurants().observe(getViewLifecycleOwner(), restaurants -> {
            List<Lunch> lunches = mapViewModel.getLunches().getValue();
            displayRestaurants(restaurants, lunches);  // Passer les lunchs pour vérifier si un restaurant est associé
        });
        mapViewModel.getUserLocation().observe(getViewLifecycleOwner(), this::updateUserLocationOnMap);
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

            // Appeler MapRepository pour charger les restaurants en fonction de la localisation de l'utilisateur
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            mapViewModel.loadRestaurants(latitude, longitude);  // Appel avec les coordonnées

            if (!isInitialCenteringDone) {
                updateUserLocationOnMap(location);
                isInitialCenteringDone = true;  // Indiquer que le recentrage initial est effectué
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
            mapView.invalidate();  // Actualiser la carte
        } else {
            Log.e(TAG, "MapView is null, cannot update user location");
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
            boolean hasLunch = lunches != null && hasLunch(lunches, restaurant);  // Vérifier si le restaurant est associé à un lunch
            setupRestaurantMarker(restaurant, hasLunch);  // Utiliser la méthode utilitaire
        }

        mapView.invalidate();  // Actualiser la carte
    }

    private void fetchYelpDetails(String restaurantId, GeoPoint location, String restaurantName) {
        RestaurantRepository restaurantRepository = new RestaurantRepository();

        // Appel à Yelp pour obtenir les détails
        restaurantRepository.getRestaurantDetails(restaurantId, location, restaurantName, new RepositoryCallback<Restaurant>() {
            @Override
            public void onSuccess(Restaurant restaurant) {
                // Si on récupère des détails depuis Yelp, on ouvre la page de détails avec ces infos
                openRestaurantDetailFragment(restaurant);
            }

            @Override
            public void onError(Throwable t) {
                // Si on ne peut pas obtenir les détails via Yelp, utiliser les infos basiques
                Log.e("MapViewFragment", "Failed to retrieve Yelp details, using basic info");
                Restaurant basicRestaurant = new Restaurant(restaurantId, restaurantName, null, null, 0, location);
                openRestaurantDetailFragment(basicRestaurant);
                Toast.makeText(requireContext(), "Yelp details not available, using basic info", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Méthode pour ouvrir la page de détails du restaurant
    private void openRestaurantDetailFragment(Restaurant restaurant) {
        // Préparer le bundle avec les infos du restaurant
        Bundle bundle = new Bundle();
        bundle.putString("restaurantId", restaurant.getRestaurantId());
        bundle.putString("restaurantName", restaurant.getName());
        bundle.putString("restaurantAddress", restaurant.getAddress());
        bundle.putString("restaurantPhotoUrl", restaurant.getPhotoUrl());
        bundle.putDouble("restaurantRating", restaurant.getRating());
        bundle.putDouble("latitude", restaurant.getLocation().getLatitude());
        bundle.putDouble("longitude", restaurant.getLocation().getLongitude());

        // Créer le fragment de détail et lui passer le bundle
        RestaurantDetailFragment fragment = new RestaurantDetailFragment();
        fragment.setArguments(bundle);

        // Remplacer le fragment actuel par le fragment de détails
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void setupRestaurantMarker(Restaurant restaurant, boolean hasLunch) {
        Marker restaurantMarker = new Marker(mapView);
        restaurantMarker.setPosition(restaurant.getLocation());
        restaurantMarker.setTitle(restaurant.getName());

        // Définir l'icône du marqueur en fonction de si le restaurant est associé à un lunch
        Drawable markerIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_restaurant_marker);

        if (markerIcon != null) {
            int color = hasLunch ? android.graphics.Color.GREEN : android.graphics.Color.RED;
            markerIcon.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
            restaurantMarker.setIcon(markerIcon);
        }

        // Rendre le marqueur cliquable
        restaurantMarker.setOnMarkerClickListener((marker, mapView) -> {
            GeoPoint location = restaurant.getLocation();
            String restaurantName = restaurant.getName();
            fetchYelpDetails(restaurant.getRestaurantId(), location, restaurantName); // Détails du restaurant via Yelp
            return true;
        });

        mapView.getOverlays().add(restaurantMarker);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

        // Restaurer les restaurants et leurs marqueurs sur la carte
        if (!restaurantList.isEmpty()) {
            displayRestaurants(restaurantList, mapViewModel.getLunches().getValue());
        } else {
            // Si la liste est vide, la recharger (optionnel)
            // Observer les données des restaurants
            mapViewModel.getRestaurants().observe(getViewLifecycleOwner(), restaurants -> {
                List<Lunch> lunches = mapViewModel.getLunches().getValue();
                displayRestaurants(restaurants, lunches);  // Passer les lunchs pour vérifier si un restaurant est associé
            });
        }
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