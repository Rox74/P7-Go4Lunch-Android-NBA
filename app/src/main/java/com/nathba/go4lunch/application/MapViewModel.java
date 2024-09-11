package com.nathba.go4lunch.application;

import android.location.Location;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nathba.go4lunch.models.Lunch;
import com.nathba.go4lunch.api.OverpassApi;
import com.nathba.go4lunch.models.Restaurant;
import com.nathba.go4lunch.repository.RepositoryCallback;
import com.nathba.go4lunch.repository.RestaurantRepository;

import java.util.ArrayList;
import java.util.List;

public class MapViewModel extends ViewModel {

    private final RestaurantRepository restaurantRepository;
    private final MutableLiveData<List<Restaurant>> restaurants = new MutableLiveData<>();
    private final MutableLiveData<Location> userLocation = new MutableLiveData<>();
    private final MutableLiveData<List<Lunch>> lunches = new MutableLiveData<>(new ArrayList<>());

    public MapViewModel() {
        restaurantRepository = new RestaurantRepository();
        loadRestaurants();
    }

    // Charger les restaurants (une seule fois au démarrage)
    private void loadRestaurants() {
        restaurantRepository.fetchRestaurantsFromApi(new RepositoryCallback<List<Restaurant>>() {
            @Override
            public void onSuccess(List<Restaurant> data) {
                restaurants.postValue(data);  // Mettre à jour les restaurants
            }

            @Override
            public void onError(Throwable t) {
                Log.e("MapViewModel", "Erreur lors de la récupération des restaurants", t);
            }
        });
    }

    public LiveData<List<Restaurant>> getRestaurants() {
        return restaurants;
    }

    public LiveData<Location> getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(Location location) {
        userLocation.setValue(location);
    }

    public LiveData<List<Lunch>> getLunches() {
        return lunches;
    }
}