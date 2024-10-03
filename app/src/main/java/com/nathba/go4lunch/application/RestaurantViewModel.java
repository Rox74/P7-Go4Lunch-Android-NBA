package com.nathba.go4lunch.application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.nathba.go4lunch.models.Lunch;
import com.nathba.go4lunch.models.Restaurant;
import com.nathba.go4lunch.repository.RestaurantRepository;

import java.util.List;

public class RestaurantViewModel extends ViewModel {

    private final RestaurantRepository restaurantRepository;
    private LiveData<List<Restaurant>> restaurants;

    public RestaurantViewModel(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
        this.restaurants = restaurantRepository.getRestaurants();
    }

    public LiveData<List<Lunch>> getLunchesForRestaurantToday(String restaurantId) {
        return restaurantRepository.getLunchesForRestaurantToday(restaurantId);
    }

    public LiveData<List<Restaurant>> getRestaurants() {
        return restaurants;
    }

    public void addLunch(Lunch lunch, Restaurant restaurant) {
        restaurantRepository.addLunch(lunch, restaurant);
    }
}