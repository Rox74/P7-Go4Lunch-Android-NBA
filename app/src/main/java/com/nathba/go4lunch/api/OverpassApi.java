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
 * <p>
 * It uses Retrofit for making network requests and Gson for JSON deserialization.
 */
public class OverpassApi {

    /** Base URL for the Overpass API */
    private static final String BASE_URL = "https://overpass-api.de/api/";

    /** Retrofit service for making Overpass API calls */
    private final OverpassService service;

    /**
     * Constructor for the OverpassApi class.
     * <p>
     * Initializes Retrofit with the base URL and a Gson converter for JSON parsing.
     * Creates an implementation of the OverpassService interface.
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
     * <p>
     * The query should be a valid Overpass QL query, which filters restaurant data
     * within a specific area or condition.
     *
     * @param query The Overpass API query to filter the restaurants.
     * @return A {@link Call} object which can be used to execute the API request
     *         asynchronously or synchronously.
     */
    public Call<OverpassResponse> getRestaurants(String query) {
        return service.getRestaurants(query);
    }

    /**
     * Service interface for Overpass API endpoints.
     * <p>
     * This interface defines methods to interact with the Overpass API using Retrofit.
     */
    private interface OverpassService {

        /**
         * Sends a GET request to the Overpass API to retrieve restaurant data.
         * <p>
         * This method calls the "/interpreter" endpoint of the Overpass API, passing
         * a query string to fetch specific data such as restaurants.
         *
         * @param query The Overpass API query to filter the restaurants.
         * @return A {@link Call} object representing the HTTP request and response.
         */
        @GET("interpreter")
        Call<OverpassResponse> getRestaurants(@Query("data") String query);
    }
}