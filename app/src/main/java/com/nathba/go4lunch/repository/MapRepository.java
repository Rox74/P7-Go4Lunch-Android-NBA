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
        // Retirer l'appel à loadRestaurants ici, car il faut maintenant des coordonnées
    }

    // Nouvelle méthode pour charger les restaurants en fonction de la localisation
    public void loadRestaurants(double latitude, double longitude) {
        // Utiliser directement getRestaurants avec LiveData
        restaurantRepository.getRestaurants(latitude, longitude).observeForever(data -> {
            if (data != null) {
                restaurants.postValue(data);
            } else {
                Log.e("MapRepository", "Error fetching restaurants");
            }
        });
    }

    public LiveData<List<Restaurant>> getRestaurants() {
        return restaurants;
    }
}