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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.nathba.go4lunch.R;
import com.nathba.go4lunch.application.MapViewModel;
import com.nathba.go4lunch.models.Restaurant;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import android.location.LocationListener;
import android.widget.Toast;

import java.util.List;

public class MapViewFragment extends Fragment implements LocationListener {
    private static final String TAG = "MapViewFragment";
    private static final int ZOOM_LEVEL = 16;
    private static final float MIN_ACCURACY = 100; // en mètres
    private static final long MIN_TIME_BETWEEN_UPDATES = 10000; // 10 secondes

    private MapView mapView;
    private MapViewModel viewModel;
    private LocationManager locationManager;
    private Marker userLocationMarker;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_view, container, false);
        mapView = view.findViewById(R.id.mapview);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MapViewModel.class);
        initializeMap();
        observeViewModel();
        requestLocationUpdates();
    }

    private void initializeMap() {
        Configuration.getInstance().setUserAgentValue(requireActivity().getPackageName());
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(ZOOM_LEVEL);

        GeoPoint startPoint = new GeoPoint(48.8566, 2.3522);
        mapView.getController().setCenter(startPoint);
    }

    private void observeViewModel() {
        viewModel.getRestaurants().observe(getViewLifecycleOwner(), this::displayRestaurants);
        viewModel.getUserLocation().observe(getViewLifecycleOwner(), this::updateUserLocationOnMap);
    }

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
            viewModel.setUserLocation(location);
        }
    }

    private boolean isLocationAccurate(Location location) {
        return location.getAccuracy() <= MIN_ACCURACY;
    }

    private void updateUserLocationOnMap(Location location) {
        GeoPoint userLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
        mapView.getController().animateTo(userLocation);

        if (userLocationMarker == null) {
            userLocationMarker = new Marker(mapView);
            userLocationMarker.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_user_location));
            userLocationMarker.setTitle("Vous êtes ici");
            mapView.getOverlays().add(userLocationMarker);
        }
        userLocationMarker.setPosition(userLocation);
        mapView.invalidate();
    }

    private void displayRestaurants(List<Restaurant> restaurants) {
        for (Restaurant restaurant : restaurants) {
            Marker restaurantMarker = new Marker(mapView);
            restaurantMarker.setPosition(restaurant.getLocation());
            restaurantMarker.setTitle(restaurant.getName());
            restaurantMarker.setSnippet("Restaurant");
            restaurantMarker.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_restaurant_marker));
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