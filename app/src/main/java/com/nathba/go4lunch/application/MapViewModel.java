package com.nathba.go4lunch.application;

import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nathba.go4lunch.models.Lunch;
import com.nathba.go4lunch.models.OverpassResponse;
import com.nathba.go4lunch.api.OverpassApi;
import com.nathba.go4lunch.models.Restaurant;
import com.nathba.go4lunch.repository.RestaurantRepository;

import org.osmdroid.util.GeoPoint;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * ViewModel class for managing map-related data, including restaurants and lunches.
 * It handles the user's location, queries for nearby restaurants, and manages lunch data.
 */
public class MapViewModel extends ViewModel {

    // Log tag for debugging
    private static final String TAG = "MapViewModel";

    // Radius for restaurant search in meters
    private static final int RESTAURANT_SEARCH_RADIUS = 1000;

    // LiveData to hold the list of restaurants
    private final MutableLiveData<List<Restaurant>> restaurants = new MutableLiveData<>();

    // LiveData to hold the list of lunches
    private final MutableLiveData<List<Lunch>> lunches = new MutableLiveData<>();

    // LiveData to hold the user's current location
    private final MutableLiveData<Location> userLocation = new MutableLiveData<>();

    // API service for querying Overpass API
    private final OverpassApi overpassApi;

    // Repository for managing restaurant data
    private final RestaurantRepository restaurantRepository;

    /**
     * Constructor for MapViewModel.
     * Initializes the OverpassApi and RestaurantRepository instances.
     */
    public MapViewModel() {
        overpassApi = new OverpassApi();
        restaurantRepository = new RestaurantRepository();
    }

    /**
     * Returns LiveData object containing the list of restaurants.
     * Observers can subscribe to this LiveData to receive updates when the restaurant list changes.
     *
     * @return LiveData<List<Restaurant>> - LiveData containing the list of restaurants.
     */
    public LiveData<List<Restaurant>> getRestaurants() {
        return restaurants;
    }

    /**
     * Returns LiveData object containing the list of lunches.
     * Observers can subscribe to this LiveData to receive updates when the lunch list changes.
     *
     * @return LiveData<List<Lunch>> - LiveData containing the list of lunches.
     */
    public LiveData<List<Lunch>> getLunches() {
        return lunches;
    }

    /**
     * Returns LiveData object containing the user's current location.
     * Observers can subscribe to this LiveData to receive updates when the user's location changes.
     *
     * @return LiveData<Location> - LiveData containing the user's current location.
     */
    public LiveData<Location> getUserLocation() {
        return userLocation;
    }

    /**
     * Sets the user's location and triggers loading of restaurants and lunches.
     *
     * @param location The user's current location.
     */
    public void setUserLocation(Location location) {
        userLocation.setValue(location);
        loadRestaurants(); // Load restaurants based on the new location
        loadLunches(); // Load lunches after updating the user's location
    }

    /**
     * Queries the Overpass API for nearby restaurants based on the user's location.
     * Updates the restaurant list in LiveData and stores the results in Firestore.
     */
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
                    storeRestaurantsInFirestore(restaurantList);
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

    /**
     * Stores a list of restaurants in Firestore.
     *
     * @param restaurants The list of restaurants to be stored.
     */
    private void storeRestaurantsInFirestore(List<Restaurant> restaurants) {
        for (Restaurant restaurant : restaurants) {
            restaurantRepository.addRestaurant(restaurant);
        }
    }

    /**
     * Loads lunch data from a data source.
     * This method currently contains a placeholder implementation.
     *
     * TODO: Implement the logic to fetch lunches from a database or API.
     */
    private void loadLunches() {
        // TODO: Implementation for fetching lunches from a data source
        List<Lunch> lunchList = new ArrayList<>();

        // Add Lunch items to the list
        lunches.postValue(lunchList);
    }

    /**
     * Converts Overpass API response elements to a list of Restaurant objects.
     *
     * @param elements The list of Overpass API response elements.
     * @return List<Restaurant> - The list of Restaurant objects.
     */
    private List<Restaurant> convertToRestaurants(List<OverpassResponse.Element> elements) {
        List<Restaurant> restaurantList = new ArrayList<>();
        for (OverpassResponse.Element element : elements) {
            // Extract necessary information
            String name = element.tags != null && element.tags.name != null ? element.tags.name : "Restaurant sans nom";
            GeoPoint location = new GeoPoint(element.lat, element.lon);
            String address = element.tags != null && element.tags.address != null ? element.tags.address : "Adresse inconnue";
            String id = element.id != null ? element.id : "ID inconnu";

            // Additional information for photoUrl and rating
            String photoUrl = element.tags != null && element.tags.photoUrl != null ? element.tags.photoUrl : "URL de photo non disponible";
            double rating = element.tags != null && element.tags.rating != null ? Double.parseDouble(element.tags.rating) : 0.0;

            // Create a Restaurant object with all the required parameters
            Restaurant restaurant = new Restaurant(id, name, address, photoUrl, rating, location);
            restaurantList.add(restaurant);
        }
        return restaurantList;
    }
}