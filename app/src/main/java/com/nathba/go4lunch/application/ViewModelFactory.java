package com.nathba.go4lunch.application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.nathba.go4lunch.repository.AuthRepository;
import com.nathba.go4lunch.repository.LunchRepository;
import com.nathba.go4lunch.repository.MainRepository;
import com.nathba.go4lunch.repository.MapRepository;
import com.nathba.go4lunch.repository.NotificationRepository;
import com.nathba.go4lunch.repository.RestaurantRepository;
import com.nathba.go4lunch.repository.WorkmateRepository;

/**
 * Factory class for creating instances of ViewModels in the application.
 * <p>
 * This factory ensures that the correct dependencies (repositories) are passed to each ViewModel.
 * It implements {@link ViewModelProvider.Factory} to create ViewModels based on their class type.
 * <p>
 * This approach centralizes the creation logic, making it easier to inject required dependencies
 * without manual instantiation in various parts of the code.
 */
public class ViewModelFactory implements ViewModelProvider.Factory {

    /** Repository for managing main application data and user state. */
    private final MainRepository mainRepository;

    /** Repository for handling user authentication logic. */
    private final AuthRepository authRepository;

    /** Repository for managing lunch-related data. */
    private final LunchRepository lunchRepository;

    /** Repository for managing restaurant-related data. */
    private final RestaurantRepository restaurantRepository;

    /** Repository for managing workmate-related data. */
    private final WorkmateRepository workmateRepository;

    /** Repository for handling map-related operations and data. */
    private final MapRepository mapRepository;

    /** Repository for managing notification-related data. */
    private final NotificationRepository notificationRepository;

    /**
     * Constructor for {@link ViewModelFactory}.
     * <p>
     * Initializes the factory with the required repositories that will be passed to the ViewModels.
     *
     * @param mainRepository        The repository for main application logic.
     * @param authRepository        The repository for user authentication.
     * @param lunchRepository       The repository for managing lunch data.
     * @param restaurantRepository  The repository for restaurant operations.
     * @param workmateRepository    The repository for workmate-related data.
     * @param mapRepository         The repository for map-related operations.
     * @param notificationRepository The repository for managing notification data.
     */
    public ViewModelFactory(MainRepository mainRepository,
                            AuthRepository authRepository,
                            LunchRepository lunchRepository,
                            RestaurantRepository restaurantRepository,
                            WorkmateRepository workmateRepository,
                            MapRepository mapRepository,
                            NotificationRepository notificationRepository) {
        this.mainRepository = mainRepository;
        this.authRepository = authRepository;
        this.lunchRepository = lunchRepository;
        this.restaurantRepository = restaurantRepository;
        this.workmateRepository = workmateRepository;
        this.mapRepository = mapRepository;
        this.notificationRepository = notificationRepository;
    }

    /**
     * Creates and returns an instance of the requested ViewModel class.
     * <p>
     * This method checks the class of the requested ViewModel and initializes it
     * with the appropriate repository or dependencies.
     *
     * @param modelClass The class of the ViewModel to be created.
     * @param <T>        The type of ViewModel to be created.
     * @return An instance of the requested ViewModel.
     * @throws IllegalArgumentException If the ViewModel class is not recognized.
     */
    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        // Return the corresponding ViewModel based on the provided class type
        if (modelClass.isAssignableFrom(MainViewModel.class)) {
            return (T) new MainViewModel(mainRepository, authRepository);
        } else if (modelClass.isAssignableFrom(AuthViewModel.class)) {
            return (T) new AuthViewModel(authRepository);
        } else if (modelClass.isAssignableFrom(LunchViewModel.class)) {
            return (T) new LunchViewModel(lunchRepository);
        } else if (modelClass.isAssignableFrom(RestaurantViewModel.class)) {
            return (T) new RestaurantViewModel(restaurantRepository, lunchRepository);
        } else if (modelClass.isAssignableFrom(WorkmateViewModel.class)) {
            return (T) new WorkmateViewModel(workmateRepository);
        } else if (modelClass.isAssignableFrom(MapViewModel.class)) {
            return (T) new MapViewModel(mapRepository, lunchRepository, restaurantRepository);
        } else if (modelClass.isAssignableFrom(NotificationViewModel.class)) {
            return (T) new NotificationViewModel(notificationRepository);
        }

        // Throw an exception if the ViewModel class is not recognized
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}