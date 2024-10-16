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

public class RestaurantRepository {

    private final OverpassApi overpassApi;
    private final YelpApi yelpApi;
    private List<Restaurant> cachedRestaurants = new ArrayList<>(); // Cache des restaurants
    private final FirebaseFirestore firestore;
    private static final String TAG = "RestaurantRepository";

    public RestaurantRepository() {
        overpassApi = new OverpassApi();
        yelpApi = new YelpApi();
        firestore = FirebaseFirestore.getInstance();
    }

    // Récupérer les restaurants à partir de plusieurs sources (API Overpass + Yelp + Firebase)
    public LiveData<List<Restaurant>> getRestaurants(double latitude, double longitude) {
        MutableLiveData<List<Restaurant>> restaurantsLiveData = new MutableLiveData<>();

        // Vérifier si les restaurants sont en cache
        if (!cachedRestaurants.isEmpty()) {
            restaurantsLiveData.setValue(cachedRestaurants);
            return restaurantsLiveData;
        }

        // Fetch restaurants depuis Overpass
        overpassApi.getRestaurants(buildOverpassQuery(latitude, longitude, 500)).enqueue(new Callback<OverpassResponse>() {
            @Override
            public void onResponse(Call<OverpassResponse> call, Response<OverpassResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Restaurant> restaurants = convertToRestaurants(response.body().elements);
                    cachedRestaurants = restaurants;
                    fetchLunchesForRestaurants(restaurants, restaurantsLiveData);
                }
            }

            @Override
            public void onFailure(Call<OverpassResponse> call, Throwable t) {
                // Gérer l'erreur ici
            }
        });

        return restaurantsLiveData;
    }

    // Fetch lunches liés à chaque restaurant
    private void fetchLunchesForRestaurants(List<Restaurant> restaurants, MutableLiveData<List<Restaurant>> restaurantsLiveData) {
        firestore.collection("lunches")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Map<String, List<Lunch>> lunchMap = new HashMap<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            Lunch lunch = document.toObject(Lunch.class);
                            if (lunch != null) {
                                if (!lunchMap.containsKey(lunch.getRestaurantId())) {
                                    lunchMap.put(lunch.getRestaurantId(), new ArrayList<>()); // Créer une nouvelle liste si elle n'existe pas
                                }
                                lunchMap.get(lunch.getRestaurantId()).add(lunch); // Ajouter le lunch à la liste existante
                            }
                        }

                        // Assigner les lunchs aux restaurants
                        for (Restaurant restaurant : restaurants) {
                            List<Lunch> lunches = lunchMap.get(restaurant.getRestaurantId());
                            if (lunches != null) {
                                restaurant.setLunches(lunches);
                            }
                        }

                        restaurantsLiveData.setValue(restaurants);
                    }
                });
    }

    public void fetchRestaurantsFromApi(double latitude, double longitude, RepositoryCallback<List<Restaurant>> callback) {
        if (!cachedRestaurants.isEmpty()) {
            // Si les restaurants sont en cache, les retourner
            Log.d(TAG, "Returning cached restaurants: " + cachedRestaurants.size());
            callback.onSuccess(cachedRestaurants);
            return;
        }

        // Utiliser les coordonnées de l'utilisateur pour construire la requête
        String overpassQuery = buildOverpassQuery(latitude, longitude, 500);
        Log.d(TAG, "Fetching restaurants from Overpass API with query: " + overpassQuery);

        overpassApi.getRestaurants(overpassQuery).enqueue(new Callback<OverpassResponse>() {
            @Override
            public void onResponse(Call<OverpassResponse> call, Response<OverpassResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Restaurant> restaurants = convertToRestaurants(response.body().elements);
                    cachedRestaurants = restaurants;  // Stocker les restaurants en cache
                    Log.d(TAG, "Successfully fetched restaurants: " + cachedRestaurants.size());
                    callback.onSuccess(cachedRestaurants);
                } else {
                    Log.e(TAG, "API Error: " + response.code());
                    callback.onError(new Exception("API Error"));
                }
            }

            @Override
            public void onFailure(Call<OverpassResponse> call, Throwable t) {
                Log.e(TAG, "Failed to fetch restaurants: " + t.getMessage(), t);
                callback.onError(t);
            }
        });
    }

    // Conversion des éléments Overpass en objets Restaurant
    private List<Restaurant> convertToRestaurants(List<OverpassResponse.Element> elements) {
        List<Restaurant> restaurants = new ArrayList<>();
        for (OverpassResponse.Element element : elements) {
            // Créer un nouvel objet Restaurant avec tous les paramètres requis
            Restaurant restaurant = new Restaurant(
                    element.id,  // restaurantId
                    element.tags.name,  // name
                    element.tags.address,  // address
                    null,  // photoUrl (ou une valeur par défaut si nécessaire)
                    0.0,  // rating (ou une valeur par défaut)
                    new GeoPoint(element.lat, element.lon),  // location
                    "",  // phoneNumber (valeur par défaut)
                    "",  // yelpUrl (valeur par défaut)
                    "",  // openingHours (valeur par défaut)
                    new ArrayList<>()  // Initialiser la liste des Lunchs
            );
            restaurants.add(restaurant);
        }
        return restaurants;
    }

    public void getRestaurantDetails(String restaurantId, GeoPoint location, String restaurantName, RepositoryCallback<Restaurant> callback) {
        // Vérification du cache
        for (Restaurant restaurant : cachedRestaurants) {
            if (restaurant.getRestaurantId().equals(restaurantId)) {
                Log.d(TAG, "Found restaurant in cache: " + restaurant.getName());

                // Si des détails sont manquants, récupère plus d'infos
                if (restaurant.getAddress() == null || restaurant.getPhotoUrl() == null) {
                    fetchYelpDetails(restaurant, location, callback);
                } else {
                    callback.onSuccess(restaurant);  // Renvoie immédiatement le restaurant depuis le cache
                }
                return;
            }
        }

        // Si pas trouvé dans le cache, appel à Yelp
        Log.d(TAG, "Restaurant not found in cache, fetching details from Yelp API");
        // Passer des valeurs par défaut pour les champs manquants
        Restaurant basicRestaurant = new Restaurant(restaurantId, restaurantName, "", "", 0.0, location, "", "", "", new ArrayList<>());
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

                    // Vérification et extraction des horaires
                    if (yelpData.hours != null && !yelpData.hours.isEmpty()) {
                        YelpBusinessResponse.YelpHours hours = yelpData.hours.get(0);
                        for (YelpBusinessResponse.YelpOpen open : hours.open) {
                            // Traiter les informations d'ouverture (jours, horaires)
                            Log.d("YelpApi", "Day: " + open.day + " Open: " + open.start + " Close: " + open.end);
                        }
                    }

                    cachedRestaurants.add(restaurant);
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

    public void addLunch(Lunch lunch, Restaurant restaurant) {
        Log.d(TAG, "Adding lunch for restaurant: " + restaurant.getName());
        // Sauvegarder le lunch dans Firestore
        FirebaseFirestore.getInstance().collection("lunches")
                .document(lunch.getLunchId())
                .set(lunch)
                .addOnSuccessListener(aVoid -> {
                    // Une fois le lunch sauvegardé, sauvegarder le restaurant si nécessaire
                    addRestaurantToFirestore(restaurant);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding lunch: ", e);
                });
    }

    private void addRestaurantToFirestore(Restaurant restaurant) {
        Log.d(TAG, "Adding restaurant to Firestore: " + restaurant.getName());
        FirebaseFirestore.getInstance().collection("restaurants")
                .document(restaurant.getRestaurantId())
                .set(restaurant)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Restaurant added successfully: " + restaurant.getName());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding restaurant: ", e);
                });
    }

    private String buildOverpassQuery(double latitude, double longitude, int radius) {
        return String.format(Locale.US, "[out:json];node[\"amenity\"=\"restaurant\"](around:%d,%f,%f);out;", radius, latitude, longitude);
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
                        Log.d(TAG, "Found lunches for restaurant: " + restaurantId + " - " + lunches.size() + " found.");
                    } else {
                        Log.e(TAG, "Error getting lunches: ", task.getException());
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