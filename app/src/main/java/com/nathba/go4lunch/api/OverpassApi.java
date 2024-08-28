package com.nathba.go4lunch.api;

import com.nathba.go4lunch.models.OverpassResponse;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * This class provides an interface for interacting with the Overpass API to retrieve
 * data about restaurants from the OpenStreetMap database.
 * It uses Retrofit for making network requests and Gson for JSON deserialization.
 */
public class OverpassApi {

    // Base URL for the Overpass API
    private static final String BASE_URL = "https://overpass-api.de/api/";

    // The Retrofit service used to make API calls
    private final OverpassService service;

    /**
     * Constructor for the OverpassApi class.
     * Initializes Retrofit with the base URL and Gson converter, and creates the service.
     */
    public OverpassApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)  // Set the base URL for API requests
                .addConverterFactory(GsonConverterFactory.create())  // Add Gson converter for JSON parsing
                .build();  // Build the Retrofit instance

        // Create an implementation of the OverpassService interface
        service = retrofit.create(OverpassService.class);
    }

    /**
     * Retrieves a list of restaurants based on the provided query.
     *
     * @param query The Overpass API query to filter the restaurants.
     * @return A Call object which can be used to request the restaurant data from the API.
     */
    public Call<OverpassResponse> getRestaurants(String query) {
        return service.getRestaurants(query);
    }

    /**
     * Service interface for Overpass API endpoints.
     * Defines the methods for making network requests.
     */
    private interface OverpassService {

        /**
         * Sends a GET request to the Overpass API to retrieve restaurant data.
         *
         * @param query The Overpass API query to filter the restaurants.
         * @return A Call object which can be used to request the restaurant data from the API.
         */
        @GET("interpreter")
        Call<OverpassResponse> getRestaurants(@Query("data") String query);
    }
}