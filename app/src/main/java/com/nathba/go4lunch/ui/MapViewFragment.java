package com.nathba.go4lunch.ui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.nathba.go4lunch.R;
import com.nathba.go4lunch.models.OverpassResponse;
import com.nathba.go4lunch.api.OverpassApi;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.location.LocationListener;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapViewFragment extends Fragment implements LocationListener {

    private static final String TAG = "MapViewFragment";
    private static final int ZOOM_LEVEL = 16;
    private static final int RESTAURANT_SEARCH_RADIUS = 1000; // en mètres
    private static final float MIN_ACCURACY = 100; // en mètres
    private static final long MIN_TIME_BETWEEN_UPDATES = 10000; // 10 secondes

    private MapView mapView;
    private OverpassApi overpassApi;
    private LocationManager locationManager;
    private Location currentLocation;
    private boolean isInitialLocationSet = false;
    private boolean isMapInitialized = false; // Pour éviter de réinitialiser la carte inutilement
    private boolean isViewCreated = false;
    private Marker userLocationMarker;
    private long lastUpdateTime = 0;
    private List<OverpassResponse.Element> pendingRestaurants = new ArrayList<>();
    private boolean isMapReady = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_view, container, false);
        mapView = view.findViewById(R.id.mapview);
        overpassApi = new OverpassApi();
        isViewCreated = true;
        return view;
    }

    private void initializeMapIfNeeded() {
        if (isViewCreated && getUserVisibleHint() && !isMapInitialized && mapView != null) {
            initializeMap();
            requestLocationUpdates();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            initializeMapIfNeeded();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initializeMapIfNeeded();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDetach();
        }
    }

    private void initializeMap() {
        if (getContext() == null) return;

        Configuration.getInstance().setUserAgentValue(requireActivity().getPackageName());
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(ZOOM_LEVEL);

        GeoPoint startPoint = new GeoPoint(48.8566, 2.3522);
        mapView.getController().setCenter(startPoint);
        isMapInitialized = true;
        isMapReady = true;

        // Affichez les restaurants en attente
        if (!pendingRestaurants.isEmpty()) {
            displayRestaurants(pendingRestaurants);
            pendingRestaurants.clear();
        }
    }

    private void requestLocationUpdates() {
        if (getContext() == null) return; // Vérification du contexte

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
        Log.d(TAG, "Location changed: Lat = " + location.getLatitude() + ", Lon = " + location.getLongitude() + ", Accuracy = " + location.getAccuracy());

        if (isLocationAccurate(location) && isTimeDifferenceSignificant()) {
            currentLocation = location;
            updateMapLocation();
            if (!isInitialLocationSet) {
                loadRestaurants();
                isInitialLocationSet = true;
            }
            lastUpdateTime = System.currentTimeMillis();
        }
    }

    private boolean isLocationAccurate(Location location) {
        return location.getAccuracy() <= MIN_ACCURACY;
    }

    private boolean isTimeDifferenceSignificant() {
        return System.currentTimeMillis() - lastUpdateTime >= MIN_TIME_BETWEEN_UPDATES;
    }

    private void updateMapLocation() {
        if (currentLocation != null && mapView != null) {
            GeoPoint userLocation = new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
            mapView.getController().animateTo(userLocation);
            updateUserLocationMarker(userLocation);
        } else {
            Log.e(TAG, "MapView or currentLocation is null in updateMapLocation");
        }
    }

    private void updateUserLocationMarker(GeoPoint userLocation) {
        if (getContext() == null || mapView == null) {
            Log.e(TAG, "Context or MapView is null in updateUserLocationMarker");
            return;
        }

        if (userLocationMarker == null) {
            userLocationMarker = new Marker(mapView);
            userLocationMarker.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_user_location));
            userLocationMarker.setTitle("Vous êtes ici");
            mapView.getOverlays().add(userLocationMarker);
        }
        userLocationMarker.setPosition(userLocation);
        mapView.invalidate();
    }

    private void loadRestaurants() {
        if (currentLocation == null || mapView == null) {
            Log.d(TAG, "Current location or MapView is null.");
            return;
        }

        String overpassQuery = String.format(Locale.US,
                "[out:json];node[\"amenity\"=\"restaurant\"](around:%d,%f,%f);out;",
                RESTAURANT_SEARCH_RADIUS, currentLocation.getLatitude(), currentLocation.getLongitude()
        );

        Log.d(TAG, "Overpass Query: " + overpassQuery);

        overpassApi.getRestaurants(overpassQuery).enqueue(new Callback<OverpassResponse>() {
            @Override
            public void onResponse(@NonNull Call<OverpassResponse> call, @NonNull Response<OverpassResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (isMapReady) {
                        displayRestaurants(response.body().elements);
                    } else {
                        pendingRestaurants.addAll(response.body().elements);
                    }
                } else {
                    Log.e(TAG, "Error response: " + (response.errorBody() != null ? response.errorBody().toString() : "Unknown error"));
                    showErrorToast(R.string.error_loading_restaurants);
                }
            }

            @Override
            public void onFailure(@NonNull Call<OverpassResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Request failed: " + t.getMessage(), t);
                showErrorToast(R.string.error_loading_restaurants);
            }
        });
    }

    private void displayRestaurants(List<OverpassResponse.Element> restaurants) {
        if (!isMapReady || mapView == null) {
            // Si la carte n'est pas prête, ajoutez les restaurants à la file d'attente
            pendingRestaurants.addAll(restaurants);
            return;
        }

        if (restaurants == null || restaurants.isEmpty()) {
            Log.d(TAG, "No restaurants found");
            return;
        }

        Log.d(TAG, "Displaying " + restaurants.size() + " restaurants");
        if (mapView == null || !isMapInitialized) {
            Log.d(TAG, "MapView is null or not initialized in displayRestaurants");
            return;
        }

        for (OverpassResponse.Element restaurant : restaurants) {
            Marker restaurantMarker = new Marker(mapView);
            restaurantMarker.setPosition(new GeoPoint(restaurant.lat, restaurant.lon));
            restaurantMarker.setTitle(restaurant.tags.name != null ? restaurant.tags.name : "Restaurant sans nom");
            restaurantMarker.setSnippet("Restaurant");
            restaurantMarker.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_restaurant_marker));
            restaurantMarker.setOnMarkerClickListener((marker, mapView) -> {
                // TODO: Ouvrir le fragment de détail du restaurant
                Toast.makeText(requireContext(), "Restaurant sélectionné : " + marker.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            });
            mapView.getOverlays().add(restaurantMarker);
        }
        mapView.invalidate();
    }

    private void showErrorToast(@StringRes int messageResId) {
        if (getContext() != null) {
            Toast.makeText(requireContext(), messageResId, Toast.LENGTH_SHORT).show();
        }
    }
}