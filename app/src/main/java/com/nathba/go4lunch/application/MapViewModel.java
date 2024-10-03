package com.nathba.go4lunch.application;

import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nathba.go4lunch.models.Lunch;
import com.nathba.go4lunch.models.Restaurant;
import com.nathba.go4lunch.repository.LunchRepository;
import com.nathba.go4lunch.repository.MapRepository;

import java.util.List;

public class MapViewModel extends ViewModel {

    private final MapRepository mapRepository;
    private final LunchRepository lunchRepository;
    private final LiveData<List<Restaurant>> restaurants;
    private final MutableLiveData<Location> userLocation = new MutableLiveData<>();
    private final LiveData<List<Lunch>> lunches;

    public MapViewModel(MapRepository mapRepository, LunchRepository lunchRepository) {
        this.mapRepository = mapRepository;
        this.lunchRepository = lunchRepository;
        this.restaurants = mapRepository.getRestaurants();
        this.lunches = lunchRepository.getLunches();
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
        return lunches;  // Exposer la liste des lunchs
    }

    // Nouvelle méthode pour charger les restaurants en fonction de la localisation
    public void loadRestaurants(double latitude, double longitude) {
        mapRepository.loadRestaurants(latitude, longitude);
    }

}