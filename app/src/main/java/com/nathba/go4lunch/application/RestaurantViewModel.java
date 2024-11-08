package com.nathba.go4lunch.application;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.nathba.go4lunch.models.Lunch;
import com.nathba.go4lunch.models.Restaurant;
import com.nathba.go4lunch.repository.LunchRepository;
import com.nathba.go4lunch.repository.RestaurantRepository;

import java.util.List;

public class RestaurantViewModel extends ViewModel {

    private final RestaurantRepository restaurantRepository;
    private final LunchRepository lunchRepository;
    private LiveData<List<Restaurant>> restaurants;
    private static final String TAG = "RestaurantViewModel";

    public RestaurantViewModel(RestaurantRepository restaurantRepository, LunchRepository lunchRepository) {
        this.restaurantRepository = restaurantRepository;
        this.lunchRepository = lunchRepository;
    }

    // Utiliser LunchRepository pour récupérer les lunchs d'un restaurant
    public LiveData<List<Lunch>> getLunchesForRestaurantToday(String restaurantId) {
        return lunchRepository.getLunchesForRestaurantToday(restaurantId);
    }

    // Méthode mise à jour pour récupérer les restaurants en fonction des coordonnées
    public LiveData<List<Restaurant>> getRestaurants(double latitude, double longitude) {
        return restaurantRepository.getRestaurants(latitude, longitude);
    }

    // Nouvelle méthode pour récupérer les détails des restaurants en bulk depuis Yelp
    public LiveData<List<Restaurant>> fetchRestaurantDetailsBulk(List<Restaurant> restaurants) {
        Log.d(TAG, "Calling fetchRestaurantDetailsBulk with " + restaurants.size() + " restaurants");
        return restaurantRepository.fetchRestaurantsBulk(restaurants);
    }

    // Utiliser LunchRepository pour ajouter un lunch
    public void addLunch(Lunch lunch) {
        lunchRepository.addLunch(lunch);
    }

    public void addRestaurant(Restaurant restaurant) {
        restaurantRepository.addRestaurantToFirestore(restaurant);
    }

    // Récupérer le nombre de lunchs pour un restaurant spécifique
    public LiveData<Integer> getLunchCountForRestaurant(String restaurantId) {
        return lunchRepository.getLunchCountForRestaurant(restaurantId);
    }

    // Récupérer la liste des restaurants déjà détaillés
    public LiveData<List<Restaurant>> getDetailedRestaurants() {
        // Retourne les restaurants détaillés déjà chargés dans le repository
        return restaurantRepository.getCachedRestaurants();
    }
}