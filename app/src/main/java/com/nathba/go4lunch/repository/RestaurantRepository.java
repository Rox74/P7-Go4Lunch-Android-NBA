package com.nathba.go4lunch.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestaurantRepository {

    private final OverpassApi overpassApi;
    private final YelpApi yelpApi;
    private List<Restaurant> cachedRestaurants = new ArrayList<>(); // Cache des restaurants
    private final FirebaseFirestore firestore;

    public RestaurantRepository() {
        overpassApi = new OverpassApi();
        yelpApi = new YelpApi();
        firestore = FirebaseFirestore.getInstance();
    }

    public LiveData<List<Restaurant>> getRestaurants() {
        MutableLiveData<List<Restaurant>> restaurantsLiveData = new MutableLiveData<>();
        restaurantsLiveData.setValue(cachedRestaurants); // Retourner les restaurants en cache
        return restaurantsLiveData;
    }

    public void fetchRestaurantsFromApi(double latitude, double longitude, RepositoryCallback<List<Restaurant>> callback) {
        if (!cachedRestaurants.isEmpty()) {
            // Si les restaurants sont en cache, les retourner
            callback.onSuccess(cachedRestaurants);
            return;
        }

        // Utiliser les coordonnées de l'utilisateur pour construire la requête
        String overpassQuery = buildOverpassQuery(latitude, longitude, 500);
        overpassApi.getRestaurants(overpassQuery).enqueue(new Callback<OverpassResponse>() {
            @Override
            public void onResponse(Call<OverpassResponse> call, Response<OverpassResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Restaurant> restaurants = convertToRestaurants(response.body().elements);
                    cachedRestaurants = restaurants;  // Stocker les restaurants en cache
                    callback.onSuccess(cachedRestaurants);
                } else {
                    callback.onError(new Exception("API Error"));
                }
            }

            @Override
            public void onFailure(Call<OverpassResponse> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    // Conversion des données Overpass en objets Restaurant
    private List<Restaurant> convertToRestaurants(List<OverpassResponse.Element> elements) {
        List<Restaurant> restaurants = new ArrayList<>();
        for (OverpassResponse.Element element : elements) {
            Restaurant restaurant = new Restaurant();
            restaurant.setRestaurantId(element.id);
            restaurant.setName(element.tags.name);
            restaurant.setAddress(element.tags.address);
            restaurant.setLocation(new GeoPoint(element.lat, element.lon));  // Utiliser les coordonnées GPS
            restaurants.add(restaurant);
        }
        return restaurants;
    }

    public void getRestaurantDetails(String restaurantId, GeoPoint location, String restaurantName, RepositoryCallback<Restaurant> callback) {
        for (Restaurant restaurant : cachedRestaurants) {
            if (restaurant.getRestaurantId().equals(restaurantId)) {
                Log.d("RestaurantRepository", "Found restaurant in cache: " + restaurant.getName());

                if (restaurant.getAddress() == null || restaurant.getPhotoUrl() == null) {
                    fetchYelpDetails(restaurant, location, callback); // Utiliser les coordonnées GPS
                } else {
                    callback.onSuccess(restaurant);
                }
                return;
            }
        }

        // Si pas dans le cache, appel à Yelp avec le nom et les coordonnées GPS
        Log.e("RestaurantRepository", "Restaurant not found in cache: " + restaurantId);
        fetchYelpDetails(new Restaurant(restaurantId, restaurantName, null, null, 0, location), location, callback);
    }

    private void fetchYelpDetails(Restaurant restaurant, GeoPoint location, RepositoryCallback<Restaurant> callback) {
        String locationQuery = location.getLatitude() + "," + location.getLongitude(); // Chaîne pour les coordonnées GPS
        yelpApi.getRestaurantDetails(restaurant.getName(), locationQuery).enqueue(new Callback<YelpBusinessResponse>() {
            @Override
            public void onResponse(Call<YelpBusinessResponse> call, Response<YelpBusinessResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().businesses.isEmpty()) {
                    YelpBusinessResponse.YelpBusiness yelpData = response.body().businesses.get(0);
                    restaurant.setAddress(yelpData.location.address);
                    restaurant.setPhotoUrl(yelpData.imageUrl);
                    restaurant.setRating(yelpData.rating);
                    restaurant.setPhoneNumber(yelpData.phone);
                    restaurant.setYelpUrl(yelpData.url);

                    cachedRestaurants.add(restaurant); // Ajouter au cache
                    callback.onSuccess(restaurant);  // Retourner les détails via le callback
                } else {
                    Log.e("YelpApi", "Failed to load Yelp details. Response code: " + response.code());
                    callback.onError(new Exception("Failed to load Yelp details"));
                }
            }

            @Override
            public void onFailure(Call<YelpBusinessResponse> call, Throwable t) {
                Log.e("YelpApi", "Yelp API request failed: " + t.getMessage());
                callback.onError(t);
            }
        });
    }

    public void addLunch(Lunch lunch, Restaurant restaurant) {
        // Sauvegarder le lunch dans Firestore
        FirebaseFirestore.getInstance().collection("lunches")
                .document(lunch.getLunchId())
                .set(lunch)
                .addOnSuccessListener(aVoid -> {
                    // Une fois le lunch sauvegardé, sauvegarder le restaurant si nécessaire
                    addRestaurantToFirestore(restaurant);
                })
                .addOnFailureListener(e -> {
                    Log.e("RestaurantRepository", "Erreur lors de l'ajout du lunch", e);
                });
    }

    private void addRestaurantToFirestore(Restaurant restaurant) {
        FirebaseFirestore.getInstance().collection("restaurants")
                .document(restaurant.getRestaurantId())
                .set(restaurant)
                .addOnSuccessListener(aVoid -> {
                    Log.d("RestaurantRepository", "Restaurant ajouté avec succès");
                })
                .addOnFailureListener(e -> {
                    Log.e("RestaurantRepository", "Erreur lors de l'ajout du restaurant", e);
                });
    }

    private String buildOverpassQuery(double latitude, double longitude, int radius) {
        return String.format(Locale.US,
                "[out:json];node[\"amenity\"=\"restaurant\"](around:%d,%f,%f);out;",
                radius, latitude, longitude);
    }

    public LiveData<List<Lunch>> getLunchesForRestaurantToday(String restaurantId) {
        MutableLiveData<List<Lunch>> lunchesLiveData = new MutableLiveData<>();
        Date today = getToday();  // Récupérer la date d'aujourd'hui sans l'heure

        firestore.collection("lunches")
                .whereEqualTo("restaurantId", restaurantId)
                .whereGreaterThanOrEqualTo("date", today)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Lunch> lunches = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Lunch lunch = document.toObject(Lunch.class);
                            lunches.add(lunch);
                        }
                        lunchesLiveData.setValue(lunches);
                    } else {
                        lunchesLiveData.setValue(null);
                    }
                });

        return lunchesLiveData;
    }

    private Date getToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}