package com.nathba.go4lunch.application;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import androidx.lifecycle.LiveData;

import com.nathba.go4lunch.models.Restaurant;
import com.nathba.go4lunch.models.Workmate;

import java.util.ArrayList;
import java.util.List;

/**
 * MainViewModel is responsible for managing the data related to restaurants and workmates.
 * It provides methods to access and update the lists of restaurants and workmates, and exposes
 * these lists as LiveData.
 */
public class MainViewModel extends ViewModel {

    private MutableLiveData<List<Restaurant>> restaurants = new MutableLiveData<>();
    private MutableLiveData<List<Workmate>> workmates = new MutableLiveData<>();

    /**
     * Constructor initializes the ViewModel with some sample data for testing purposes.
     */
    public MainViewModel() {
        // Initialize with sample data for restaurants
        List<Restaurant> sampleRestaurants = new ArrayList<>();
        sampleRestaurants.add(new Restaurant("Le Zinc", "12 rue du Faubourg Poissonnière"));
        sampleRestaurants.add(new Restaurant("Tokyomaki", "24 rue des Petites Écoles"));
        sampleRestaurants.add(new Restaurant("Casa Nostra", "42 rue de Sicile"));
        restaurants.setValue(sampleRestaurants);

        // Initialize with sample data for workmates
        List<Workmate> sampleWorkmates = new ArrayList<>();
        sampleWorkmates.add(new Workmate("Scarlett"));
        sampleWorkmates.add(new Workmate("Hugh"));
        sampleWorkmates.add(new Workmate("Nana"));
        workmates.setValue(sampleWorkmates);
    }

    /**
     * Returns a LiveData object that contains the list of restaurants.
     *
     * @return LiveData<List<Restaurant>> representing the current list of restaurants.
     */
    public LiveData<List<Restaurant>> getRestaurants() {
        return restaurants;
    }

    /**
     * Returns a LiveData object that contains the list of workmates.
     *
     * @return LiveData<List<Workmate>> representing the current list of workmates.
     */
    public LiveData<List<Workmate>> getWorkmates() {
        return workmates;
    }

    /**
     * Updates the list of restaurants with the provided list and notifies observers.
     *
     * @param newRestaurants The new list of restaurants to be set.
     */
    public void setRestaurants(List<Restaurant> newRestaurants) {
        restaurants.setValue(newRestaurants);
    }

    /**
     * Updates the list of workmates with the provided list and notifies observers.
     *
     * @param newWorkmates The new list of workmates to be set.
     */
    public void setWorkmates(List<Workmate> newWorkmates) {
        workmates.setValue(newWorkmates);
    }
}