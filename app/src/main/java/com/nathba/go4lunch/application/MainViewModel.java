package com.nathba.go4lunch.application;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import androidx.lifecycle.LiveData;

import com.nathba.go4lunch.models.Restaurant;
import com.nathba.go4lunch.models.Workmate;

import java.util.ArrayList;
import java.util.List;

public class MainViewModel extends ViewModel {

    private MutableLiveData<List<Restaurant>> restaurants = new MutableLiveData<>();
    private MutableLiveData<List<Workmate>> workmates = new MutableLiveData<>();

    public MainViewModel() {
        // Initializing with some sample data for testing
        List<Restaurant> sampleRestaurants = new ArrayList<>();
        sampleRestaurants.add(new Restaurant("Le Zinc", "12 rue du Faubourg Poissonnière"));
        sampleRestaurants.add(new Restaurant("Tokyomaki", "24 rue des Petites Écoles"));
        sampleRestaurants.add(new Restaurant("Casa Nostra", "42 rue de Sicile"));
        restaurants.setValue(sampleRestaurants);

        List<Workmate> sampleWorkmates = new ArrayList<>();
        sampleWorkmates.add(new Workmate("Scarlett"));
        sampleWorkmates.add(new Workmate("Hugh"));
        sampleWorkmates.add(new Workmate("Nana"));
        workmates.setValue(sampleWorkmates);
    }

    public LiveData<List<Restaurant>> getRestaurants() {
        return restaurants;
    }

    public LiveData<List<Workmate>> getWorkmates() {
        return workmates;
    }

    // TODO : Add methods to update restaurants and workmates as needed
}