package com.nathba.go4lunch.application;

import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nathba.go4lunch.models.OverpassResponse;
import com.nathba.go4lunch.api.OverpassApi;
import com.nathba.go4lunch.models.Restaurant;

import org.osmdroid.util.GeoPoint;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapViewModel extends ViewModel {
    private static final String TAG = "MapViewModel";
    private static final int RESTAURANT_SEARCH_RADIUS = 1000; // en mètres

    private final MutableLiveData<List<Restaurant>> restaurants = new MutableLiveData<>();
    private final MutableLiveData<Location> userLocation = new MutableLiveData<>();
    private final OverpassApi overpassApi;

    public MapViewModel() {
        overpassApi = new OverpassApi();
    }

    public LiveData<List<Restaurant>> getRestaurants() {
        return restaurants;
    }

    public LiveData<Location> getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(Location location) {
        userLocation.setValue(location);
        loadRestaurants();
    }

    private void loadRestaurants() {
        Location location = userLocation.getValue();
        if (location == null) {
            Log.d(TAG, "User location is null.");
            return;
        }

        String overpassQuery = String.format(Locale.US,
                "[out:json];node[\"amenity\"=\"restaurant\"](around:%d,%f,%f);out;",
                RESTAURANT_SEARCH_RADIUS, location.getLatitude(), location.getLongitude()
        );

        overpassApi.getRestaurants(overpassQuery).enqueue(new Callback<OverpassResponse>() {
            @Override
            public void onResponse(@NonNull Call<OverpassResponse> call, @NonNull Response<OverpassResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Restaurant> restaurantList = convertToRestaurants(response.body().elements);
                    restaurants.postValue(restaurantList);
                } else {
                    Log.e(TAG, "Error response: " + (response.errorBody() != null ? response.errorBody().toString() : "Unknown error"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<OverpassResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Request failed: " + t.getMessage(), t);
            }
        });
    }

    private List<Restaurant> convertToRestaurants(List<OverpassResponse.Element> elements) {
        List<Restaurant> restaurantList = new ArrayList<>();
        for (OverpassResponse.Element element : elements) {
            Restaurant restaurant = new Restaurant(
                    element.tags.name != null ? element.tags.name : "Restaurant sans nom",
                    new GeoPoint(element.lat, element.lon)
            );
            restaurantList.add(restaurant);
        }
        return restaurantList;
    }
}