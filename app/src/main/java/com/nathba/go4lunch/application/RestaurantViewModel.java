package com.nathba.go4lunch.application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.nathba.go4lunch.models.Lunch;
import com.nathba.go4lunch.models.Restaurant;
import com.nathba.go4lunch.repository.LunchRepository;
import com.nathba.go4lunch.repository.RestaurantRepository;

import java.util.List;

/**
 * ViewModel class for managing restaurant and lunch-related data.
 * <p>
 * This class serves as an intermediary between the UI and the repositories:
 * {@link RestaurantRepository} for restaurant-related operations and
 * {@link LunchRepository} for managing lunch data. It exposes data as {@link LiveData}
 * to ensure the UI layer can observe changes reactively.
 */
public class RestaurantViewModel extends ViewModel {

    /** Repository for managing restaurant data and operations. */
    private final RestaurantRepository restaurantRepository;

    /** Repository for managing lunch-related data and operations. */
    private final LunchRepository lunchRepository;

    /** Tag used for logging purposes. */
    private static final String TAG = "RestaurantViewModel";

    /** LiveData to hold the list of restaurants (optional lazy initialization). */
    private LiveData<List<Restaurant>> restaurants;

    /**
     * Constructor for {@link RestaurantViewModel}.
     * <p>
     * Initializes the ViewModel with instances of {@link RestaurantRepository}
     * and {@link LunchRepository}.
     *
     * @param restaurantRepository The repository responsible for managing restaurant data.
     * @param lunchRepository      The repository responsible for managing lunch data.
     */
    public RestaurantViewModel(RestaurantRepository restaurantRepository, LunchRepository lunchRepository) {
        this.restaurantRepository = restaurantRepository;
        this.lunchRepository = lunchRepository;
    }

    /**
     * Retrieves the list of lunches for a specific restaurant for the current day.
     * <p>
     * This method queries the {@link LunchRepository} to fetch the list of lunches
     * associated with the given restaurant ID for today's date.
     *
     * @param restaurantId The unique ID of the restaurant.
     * @return LiveData containing a list of {@link Lunch} objects for the specified restaurant.
     */
    public LiveData<List<Lunch>> getLunchesForRestaurantToday(String restaurantId) {
        return lunchRepository.getLunchesForRestaurantToday(restaurantId);
    }

    /**
     * Retrieves the list of restaurants based on the user's geographical coordinates.
     * <p>
     * This method queries the {@link RestaurantRepository} to fetch nearby restaurants
     * based on the provided latitude and longitude.
     *
     * @param latitude  The latitude of the user's location.
     * @param longitude The longitude of the user's location.
     * @return LiveData containing a list of {@link Restaurant} objects nearby.
     */
    public LiveData<List<Restaurant>> getRestaurants(double latitude, double longitude) {
        return restaurantRepository.getRestaurants(latitude, longitude);
    }

    /**
     * Fetches detailed information about a list of restaurants in bulk.
     * <p>
     * This method calls the {@link RestaurantRepository} to retrieve detailed data
     * (e.g., from Yelp API) for multiple restaurants.
     *
     * @param restaurants A list of {@link Restaurant} objects for which details are to be fetched.
     * @return LiveData containing a list of detailed {@link Restaurant} objects.
     */
    public LiveData<List<Restaurant>> fetchRestaurantDetailsBulk(List<Restaurant> restaurants) {
        return restaurantRepository.fetchRestaurantsBulk(restaurants);
    }

    /**
     * Adds a new lunch entry to the database.
     * <p>
     * This method delegates to {@link LunchRepository} to add the provided lunch data.
     *
     * @param lunch The {@link Lunch} object representing the lunch to be added.
     */
    public void addLunch(Lunch lunch) {
        lunchRepository.addLunch(lunch);
    }

    /**
     * Adds a new restaurant to Firestore.
     * <p>
     * This method delegates to {@link RestaurantRepository} to store the restaurant data in Firestore.
     *
     * @param restaurant The {@link Restaurant} object representing the restaurant to be added.
     */
    public void addRestaurant(Restaurant restaurant) {
        restaurantRepository.addRestaurantToFirestore(restaurant);
    }

    /**
     * Retrieves the list of already detailed restaurants cached in the repository.
     * <p>
     * This ensures that previously fetched restaurant details can be reused without additional API calls.
     *
     * @return LiveData containing a list of cached {@link Restaurant} objects.
     */
    public LiveData<List<Restaurant>> getDetailedRestaurants() {
        return restaurantRepository.getCachedRestaurants();
    }

    public void addLike(String userId, String restaurantId) {
        restaurantRepository.addLike(userId, restaurantId);
    }

    public void removeLike(String userId, String restaurantId) {
        restaurantRepository.removeLike(userId, restaurantId);
    }

    public LiveData<Boolean> isRestaurantLikedByUser(String userId, String restaurantId) {
        return restaurantRepository.isRestaurantLikedByUser(userId, restaurantId);
    }
}