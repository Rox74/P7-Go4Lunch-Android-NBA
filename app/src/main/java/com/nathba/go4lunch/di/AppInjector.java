package com.nathba.go4lunch.di;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nathba.go4lunch.application.ViewModelFactory;
import com.nathba.go4lunch.repository.AuthRepository;
import com.nathba.go4lunch.repository.LunchRepository;
import com.nathba.go4lunch.repository.MainRepository;
import com.nathba.go4lunch.repository.MapRepository;
import com.nathba.go4lunch.repository.NotificationRepository;
import com.nathba.go4lunch.repository.RestaurantRepository;
import com.nathba.go4lunch.repository.WorkmateRepository;

/**
 * The AppInjector class is responsible for providing the application's dependencies.
 * It follows the Singleton pattern to ensure that only one instance of the injector is created.
 * This class initializes all necessary repositories and the ViewModelFactory.
 */
public class AppInjector {

    // Singleton instance of AppInjector
    private static AppInjector instance;

    // Repositories for various data sources and services
    private final MainRepository mainRepository;
    private final AuthRepository authRepository;
    private final LunchRepository lunchRepository;
    private final RestaurantRepository restaurantRepository;
    private final WorkmateRepository workmateRepository;
    private final MapRepository mapRepository;
    private final NotificationRepository notificationRepository;

    // Factory for creating ViewModels
    private ViewModelFactory viewModelFactory;

    /**
     * Private constructor for the AppInjector.
     * Initializes Firebase and the repositories required by the application.
     * It also initializes the ViewModelFactory with all repositories.
     */
    private AppInjector() {
        // Get instances of Firebase services
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        // Initialize Repositories
        mainRepository = new MainRepository(firebaseAuth);        // Repository for main app data
        authRepository = new AuthRepository(firebaseAuth);        // Repository for authentication data
        lunchRepository = new LunchRepository(firestore);         // Repository for managing lunch data
        restaurantRepository = new RestaurantRepository();        // Repository for restaurant data (API, cache)
        workmateRepository = new WorkmateRepository(firestore);   // Repository for managing workmates data
        mapRepository = new MapRepository();                      // Repository for managing map and geolocation
        notificationRepository = new NotificationRepository(firestore);  // Repository for managing notifications

        // Initialize ViewModelFactory with all repositories
        viewModelFactory = new ViewModelFactory(mainRepository, authRepository, lunchRepository,
                restaurantRepository, workmateRepository, mapRepository, notificationRepository);
    }

    /**
     * Returns the singleton instance of AppInjector.
     * If the instance does not exist, it is created and returned.
     *
     * @return The singleton instance of AppInjector.
     */
    public static synchronized AppInjector getInstance() {
        // If the instance is null, create it
        if (instance == null) {
            instance = new AppInjector();
        }
        return instance;  // Return the singleton instance
    }

    /**
     * Provides the ViewModelFactory that is used to create ViewModels for the application.
     *
     * @return The ViewModelFactory containing all required dependencies.
     */
    public ViewModelFactory getViewModelFactory() {
        return viewModelFactory;  // Return the ViewModelFactory instance
    }
}