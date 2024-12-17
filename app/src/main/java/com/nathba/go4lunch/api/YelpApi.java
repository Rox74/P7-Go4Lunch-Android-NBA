package com.nathba.go4lunch.api;

import com.nathba.go4lunch.models.Restaurant;
import com.nathba.go4lunch.models.YelpBusinessResponse;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * This class provides an interface to interact with the Yelp Fusion API for retrieving
 * restaurant details and search results.
 * <p>
 * It uses Retrofit for making HTTP requests, Gson for JSON deserialization,
 * and OkHttp for adding headers such as the API key.
 */
public class YelpApi {

    /** Base URL for the Yelp Fusion API */
    private static final String BASE_URL = "https://api.yelp.com/v3/";

    /** API key for authenticating Yelp API requests */
    private static final String API_KEY = "KxudpBfyyIQppNyBd0j1QhavtEucbImVrw4pr2YL24PJsH_sA7Svew7kHYAqyzNdRivrnb0uSBoDtIMFwOZEiugAJ09aAI8EvNLQJiphvXp57g70xbOQhtnd9XlhZ3Yx";

    /** Retrofit service for interacting with the Yelp Fusion API */
    private final YelpService yelpService;

    /**
     * Constructor for YelpApi.
     * <p>
     * Initializes Retrofit with the base URL, Gson converter, and an OkHttp client
     * for adding the Authorization header with the Yelp API key.
     */
    public YelpApi() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    // Interceptor to add the Authorization header to every request
                    Request request = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer " + API_KEY)
                            .build();
                    return chain.proceed(request);
                })
                .build();

        // Initialize Retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)  // Set the base URL for the Yelp API
                .client(client)  // Attach OkHttp client with Authorization interceptor
                .addConverterFactory(GsonConverterFactory.create())  // Add Gson for JSON conversion
                .build();

        // Create an implementation of the YelpService interface
        yelpService = retrofit.create(YelpService.class);
    }

    /**
     * Retrieves restaurant details by searching for a restaurant based on its name and location.
     *
     * @param restaurantName The name of the restaurant to search for.
     * @param location       The location (latitude, longitude) or city where to search for the restaurant.
     * @return A {@link Call} object that can be used to request the restaurant data from the Yelp API.
     */
    public Call<YelpBusinessResponse> getRestaurantDetails(String restaurantName, String location) {
        return yelpService.searchRestaurants(restaurantName, location);
    }

    /**
     * Service interface for Yelp Fusion API endpoints.
     * <p>
     * This interface defines the GET request for searching businesses (restaurants)
     * using the Yelp Fusion API.
     */
    private interface YelpService {

        /**
         * Sends a GET request to the Yelp Fusion API to search for restaurants.
         *
         * @param term     The term or name of the restaurant to search.
         * @param location The location (latitude,longitude) or city to search within.
         * @return A {@link Call} object containing the Yelp business search results.
         */
        @GET("businesses/search")
        Call<YelpBusinessResponse> searchRestaurants(
                @Query("term") String term,
                @Query("location") String location);
    }

    /**
     * Retrieves restaurant details for a list of restaurants in bulk.
     * <p>
     * This method creates multiple API calls for each restaurant in the provided list.
     *
     * @param restaurants A list of {@link Restaurant} objects containing restaurant data (name, location).
     * @return A list of {@link Call} objects that can be executed to fetch restaurant details.
     */
    public List<Call<YelpBusinessResponse>> getBulkRestaurantDetails(List<Restaurant> restaurants) {
        List<Call<YelpBusinessResponse>> calls = new ArrayList<>();
        for (Restaurant restaurant : restaurants) {
            // Create an API call for each restaurant using its name and location
            Call<YelpBusinessResponse> call = yelpService.searchRestaurants(
                    restaurant.getName(),
                    restaurant.getLocation().getLatitude() + "," + restaurant.getLocation().getLongitude()
            );
            calls.add(call);
        }
        return calls;
    }
}