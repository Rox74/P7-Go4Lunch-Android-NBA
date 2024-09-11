package com.nathba.go4lunch.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nathba.go4lunch.api.OverpassApi;
import com.nathba.go4lunch.api.YelpApi;
import com.nathba.go4lunch.models.Lunch;
import com.nathba.go4lunch.models.OverpassResponse;
import com.nathba.go4lunch.models.Restaurant;
import com.nathba.go4lunch.models.YelpBusinessResponse;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestaurantRepository {

    private final OverpassApi overpassApi;
    private final YelpApi yelpApi;
    private List<Restaurant> cachedRestaurants = new ArrayList<>(); // Cache des restaurants

    public RestaurantRepository() {
        overpassApi = new OverpassApi();
        yelpApi = new YelpApi();
    }

    public LiveData<List<Restaurant>> getRestaurants() {
        MutableLiveData<List<Restaurant>> restaurantsLiveData = new MutableLiveData<>();
        restaurantsLiveData.setValue(cachedRestaurants); // Retourner les restaurants en cache
        return restaurantsLiveData;
    }

    // Appel API pour récupérer les restaurants (exécuté une seule fois)
    public void fetchRestaurantsFromApi(RepositoryCallback<List<Restaurant>> callback) {
        if (!cachedRestaurants.isEmpty()) {
            // Si les restaurants sont en cache, les retourner
            callback.onSuccess(cachedRestaurants);
            return;
        }

        String overpassQuery = buildOverpassQuery(47.3123, 5.0914, 500);
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
        // Chercher dans le cache si le restaurant a déjà été récupéré
        for (Restaurant restaurant : cachedRestaurants) {
            if (restaurant.getRestaurantId().equals(restaurantId)) {
                Log.d("RestaurantRepository", "Found restaurant in cache: " + restaurant.getName());

                // Si le restaurant n'a pas encore les détails de Yelp, les récupérer
                if (restaurant.getAddress() == null || restaurant.getPhotoUrl() == null) {
                    fetchYelpDetails(restaurant, location, callback);  // Correction : Passer les coordonnées GPS
                } else {
                    callback.onSuccess(restaurant);  // Retourner les détails du restaurant si déjà complet
                }
                return;
            }
        }

        // Si le restaurant n'est pas dans le cache, le récupérer de Yelp en utilisant son nom et coordonnées GPS
        Log.e("RestaurantRepository", "Restaurant not found in cache: " + restaurantId);
        fetchYelpDetails(new Restaurant(restaurantId, restaurantName, null, null, 0, location), location, callback);  // Correction : Passer les coordonnées GPS
    }

    private void fetchYelpDetails(Restaurant restaurant, GeoPoint location, RepositoryCallback<Restaurant> callback) {
        // Utilisation des coordonnées GPS pour rechercher sur Yelp
        String locationQuery = location.getLatitude() + "," + location.getLongitude();  // Créer une chaîne pour les coordonnées GPS
        yelpApi.getRestaurantDetails(restaurant.getName(), locationQuery).enqueue(new Callback<YelpBusinessResponse>() {
            @Override
            public void onResponse(Call<YelpBusinessResponse> call, Response<YelpBusinessResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().businesses.isEmpty()) {
                    YelpBusinessResponse.YelpBusiness yelpData = response.body().businesses.get(0);
                    restaurant.setAddress(yelpData.location.address);  // Récupérer l'adresse Yelp
                    restaurant.setPhotoUrl(yelpData.imageUrl);  // Récupérer la photo Yelp
                    restaurant.setRating(yelpData.rating);  // Récupérer la note Yelp
                    restaurant.setPhoneNumber(yelpData.phone);  // Récupérer le numéro de téléphone Yelp
                    restaurant.setYelpUrl(yelpData.url);  // Récupérer l'URL Yelp

                    cachedRestaurants.add(restaurant);  // Ajouter le restaurant au cache
                    callback.onSuccess(restaurant);  // Retourner les détails du restaurant via le callback
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
                });
    }

    private void addRestaurantToFirestore(Restaurant restaurant) {
        FirebaseFirestore.getInstance().collection("restaurants")
                .document(restaurant.getRestaurantId())
                .set(restaurant)
                .addOnSuccessListener(aVoid -> Log.d("RestaurantRepository", "Restaurant ajouté avec succès"))
                .addOnFailureListener(e -> Log.e("RestaurantRepository", "Erreur lors de l'ajout du restaurant", e));
    }

    private String buildOverpassQuery(double latitude, double longitude, int radius) {
        return String.format(Locale.US,
                "[out:json];node[\"amenity\"=\"restaurant\"](around:%d,%f,%f);out;",
                radius, latitude, longitude);
    }
}