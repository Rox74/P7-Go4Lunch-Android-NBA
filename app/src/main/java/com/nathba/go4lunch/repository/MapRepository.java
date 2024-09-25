package com.nathba.go4lunch.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.nathba.go4lunch.models.Restaurant;

import java.util.List;

public class MapRepository {

    private final RestaurantRepository restaurantRepository = new RestaurantRepository();
    private final MutableLiveData<List<Restaurant>> restaurants = new MutableLiveData<>();

    public MapRepository() {
        loadRestaurants();
    }

    private void loadRestaurants() {
        restaurantRepository.fetchRestaurantsFromApi(new RepositoryCallback<List<Restaurant>>() {
            @Override
            public void onSuccess(List<Restaurant> data) {
                restaurants.postValue(data);
            }

            @Override
            public void onError(Throwable t) {
                Log.e("MapRepository", "Error fetching restaurants", t);
            }
        });
    }

    public LiveData<List<Restaurant>> getRestaurants() {
        return restaurants;
    }
}