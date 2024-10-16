package com.nathba.go4lunch.application;

import android.location.Location;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nathba.go4lunch.models.Lunch;
import com.nathba.go4lunch.models.Restaurant;
import com.nathba.go4lunch.repository.LunchRepository;
import com.nathba.go4lunch.repository.MapRepository;
import com.nathba.go4lunch.repository.RepositoryCallback;
import com.nathba.go4lunch.repository.RestaurantRepository;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class MapViewModel extends ViewModel {

    private final MapRepository mapRepository;
    private final LunchRepository lunchRepository;
    private final RestaurantRepository restaurantRepository;
    private final MutableLiveData<Restaurant> selectedRestaurant = new MutableLiveData<>();
    private final LiveData<List<Restaurant>> restaurants;
    private final MutableLiveData<Location> userLocation = new MutableLiveData<>();
    private final LiveData<List<Lunch>> lunches;
    private static final String TAG = "MapViewModel";

    public MapViewModel(MapRepository mapRepository, LunchRepository lunchRepository, RestaurantRepository restaurantRepository) {
        this.mapRepository = mapRepository;
        this.lunchRepository = lunchRepository;
        this.restaurantRepository = restaurantRepository;
        this.restaurants = mapRepository.getRestaurants();
        this.lunches = lunchRepository.getLunches();
    }

    // Method to fetch restaurant details from the repository
    public void fetchRestaurantDetails(String restaurantId, GeoPoint location, String restaurantName) {
        Log.d(TAG, "Fetching details for restaurant: " + restaurantName + " from repository");

        restaurantRepository.getRestaurantDetails(restaurantId, location, restaurantName, new RepositoryCallback<Restaurant>() {
            @Override
            public void onSuccess(Restaurant restaurant) {
                Log.d(TAG, "Successfully retrieved restaurant details: " + restaurant.getName());
                selectedRestaurant.setValue(restaurant);
            }

            @Override
            public void onError(Throwable t) {
                Log.e(TAG, "Failed to retrieve restaurant details: " + t.getMessage());
                // Créer une instance de Restaurant avec des valeurs par défaut
                Restaurant basicRestaurant = new Restaurant(restaurantId, restaurantName, "", "", 0.0, location, "", "", "", new ArrayList<>());
                selectedRestaurant.setValue(basicRestaurant); // Utiliser des détails de base en cas d'erreur
            }
        });
    }

    // Expose the selected restaurant data to observers
    public LiveData<Restaurant> getSelectedRestaurant() {
        return selectedRestaurant;
    }

    // Expose the list of restaurants to observers
    public LiveData<List<Restaurant>> getRestaurants() {
        return restaurants;
    }

    public LiveData<List<Restaurant>> getRestaurants(double latitude, double longitude) {
        return restaurantRepository.getRestaurants(latitude, longitude);
    }

    // Expose the user's location to observers
    public LiveData<Location> getUserLocation() {
        return userLocation;
    }

    // Set the user's location
    public void setUserLocation(Location location) {
        Log.d(TAG, "Setting user location: " + location);
        userLocation.setValue(location);
    }

    // Expose the list of lunches to observers
    public LiveData<List<Lunch>> getLunches() {
        return lunches;
    }

    // Load restaurants based on the user's location
    public void loadRestaurants(double latitude, double longitude) {
        Log.d(TAG, "Loading restaurants at latitude: " + latitude + ", longitude: " + longitude);
        mapRepository.loadRestaurants(latitude, longitude);
    }
}