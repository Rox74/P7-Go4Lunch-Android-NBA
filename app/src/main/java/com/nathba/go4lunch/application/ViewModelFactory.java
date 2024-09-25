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

import java.security.Provider;
import java.util.Map;

public class ViewModelFactory implements ViewModelProvider.Factory {

    private final MainRepository mainRepository;
    private final AuthRepository authRepository;
    private final LunchRepository lunchRepository;
    private final RestaurantRepository restaurantRepository;
    private final WorkmateRepository workmateRepository;
    private final MapRepository mapRepository;

    // Constructor that accepts all necessary repositories
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

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
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
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}