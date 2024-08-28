package com.nathba.go4lunch.application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.nathba.go4lunch.models.Restaurant;
import com.nathba.go4lunch.repository.RestaurantRepository;

import java.util.List;

/**
 * ViewModel class for managing restaurant data.
 * It interacts with the RestaurantRepository to fetch and update restaurant information.
 */
public class RestaurantViewModel extends ViewModel {

    // Repository for managing restaurant data
    private final RestaurantRepository repository;

    // LiveData to hold the list of restaurants
    private final LiveData<List<Restaurant>> restaurants;

    /**
     * Constructor for RestaurantViewModel.
     * Initializes the RestaurantRepository instance and fetches the list of restaurants.
     */
    public RestaurantViewModel() {
        repository = new RestaurantRepository();
        restaurants = repository.getRestaurants();
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
     * Adds a new restaurant to the repository.
     *
     * @param restaurant The restaurant to be added.
     */
    public void addRestaurant(Restaurant restaurant) {
        repository.addRestaurant(restaurant);
    }
}