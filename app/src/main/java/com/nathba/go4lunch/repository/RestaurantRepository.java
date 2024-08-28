package com.nathba.go4lunch.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nathba.go4lunch.models.Restaurant;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for managing restaurant data in Firestore.
 * Provides methods to retrieve and add restaurant records.
 */
public class RestaurantRepository {

    // Reference to the "restaurants" collection in Firestore
    private final CollectionReference restaurantsCollection;

    /**
     * Constructs a RestaurantRepository instance.
     * Initializes the Firestore database reference for the "restaurants" collection.
     */
    public RestaurantRepository() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        restaurantsCollection = db.collection("restaurants");
    }

    /**
     * Gets a LiveData object containing a list of restaurants from Firestore.
     * Listens for real-time updates to the "restaurants" collection.
     *
     * @return LiveData containing a list of Restaurant objects
     */
    public LiveData<List<Restaurant>> getRestaurants() {
        MutableLiveData<List<Restaurant>> restaurantsLiveData = new MutableLiveData<>();

        // Add a snapshot listener to listen for changes in the "restaurants" collection
        restaurantsCollection.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                // Log error if there's an issue with the snapshot retrieval
                Log.e("RestaurantRepository", "Error while retrieving restaurants", e);
                return;
            }
            if (snapshots != null) {
                List<Restaurant> restaurants = new ArrayList<>();
                // Iterate over each document in the snapshot
                for (DocumentSnapshot document : snapshots.getDocuments()) {
                    // Extract restaurant details from the document
                    String restaurantId = document.getString("restaurantId");
                    String name = document.getString("name");
                    String address = document.getString("address");
                    String photoUrl = document.getString("photoUrl");
                    Double rating = document.getDouble("rating");

                    // Handle potential null values and default rating if not available
                    double restaurantRating = rating != null ? rating : 0.0;

                    // Create a Restaurant object with the extracted data
                    Restaurant restaurant = new Restaurant(restaurantId, name, address, photoUrl, restaurantRating, null);
                    restaurants.add(restaurant);
                }
                // Update the LiveData with the list of restaurants
                restaurantsLiveData.setValue(restaurants);
            }
        });

        return restaurantsLiveData;
    }

    /**
     * Adds a restaurant record to Firestore.
     * The restaurant record is stored under its unique ID.
     *
     * @param restaurant the Restaurant object to be added
     */
    public void addRestaurant(Restaurant restaurant) {
        restaurantsCollection.document(restaurant.getRestaurantId())
                .set(restaurant)
                .addOnSuccessListener(aVoid -> {
                    // Log success message when the restaurant is added successfully
                    Log.d("RestaurantRepository", "Restaurant added successfully");
                })
                .addOnFailureListener(e -> {
                    // Log error message if there's an issue adding the restaurant
                    Log.e("RestaurantRepository", "Error while adding restaurant", e);
                });
    }
}