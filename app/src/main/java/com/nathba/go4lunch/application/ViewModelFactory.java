package com.nathba.go4lunch.application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.nathba.go4lunch.repository.AuthRepository;
import com.nathba.go4lunch.repository.LunchRepository;
import com.nathba.go4lunch.repository.MainRepository;
import com.nathba.go4lunch.repository.MapRepository;
import com.nathba.go4lunch.repository.RestaurantRepository;
import com.nathba.go4lunch.repository.WorkmateRepository;

/**
 * ViewModelFactory is responsible for creating instances of ViewModels in the application.
 * It holds references to the necessary repositories and uses them to instantiate the ViewModels.
 * This factory is used to ensure the correct dependencies are passed to each ViewModel.
 */
public class ViewModelFactory implements ViewModelProvider.Factory {

    // Repositories required by various ViewModels
    private final MainRepository mainRepository;
    private final AuthRepository authRepository;
    private final LunchRepository lunchRepository;
    private final RestaurantRepository restaurantRepository;
    private final WorkmateRepository workmateRepository;
    private final MapRepository mapRepository;

    /**
     * Constructor that accepts all necessary repositories for ViewModel creation.
     *
     * @param mainRepository        Repository for main app data.
     * @param authRepository        Repository for authentication.
     * @param lunchRepository       Repository for managing lunch data.
     * @param restaurantRepository  Repository for restaurant data.
     * @param workmateRepository    Repository for workmate data.
     * @param mapRepository         Repository for map and location data.
     */
    public ViewModelFactory(MainRepository mainRepository,
                            AuthRepository authRepository,
                            LunchRepository lunchRepository,
                            RestaurantRepository restaurantRepository,
                            WorkmateRepository workmateRepository,
                            MapRepository mapRepository) {
        this.mainRepository = mainRepository;
        this.authRepository = authRepository;
        this.lunchRepository = lunchRepository;
        this.restaurantRepository = restaurantRepository;
        this.workmateRepository = workmateRepository;
        this.mapRepository = mapRepository;
    }

    /**
     * Creates and returns an instance of the requested ViewModel.
     * It checks the class of the requested ViewModel and initializes it with the appropriate repository.
     *
     * @param modelClass The class of the ViewModel to be created.
     * @param <T>        The type of ViewModel to be created.
     * @return An instance of the requested ViewModel.
     * @throws IllegalArgumentException if the ViewModel class is not recognized.
     */
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        // Return the corresponding ViewModel based on the provided class type
        if (modelClass.isAssignableFrom(MainViewModel.class)) {
            return (T) new MainViewModel(mainRepository);
        } else if (modelClass.isAssignableFrom(AuthViewModel.class)) {
            return (T) new AuthViewModel(authRepository);
        } else if (modelClass.isAssignableFrom(LunchViewModel.class)) {
            return (T) new LunchViewModel(lunchRepository);
        } else if (modelClass.isAssignableFrom(RestaurantViewModel.class)) {
            return (T) new RestaurantViewModel(restaurantRepository);
        } else if (modelClass.isAssignableFrom(WorkmateViewModel.class)) {
            return (T) new WorkmateViewModel(workmateRepository);
        } else if (modelClass.isAssignableFrom(MapViewModel.class)) {
            return (T) new MapViewModel(mapRepository, lunchRepository);
        }

        // Throw an exception if the ViewModel class is not recognized
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}