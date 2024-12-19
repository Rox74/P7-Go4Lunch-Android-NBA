package com.nathba.go4lunch.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.nathba.go4lunch.api.OverpassApi;
import com.nathba.go4lunch.api.YelpApi;
import com.nathba.go4lunch.models.OverpassResponse;
import com.nathba.go4lunch.models.Restaurant;
import com.nathba.go4lunch.models.YelpBusinessResponse;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Repository for managing restaurant data from multiple sources, such as Overpass API, Yelp API,
 * and Firestore. It provides caching capabilities to optimize API calls and avoid redundant requests.
 */
public class RestaurantRepository {

    /** Instance of Overpass API to fetch basic restaurant data. */
    private final OverpassApi overpassApi;

    /** Instance of Yelp API to fetch detailed restaurant data. */
    private final YelpApi yelpApi;

    /** List to cache fetched restaurants to minimize redundant API calls. */
    private List<Restaurant> cachedRestaurants = new ArrayList<>();

    /** LiveData containing the list of cached restaurants for observation. */
    private final MutableLiveData<List<Restaurant>> restaurantsLiveData = new MutableLiveData<>();

    /** Instance of Firebase Firestore to manage restaurant persistence. */
    private final FirebaseFirestore firestore;

    /** Tag for logging purposes. */
    private static final String TAG = "RestaurantRepository";

    /** Map to track the state of fetched restaurant details to avoid duplicate requests. */
    private final Map<String, Boolean> detailsFetchedMap = new HashMap<>();

    private static final String LIKES_COLLECTION = "likes";

    /**
     * Default constructor that initializes API clients and Firestore instance.
     */
    public RestaurantRepository() {
        overpassApi = new OverpassApi();
        yelpApi = new YelpApi();
        firestore = FirebaseFirestore.getInstance();
    }

    /**
     * Retrieves a list of restaurants based on the specified location coordinates.
     * <p>
     * The method fetches data from Overpass API. If the data is already cached, it returns the cached list.
     *
     * @param latitude  The latitude of the user's location.
     * @param longitude The longitude of the user's location.
     * @return A {@link LiveData} object containing the list of restaurants.
     */
    public LiveData<List<Restaurant>> getRestaurants(double latitude, double longitude) {
        MutableLiveData<List<Restaurant>> restaurantsLiveData = new MutableLiveData<>();

        if (!cachedRestaurants.isEmpty()) {
            restaurantsLiveData.setValue(cachedRestaurants);
            return restaurantsLiveData;
        }

        overpassApi.getRestaurants(buildOverpassQuery(latitude, longitude, 500)).enqueue(new Callback<OverpassResponse>() {
            @Override
            public void onResponse(Call<OverpassResponse> call, Response<OverpassResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Restaurant> restaurants = convertToRestaurants(response.body().elements);
                    cachedRestaurants = restaurants;
                    restaurantsLiveData.setValue(restaurants);
                } else {
                    Log.e(TAG, "Failed to fetch restaurants from Overpass API: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<OverpassResponse> call, Throwable t) {
                Log.e(TAG, "Overpass API call failed: " + t.getMessage());
            }
        });

        return restaurantsLiveData;
    }

    /**
     * Retrieves detailed restaurant data from Yelp API.
     *
     * @param restaurantId   The unique identifier of the restaurant.
     * @param location       The geographical location of the restaurant.
     * @param restaurantName The name of the restaurant.
     * @param callback       A callback to handle success or failure of the API call.
     */
    public void getRestaurantDetails(String restaurantId, GeoPoint location, String restaurantName, RepositoryCallback<Restaurant> callback) {
        for (Restaurant restaurant : cachedRestaurants) {
            if (restaurant.getRestaurantId().equals(restaurantId)) {
                Log.d(TAG, "Found restaurant in cache: " + restaurant.getName());
                if (restaurant.getAddress() == null || restaurant.getPhotoUrl() == null) {
                    fetchYelpDetails(restaurant, location, callback);
                } else {
                    callback.onSuccess(restaurant);
                }
                return;
            }
        }

        Log.d(TAG, "Restaurant not found in cache, fetching details from Yelp API");
        Restaurant basicRestaurant = new Restaurant(restaurantId, restaurantName, "", "", 0.0, location, "", "", "", false);
        fetchYelpDetails(basicRestaurant, location, callback);
    }

    /**
     * Fetches restaurant details from Yelp API and updates the provided {@link Restaurant} object.
     *
     * @param restaurant The restaurant object to update.
     * @param location   The location of the restaurant.
     * @param callback   A callback to handle the API response.
     */
    private void fetchYelpDetails(Restaurant restaurant, GeoPoint location, RepositoryCallback<Restaurant> callback) {
        String restaurantName = restaurant.getName();
        String locationQuery = location.getLatitude() + "," + location.getLongitude();

        yelpApi.getRestaurantDetails(restaurantName, locationQuery).enqueue(new Callback<YelpBusinessResponse>() {
            @Override
            public void onResponse(Call<YelpBusinessResponse> call, Response<YelpBusinessResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().businesses.isEmpty()) {
                    YelpBusinessResponse.YelpBusiness yelpData = response.body().businesses.get(0);
                    restaurant.setAddress(yelpData.location.address);
                    restaurant.setPhotoUrl(yelpData.imageUrl);
                    restaurant.setRating(yelpData.rating);
                    restaurant.setPhoneNumber(yelpData.phone);
                    restaurant.setYelpUrl(yelpData.url);

                    callback.onSuccess(restaurant);
                } else {
                    callback.onError(new Exception("Failed to load Yelp details"));
                }
            }

            @Override
            public void onFailure(Call<YelpBusinessResponse> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    /**
     * Builds an Overpass API query string based on the given coordinates and radius.
     *
     * @param latitude  The latitude of the location.
     * @param longitude The longitude of the location.
     * @param radius    The search radius in meters.
     * @return The Overpass API query string.
     */
    private String buildOverpassQuery(double latitude, double longitude, int radius) {
        return String.format(Locale.US, "[out:json];node[\"amenity\"=\"restaurant\"](around:%d,%f,%f);out;", radius, latitude, longitude);
    }

    /**
     * Converts a list of Overpass API response elements into a list of {@link Restaurant} objects.
     *
     * @param elements The list of Overpass API elements.
     * @return A list of {@link Restaurant} objects.
     */
    private List<Restaurant> convertToRestaurants(List<OverpassResponse.Element> elements) {
        List<Restaurant> restaurants = new ArrayList<>();
        for (OverpassResponse.Element element : elements) {
            Restaurant restaurant = new Restaurant(
                    element.id,                               // restaurantId
                    element.tags.name,                        // name
                    element.tags.address,                     // address
                    null,                                     // photoUrl
                    0.0,                                      // rating
                    new GeoPoint(element.lat, element.lon),   // location
                    "",                                       // phoneNumber
                    "",                                       // yelpUrl
                    "",                                       // openingHours
                    false                                     // detailsFetched
            );
            restaurants.add(restaurant);
        }
        return restaurants;
    }

    /**
     * Adds a restaurant to the Firestore "restaurants" collection.
     *
     * @param restaurant The restaurant object to add.
     */
    public void addRestaurantToFirestore(Restaurant restaurant) {
        firestore.collection("restaurants")
                .document(restaurant.getRestaurantId())
                .set(restaurant)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Restaurant added successfully: " + restaurant.getName());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding restaurant: ", e);
                });
    }

    /**
     * Fetches detailed restaurant data for a bulk list of restaurants using Yelp API.
     *
     * @param restaurants The list of restaurants to fetch details for.
     * @return A {@link LiveData} object containing the updated list of restaurants.
     */
    public LiveData<List<Restaurant>> fetchRestaurantsBulk(List<Restaurant> restaurants) {
        Log.d(TAG, "Starting bulk fetch for Yelp details with " + restaurants.size() + " restaurants");

        AtomicInteger pendingCalls = new AtomicInteger(0);

        for (Restaurant restaurant : restaurants) {
            String restaurantId = restaurant.getRestaurantId();
            boolean isFetched = detailsFetchedMap.containsKey(restaurantId) ? detailsFetchedMap.get(restaurantId) : false;
            Log.d(TAG, "Restaurant " + restaurantId + " detailsFetched: " + isFetched);

            if (!isFetched) {
                detailsFetchedMap.put(restaurantId, true);

                Call<YelpBusinessResponse> call = yelpApi.getRestaurantDetails(
                        restaurant.getName(),
                        restaurant.getLocation().getLatitude() + "," + restaurant.getLocation().getLongitude()
                );

                pendingCalls.incrementAndGet();

                call.enqueue(new Callback<YelpBusinessResponse>() {
                    @Override
                    public void onResponse(Call<YelpBusinessResponse> call, Response<YelpBusinessResponse> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().businesses.isEmpty()) {
                            YelpBusinessResponse.YelpBusiness yelpData = response.body().businesses.get(0);
                            restaurant.setAddress(yelpData.location.address);
                            restaurant.setPhotoUrl(yelpData.imageUrl);
                            restaurant.setRating(yelpData.rating);
                            restaurant.setPhoneNumber(yelpData.phone);
                            restaurant.setYelpUrl(yelpData.url);

                            Log.d(TAG, "Yelp details fetched for restaurant: " + restaurant.getName());
                        } else {
                            Log.e(TAG, "Failed to load Yelp details for restaurant: " + restaurant.getName());
                        }

                        if (pendingCalls.decrementAndGet() == 0) {
                            cachedRestaurants = new ArrayList<>(restaurants);
                            restaurantsLiveData.postValue(cachedRestaurants);
                        }
                    }

                    @Override
                    public void onFailure(Call<YelpBusinessResponse> call, Throwable t) {
                        Log.e(TAG, "Yelp API call failed: " + t.getMessage() + " for restaurant: " + restaurant.getName());

                        if (pendingCalls.decrementAndGet() == 0) {
                            cachedRestaurants = new ArrayList<>(restaurants);
                            restaurantsLiveData.postValue(cachedRestaurants);
                        }
                    }
                });
            }
        }

        if (pendingCalls.get() == 0) {
            restaurantsLiveData.postValue(restaurants);
        }

        return restaurantsLiveData;
    }

    /**
     * Returns the cached list of restaurants wrapped in {@link LiveData}.
     *
     * @return A {@link LiveData} object containing the cached list of restaurants.
     */
    public LiveData<List<Restaurant>> getCachedRestaurants() {
        restaurantsLiveData.setValue(cachedRestaurants);
        return restaurantsLiveData;
    }

    /**
     * Adds a "like" for a specific restaurant by a user.
     * The like is stored in the Firestore database.
     *
     * @param userId       The unique identifier of the user liking the restaurant.
     * @param restaurantId The unique identifier of the restaurant being liked.
     */
    public void addLike(String userId, String restaurantId) {
        String likeId = userId + "_" + restaurantId;
        Map<String, Object> likeData = new HashMap<>();
        likeData.put("userId", userId);
        likeData.put("restaurantId", restaurantId);

        firestore.collection(LIKES_COLLECTION)
                .document(likeId)
                .set(likeData)
                .addOnSuccessListener(aVoid -> Log.d("RestaurantRepository", "Like added successfully"))
                .addOnFailureListener(e -> Log.e("RestaurantRepository", "Error adding like", e));
    }

    /**
     * Removes a "like" for a specific restaurant by a user.
     * The like is deleted from the Firestore database.
     *
     * @param userId       The unique identifier of the user.
     * @param restaurantId The unique identifier of the restaurant.
     */
    public void removeLike(String userId, String restaurantId) {
        String likeId = userId + "_" + restaurantId;
        firestore.collection(LIKES_COLLECTION)
                .document(likeId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("RestaurantRepository", "Like removed successfully"))
                .addOnFailureListener(e -> Log.e("RestaurantRepository", "Error removing like", e));
    }

    /**
     * Checks if a user has liked a specific restaurant.
     *
     * @param userId       The unique identifier of the user.
     * @param restaurantId The unique identifier of the restaurant.
     * @return A {@link LiveData} object containing a boolean value:
     *         true if the restaurant is liked by the user, false otherwise.
     */
    public LiveData<Boolean> isRestaurantLikedByUser(String userId, String restaurantId) {
        MutableLiveData<Boolean> isLiked = new MutableLiveData<>();
        String likeId = userId + "_" + restaurantId;

        firestore.collection(LIKES_COLLECTION)
                .document(likeId)
                .get()
                .addOnSuccessListener(documentSnapshot -> isLiked.setValue(documentSnapshot.exists()))
                .addOnFailureListener(e -> isLiked.setValue(false));

        return isLiked;
    }
}