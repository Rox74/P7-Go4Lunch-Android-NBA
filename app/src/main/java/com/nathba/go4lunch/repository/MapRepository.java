package com.nathba.go4lunch.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.nathba.go4lunch.models.Restaurant;

import java.util.List;

/**
 * Repository class for managing map-related data.
 * <p>
 * This class interacts with {@link RestaurantRepository} to fetch and provide a list of restaurants
 * based on the user's geographical location. The data is exposed as {@link LiveData} for observation
 * in the UI layer.
 */
public class MapRepository {

    /** Instance of {@link RestaurantRepository} to fetch restaurant data. */
    private final RestaurantRepository restaurantRepository = new RestaurantRepository();

    /** LiveData object containing the list of restaurants fetched for the current location. */
    private final MutableLiveData<List<Restaurant>> restaurants = new MutableLiveData<>();

    /**
     * Default constructor for {@link MapRepository}.
     * <p>
     * No initial data is loaded in the constructor as coordinates are required to fetch restaurants.
     */
    public MapRepository() {
        // Void constructor
    }

    /**
     * Loads a list of restaurants based on the specified geographical coordinates.
     * <p>
     * This method calls {@link RestaurantRepository#getRestaurants(double, double)} to fetch
     * restaurants for the given latitude and longitude. The data is observed and updated in
     * the {@link #restaurants} LiveData.
     *
     * @param latitude  The latitude of the user's current location.
     * @param longitude The longitude of the user's current location.
     */
    public void loadRestaurants(double latitude, double longitude) {
        restaurantRepository.getRestaurants(latitude, longitude).observeForever(data -> {
            if (data != null) {
                restaurants.postValue(data);
            } else {
                Log.e("MapRepository", "Error fetching restaurants");
            }
        });
    }

    /**
     * Returns a {@link LiveData} object containing the list of restaurants.
     * <p>
     * Observers can subscribe to this LiveData to receive updates when the restaurant data changes.
     *
     * @return A {@link LiveData} object containing a list of {@link Restaurant} objects.
     */
    public LiveData<List<Restaurant>> getRestaurants() {
        return restaurants;
    }
}