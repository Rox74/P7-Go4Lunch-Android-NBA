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
 * The {@link AppInjector} class is responsible for providing the application's dependencies.
 * <p>
 * It follows the Singleton design pattern to ensure that only one instance of the injector is created
 * throughout the application's lifecycle. This class initializes and holds references to all the repositories
 * required by the application and provides a centralized {@link ViewModelFactory} for creating ViewModels.
 * <p>
 * It simplifies dependency management and ensures proper initialization of components such as Firebase services.
 */
public class AppInjector {

    /** Singleton instance of {@link AppInjector}. */
    private static AppInjector instance;

    /** Repository for main application data and user state management. */
    private final MainRepository mainRepository;

    /** Repository for handling user authentication operations. */
    private final AuthRepository authRepository;

    /** Repository for managing lunch-related data. */
    private final LunchRepository lunchRepository;

    /** Repository for handling restaurant-related operations. */
    private final RestaurantRepository restaurantRepository;

    /** Repository for managing workmate-related data. */
    private final WorkmateRepository workmateRepository;

    /** Repository for managing map and geolocation data. */
    private final MapRepository mapRepository;

    /** Repository for handling notification-related data and operations. */
    private final NotificationRepository notificationRepository;

    /** Factory for creating ViewModels with the required repositories. */
    private final ViewModelFactory viewModelFactory;

    /**
     * Private constructor for {@link AppInjector}.
     * <p>
     * Initializes all repositories and the {@link ViewModelFactory} required by the application.
     * This method also initializes Firebase services, such as {@link FirebaseFirestore} and {@link FirebaseAuth}.
     */
    private AppInjector() {
        // Get instances of Firebase services
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        // Initialize repositories with required dependencies
        mainRepository = new MainRepository(firebaseAuth);        // Main repository for app data
        authRepository = new AuthRepository(firebaseAuth);        // Repository for authentication logic
        lunchRepository = new LunchRepository(firestore);         // Repository for lunch data
        restaurantRepository = new RestaurantRepository();        // Repository for restaurant data (API/caching)
        workmateRepository = new WorkmateRepository(firestore);   // Repository for workmate management
        mapRepository = new MapRepository();                      // Repository for geolocation and map data
        notificationRepository = new NotificationRepository(firestore);  // Repository for notifications

        // Initialize ViewModelFactory with all repositories
        viewModelFactory = new ViewModelFactory(mainRepository, authRepository, lunchRepository,
                restaurantRepository, workmateRepository, mapRepository, notificationRepository);
    }

    /**
     * Returns the singleton instance of {@link AppInjector}.
     * <p>
     * If the instance does not already exist, it is created. This ensures that only one instance
     * of the AppInjector is used throughout the application.
     *
     * @return The singleton instance of {@link AppInjector}.
     */
    public static synchronized AppInjector getInstance() {
        if (instance == null) {
            instance = new AppInjector();  // Create instance if not already created
        }
        return instance;  // Return the singleton instance
    }

    /**
     * Provides the {@link ViewModelFactory} used for creating ViewModels with the correct dependencies.
     * <p>
     * The ViewModelFactory ensures that ViewModels receive the required repositories
     * during instantiation.
     *
     * @return The {@link ViewModelFactory} instance containing all required dependencies.
     */
    public ViewModelFactory getViewModelFactory() {
        return viewModelFactory;  // Return the ViewModelFactory
    }
}