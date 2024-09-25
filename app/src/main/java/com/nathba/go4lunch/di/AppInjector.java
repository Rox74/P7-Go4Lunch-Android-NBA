package com.nathba.go4lunch.di;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nathba.go4lunch.api.OverpassApi;
import com.nathba.go4lunch.api.YelpApi;
import com.nathba.go4lunch.application.ViewModelFactory;
import com.nathba.go4lunch.repository.AuthRepository;
import com.nathba.go4lunch.repository.LunchRepository;
import com.nathba.go4lunch.repository.MainRepository;
import com.nathba.go4lunch.repository.MapRepository;
import com.nathba.go4lunch.repository.RestaurantRepository;
import com.nathba.go4lunch.repository.WorkmateRepository;

public class AppInjector {

    private static AppInjector instance;

    private final MainRepository mainRepository;
    private final AuthRepository authRepository;
    private final LunchRepository lunchRepository;
    private final RestaurantRepository restaurantRepository;
    private final WorkmateRepository workmateRepository;
    private final MapRepository mapRepository;

    private ViewModelFactory viewModelFactory;

    // Private constructor to create the instance with all dependencies
    private AppInjector() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        // Initialize Repositories
        mainRepository = new MainRepository(firebaseAuth);
        authRepository = new AuthRepository(firebaseAuth);
        lunchRepository = new LunchRepository(firestore);
        restaurantRepository = new RestaurantRepository();
        workmateRepository = new WorkmateRepository(firestore);
        mapRepository = new MapRepository();

        // Initialize ViewModelFactory with all repositories
        viewModelFactory = new ViewModelFactory(mainRepository, authRepository, lunchRepository,
                restaurantRepository, workmateRepository, mapRepository);
    }

    // Singleton pattern to ensure only one instance of AppInjector
    public static synchronized AppInjector getInstance() {
        if (instance == null) {
            instance = new AppInjector();
        }
        return instance;
    }

    // Provide the ViewModelFactory
    public ViewModelFactory getViewModelFactory() {
        return viewModelFactory;
    }
}