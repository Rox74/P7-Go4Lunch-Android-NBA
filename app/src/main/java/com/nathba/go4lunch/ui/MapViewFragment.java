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
import androidx.core.app.ActivityCompat;
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

import org.osmdroid.api.IMapController;

public class MapViewFragment extends Fragment implements LocationListener {

    private MapView mapView;
    private OverpassApi overpassApi;
    private LocationManager locationManager;
    private Location currentLocation;
    private boolean isInitialLocationSet = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_view, container, false);
        mapView = view.findViewById(R.id.mapview);

        // Initialiser OverpassApi
        overpassApi = new OverpassApi();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Initialiser la carte et demander les mises à jour de localisation
        if (mapView != null) {
            initializeMap();
        }
        requestLocationUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Arrêter les mises à jour de localisation pour économiser les ressources
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    private void initializeMap() {
        // Configurer OsmDroid User-Agent
        Configuration.getInstance().setUserAgentValue("VotreAppName/1.0");

        // Configurer la source des tuiles et les contrôles multi-touch
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        // Initialiser la carte avec une position par défaut (par exemple, Paris)
        GeoPoint startPoint = new GeoPoint(48.8566, 2.3522); // Paris
        IMapController mapController = mapView.getController();
        mapController.setCenter(startPoint);
        mapController.setZoom(10);
    }

    private void requestLocationUpdates() {
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        // Demander des mises à jour de localisation
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, this);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Log.d("MapViewFragment", "Location changed: Latitude = " + location.getLatitude() + ", Longitude = " + location.getLongitude());

        if (mapView == null) {
            Log.e("MapViewFragment", "MapView is null in onLocationChanged");
            return;
        }

        // Vérifier si la localisation initiale est déjà définie
        if (!isInitialLocationSet) {
            currentLocation = location;
            updateMapLocation();
            isInitialLocationSet = true;
        }
    }

    private void updateMapLocation() {
        if (currentLocation != null && mapView != null) {
            GeoPoint userLocation = new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
            IMapController mapController = mapView.getController();
            mapController.setCenter(userLocation);
            mapController.setZoom(15); // Ajuster le niveau de zoom si nécessaire

            // Ajouter un curseur pour "vous êtes ici"
            Marker marker = createUserLocationMarker(userLocation);
            if (marker != null) {
                mapView.getOverlays().add(marker);
            }

            // Charger les restaurants
            loadRestaurants();
        } else {
            Log.e("MapViewFragment", "updateMapLocation called but currentLocation or mapView is null");
        }
    }

    private Marker createUserLocationMarker(GeoPoint userLocation) {
        if (mapView == null) {
            Log.e("MapViewFragment", "MapView is null in createUserLocationMarker");
            return null;
        }

        Marker userLocationMarker = new Marker(mapView);
        userLocationMarker.setPosition(userLocation);
        userLocationMarker.setIcon(getResources().getDrawable(R.drawable.user_location_icon)); // Remplacez par votre icône
        userLocationMarker.setTitle("Vous êtes ici");
        return userLocationMarker;
    }

    private void loadRestaurants() {
        if (currentLocation == null) {
            Log.d("MapViewFragment", "Current location is null.");
            return;
        }

        String overpassQuery = String.format(
                "[out:json];node[amenity=restaurant](around:1000,%f,%f);out;",
                currentLocation.getLatitude(), currentLocation.getLongitude()
        );

        // Ajouter un log pour afficher la requête Overpass
        Log.d("MapViewFragment", "Overpass Query: " + overpassQuery);

        Call<OverpassResponse> call = overpassApi.getRestaurants(overpassQuery);
        call.enqueue(new Callback<OverpassResponse>() {
            @Override
            public void onResponse(Call<OverpassResponse> call, Response<OverpassResponse> response) {
                if (response.isSuccessful()) {
                    OverpassResponse overpassResponse = response.body();
                    if (overpassResponse != null && overpassResponse.elements != null) {
                        Log.d("MapViewFragment", "Response received with " + overpassResponse.elements.size() + " elements.");
                        mapView.getOverlays().clear(); // Effacer les anciens marqueurs
                        Marker marker = createUserLocationMarker(new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()));
                        if (marker != null) {
                            mapView.getOverlays().add(marker);
                        }

                        for (OverpassResponse.Element element : overpassResponse.elements) {
                            Log.d("MapViewFragment", "Element: " + element.tags.name + " at " + element.lat + ", " + element.lon);
                            Marker restaurantMarker = new Marker(mapView);
                            restaurantMarker.setPosition(new GeoPoint(element.lat, element.lon));
                            restaurantMarker.setTitle(element.tags.name);
                            mapView.getOverlays().add(restaurantMarker);
                        }
                        mapView.invalidate();
                    } else {
                        Log.d("MapViewFragment", "Response body is null or empty.");
                    }
                } else {
                    Log.d("MapViewFragment", "Response not successful: " + response.code());
                    // Ajouter un log pour afficher le corps de la réponse en cas d'erreur
                    Log.d("MapViewFragment", "Error response: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<OverpassResponse> call, Throwable t) {
                Log.d("MapViewFragment", "Request failed: " + t.getMessage());
            }
        });
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Pas nécessaire pour cette implémentation
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        // Pas nécessaire pour cette implémentation
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        // Pas nécessaire pour cette implémentation
    }
}