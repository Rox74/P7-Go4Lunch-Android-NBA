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

/**
 * ViewModel class for managing map-related data, including restaurants, user location, and lunch information.
 * <p>
 * This ViewModel serves as an intermediary between the UI layer and the repositories:
 * {@link MapRepository}, {@link LunchRepository}, and {@link RestaurantRepository}.
 * It handles the retrieval and observation of data such as restaurants, user location, and lunches.
 */
public class MapViewModel extends ViewModel {

    /** Repository for handling map-related operations and restaurant data. */
    private final MapRepository mapRepository;

    /** Repository for managing lunch data. */
    private final LunchRepository lunchRepository;

    /** Repository for managing detailed restaurant data. */
    private final RestaurantRepository restaurantRepository;

    /** LiveData containing a list of nearby restaurants. */
    private final LiveData<List<Restaurant>> restaurants;

    /** LiveData containing a list of lunches. */
    private final LiveData<List<Lunch>> lunches;

    /** MutableLiveData to hold the currently selected restaurant details. */
    private final MutableLiveData<Restaurant> selectedRestaurant = new MutableLiveData<>();

    /** MutableLiveData to store the current user's location. */
    private final MutableLiveData<Location> userLocation = new MutableLiveData<>();

    /** MutableLiveData to hold the list of detailed restaurants. */
    private final MutableLiveData<List<Restaurant>> detailedRestaurants = new MutableLiveData<>();

    /** MutableLiveData to hold the list of lunches for today. */
    private final MutableLiveData<List<Lunch>> lunchesToday = new MutableLiveData<>();

    /** Tag used for logging purposes. */
    private static final String TAG = "MapViewModel";

    /** Boolean to ensure restaurant details are fetched only once. */
    private boolean detailsFetched = false;

    /**
     * Constructor for {@link MapViewModel}.
     * <p>
     * Initializes the repositories and fetches initial data for restaurants and lunches.
     *
     * @param mapRepository        The repository responsible for map-related operations.
     * @param lunchRepository      The repository responsible for lunch data.
     * @param restaurantRepository The repository responsible for restaurant details.
     */
    public MapViewModel(MapRepository mapRepository, LunchRepository lunchRepository, RestaurantRepository restaurantRepository) {
        this.mapRepository = mapRepository;
        this.lunchRepository = lunchRepository;
        this.restaurantRepository = restaurantRepository;
        this.restaurants = mapRepository.getRestaurants();
        this.lunches = lunchRepository.getLunches();
    }

    /**
     * Fetches the details of a specific restaurant and updates the selected restaurant LiveData.
     *
     * @param restaurantId   The unique identifier of the restaurant.
     * @param location       The geographical location (latitude, longitude) of the restaurant.
     * @param restaurantName The name of the restaurant.
     */
    public void fetchRestaurantDetails(String restaurantId, GeoPoint location, String restaurantName) {
        restaurantRepository.getRestaurantDetails(restaurantId, location, restaurantName, new RepositoryCallback<Restaurant>() {
            @Override
            public void onSuccess(Restaurant restaurant) {
                selectedRestaurant.setValue(restaurant);
            }

            @Override
            public void onError(Throwable t) {
                // Create a basic Restaurant object with default values in case of an error
                Restaurant basicRestaurant = new Restaurant(restaurantId, restaurantName, "", "", 0.0, location, "", "", "", false);
                selectedRestaurant.setValue(basicRestaurant);
            }
        });
    }

    /**
     * Returns the currently selected restaurant as a {@link LiveData}.
     *
     * @return LiveData containing the details of the selected restaurant.
     */
    public LiveData<Restaurant> getSelectedRestaurant() {
        return selectedRestaurant;
    }

    /**
     * Returns the list of nearby restaurants as a {@link LiveData}.
     *
     * @return LiveData containing the list of {@link Restaurant} objects.
     */
    public LiveData<List<Restaurant>> getRestaurants() {
        return restaurants;
    }

    /**
     * Sets the user's current location.
     *
     * @param location The {@link Location} object representing the user's current geographical position.
     */
    public void setUserLocation(Location location) {
        userLocation.setValue(location);
    }

    /**
     * Returns the list of lunches as a {@link LiveData}.
     *
     * @return LiveData containing the list of {@link Lunch} objects.
     */
    public LiveData<List<Lunch>> getLunches() {
        return lunches;
    }

    /**
     * Loads the list of restaurants based on the user's geographical position.
     *
     * @param latitude  The latitude of the user's location.
     * @param longitude The longitude of the user's location.
     */
    public void loadRestaurants(double latitude, double longitude) {
        mapRepository.loadRestaurants(latitude, longitude);
    }

    /**
     * Fetches detailed restaurant information in bulk, ensuring that details are only loaded once.
     *
     * @param restaurants A list of {@link Restaurant} objects for which details are to be fetched.
     */
    public void fetchRestaurantsDetailsIfNeeded(List<Restaurant> restaurants) {
        if (!detailsFetched) {
            restaurantRepository.fetchRestaurantsBulk(restaurants).observeForever(detailedRestaurants::setValue);
            detailsFetched = true;  // Mark details as fetched
        }
    }

    /**
     * Returns the list of detailed restaurants as a {@link LiveData}.
     *
     * @return LiveData containing a list of detailed {@link Restaurant} objects.
     */
    public LiveData<List<Restaurant>> getDetailedRestaurants() {
        return detailedRestaurants;
    }

    /**
     * Returns the list of today's lunches as a {@link LiveData}.
     *
     * @return LiveData containing the list of {@link Lunch} objects for today.
     */
    public LiveData<List<Lunch>> getLunchesToday() {
        return lunchesToday;
    }

    /**
     * Loads the list of today's lunches and updates the corresponding LiveData.
     */
    public void loadLunchesToday() {
        lunchRepository.getLunchesToday().observeForever(lunchesToday::setValue);
    }
}