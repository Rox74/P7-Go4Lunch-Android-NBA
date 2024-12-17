package com.nathba.go4lunch.application;

import android.location.Location;

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

import java.util.List;

public class MapViewModel extends ViewModel {

    private final MapRepository mapRepository;
    private final LunchRepository lunchRepository;
    private final RestaurantRepository restaurantRepository;
    private final LiveData<List<Restaurant>> restaurants;
    private final LiveData<List<Lunch>> lunches;
    private final MutableLiveData<Restaurant> selectedRestaurant = new MutableLiveData<>();
    private final MutableLiveData<Location> userLocation = new MutableLiveData<>();
    private final MutableLiveData<List<Restaurant>> detailedRestaurants = new MutableLiveData<>();
    private final MutableLiveData<List<Lunch>> lunchesToday = new MutableLiveData<>();
    private static final String TAG = "MapViewModel";
    private boolean detailsFetched = false;

    public MapViewModel(MapRepository mapRepository, LunchRepository lunchRepository, RestaurantRepository restaurantRepository) {
        this.mapRepository = mapRepository;
        this.lunchRepository = lunchRepository;
        this.restaurantRepository = restaurantRepository;
        this.restaurants = mapRepository.getRestaurants();
        this.lunches = lunchRepository.getLunches();
    }

    // Method to fetch restaurant details from the repository
    public void fetchRestaurantDetails(String restaurantId, GeoPoint location, String restaurantName) {

        restaurantRepository.getRestaurantDetails(restaurantId, location, restaurantName, new RepositoryCallback<Restaurant>() {
            @Override
            public void onSuccess(Restaurant restaurant) {
                selectedRestaurant.setValue(restaurant);
            }

            @Override
            public void onError(Throwable t) {
                // Créer une instance de Restaurant avec des valeurs par défaut
                Restaurant basicRestaurant = new Restaurant(restaurantId, restaurantName, "", "", 0.0, location, "", "", "", false);
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

    // Set the user's location
    public void setUserLocation(Location location) {
        userLocation.setValue(location);
    }

    // Expose the list of lunches to observers
    public LiveData<List<Lunch>> getLunches() {
        return lunches;
    }

    // Load restaurants based on the user's location
    public void loadRestaurants(double latitude, double longitude) {
        mapRepository.loadRestaurants(latitude, longitude);
    }

    // Méthode pour charger les restaurants avec détails en bulk
    public void fetchRestaurantsDetailsIfNeeded(List<Restaurant> restaurants) {
        if (!detailsFetched) {
            restaurantRepository.fetchRestaurantsBulk(restaurants).observeForever(detailedRestaurants::setValue);
            detailsFetched = true;  // Marquer les détails comme chargés
        }
    }

    // Observer pour récupérer la liste des restaurants détaillés
    public LiveData<List<Restaurant>> getDetailedRestaurants() {
        return detailedRestaurants;
    }

    public LiveData<List<Lunch>> getLunchesToday() {
        return lunchesToday;
    }

    public void loadLunchesToday() {
        lunchRepository.getLunchesToday().observeForever(lunchesToday::setValue);
    }
}