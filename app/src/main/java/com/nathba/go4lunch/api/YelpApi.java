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

public class YelpApi {

    private static final String BASE_URL = "https://api.yelp.com/v3/";
    private static final String API_KEY = "KxudpBfyyIQppNyBd0j1QhavtEucbImVrw4pr2YL24PJsH_sA7Svew7kHYAqyzNdRivrnb0uSBoDtIMFwOZEiugAJ09aAI8EvNLQJiphvXp57g70xbOQhtnd9XlhZ3Yx";

    private final YelpService yelpService;

    public YelpApi() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request request = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer " + API_KEY)
                            .build();
                    return chain.proceed(request);
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        yelpService = retrofit.create(YelpService.class);
    }

    public Call<YelpBusinessResponse> getRestaurantDetails(String restaurantName, String location) {
        return yelpService.searchRestaurants(restaurantName, location);
    }

    private interface YelpService {
        @GET("businesses/search")
        Call<YelpBusinessResponse> searchRestaurants(
                @Query("term") String term,
                @Query("location") String location);
    }

    // Récupérer les détails en bulk
    public List<Call<YelpBusinessResponse>> getBulkRestaurantDetails(List<Restaurant> restaurants) {
        List<Call<YelpBusinessResponse>> calls = new ArrayList<>();
        for (Restaurant restaurant : restaurants) {
            Call<YelpBusinessResponse> call = yelpService.searchRestaurants(
                    restaurant.getName(),
                    restaurant.getLocation().getLatitude() + "," + restaurant.getLocation().getLongitude()
            );
            calls.add(call);
        }
        return calls;
    }
}