package com.nathba.go4lunch.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nathba.go4lunch.api.OverpassApi;
import com.nathba.go4lunch.api.YelpApi;
import com.nathba.go4lunch.models.Lunch;
import com.nathba.go4lunch.models.OverpassResponse;
import com.nathba.go4lunch.models.Restaurant;
import com.nathba.go4lunch.models.YelpBusinessResponse;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class RestaurantRepository {

    private final OverpassApi overpassApi;
    private final YelpApi yelpApi;
    private List<Restaurant> cachedRestaurants = new ArrayList<>(); // Cache des restaurants
    private final MutableLiveData<List<Restaurant>> restaurantsLiveData = new MutableLiveData<>();
    private final FirebaseFirestore firestore;
    private static final String TAG = "RestaurantRepository";

    // Map pour suivre l'état de récupération
    private final Map<String, Boolean> detailsFetchedMap = new HashMap<>();

    public RestaurantRepository() {
        overpassApi = new OverpassApi();
        yelpApi = new YelpApi();
        firestore = FirebaseFirestore.getInstance();
    }

    // Récupérer les restaurants uniquement à partir de plusieurs sources (API Overpass + Yelp)
    public LiveData<List<Restaurant>> getRestaurants(double latitude, double longitude) {
        MutableLiveData<List<Restaurant>> restaurantsLiveData = new MutableLiveData<>();

        // Utiliser les restaurants mis en cache s'ils existent
        if (!cachedRestaurants.isEmpty()) {
            restaurantsLiveData.setValue(cachedRestaurants);
            return restaurantsLiveData;
        }

        // Fetch restaurants depuis Overpass API
        overpassApi.getRestaurants(buildOverpassQuery(latitude, longitude, 500)).enqueue(new Callback<OverpassResponse>() {
            @Override
            public void onResponse(Call<OverpassResponse> call, Response<OverpassResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Restaurant> restaurants = convertToRestaurants(response.body().elements);
                    cachedRestaurants = restaurants; // Cache les restaurants récupérés
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

    // Récupérer les détails du restaurant depuis Yelp
    public void getRestaurantDetails(String restaurantId, GeoPoint location, String restaurantName, RepositoryCallback<Restaurant> callback) {
        // Vérification dans le cache
        for (Restaurant restaurant : cachedRestaurants) {
            if (restaurant.getRestaurantId().equals(restaurantId)) {
                Log.d(TAG, "Found restaurant in cache: " + restaurant.getName());
                // Si des détails sont manquants, fetch depuis Yelp
                if (restaurant.getAddress() == null || restaurant.getPhotoUrl() == null) {
                    fetchYelpDetails(restaurant, location, callback);
                } else {
                    callback.onSuccess(restaurant); // Retourne immédiatement le restaurant depuis le cache
                }
                return;
            }
        }

        // Si pas trouvé dans le cache, fetch depuis Yelp
        Log.d(TAG, "Restaurant not found in cache, fetching details from Yelp API");
        Restaurant basicRestaurant = new Restaurant(restaurantId, restaurantName, "", "", 0.0, location, "", "", "", false);
        fetchYelpDetails(basicRestaurant, location, callback);
    }

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

    // Méthode pour construire la requête Overpass
    private String buildOverpassQuery(double latitude, double longitude, int radius) {
        return String.format(Locale.US, "[out:json];node[\"amenity\"=\"restaurant\"](around:%d,%f,%f);out;", radius, latitude, longitude);
    }

    // Conversion de la réponse Overpass en objets Restaurant
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

    public LiveData<List<Restaurant>> fetchRestaurantsBulk(List<Restaurant> restaurants) {
        Log.d(TAG, "Starting bulk fetch for Yelp details with " + restaurants.size() + " restaurants");

        AtomicInteger pendingCalls = new AtomicInteger(0);

        for (Restaurant restaurant : restaurants) {
            String restaurantId = restaurant.getRestaurantId();
            boolean isFetched = detailsFetchedMap.containsKey(restaurantId) ? detailsFetchedMap.get(restaurantId) : false;
            Log.d(TAG, "Restaurant " + restaurantId + " detailsFetched: " + isFetched);

            if (!isFetched) {
                // Met à jour le Map pour éviter les appels multiples
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

        // Si aucun appel n'a été ajouté, publie immédiatement les restaurants
        if (pendingCalls.get() == 0) {
            restaurantsLiveData.postValue(restaurants);
        }

        return restaurantsLiveData;
    }

    public LiveData<List<Restaurant>> getCachedRestaurants() {
        // Retourne les restaurants mis en cache dans un LiveData
        restaurantsLiveData.setValue(cachedRestaurants);
        return restaurantsLiveData;
    }
}