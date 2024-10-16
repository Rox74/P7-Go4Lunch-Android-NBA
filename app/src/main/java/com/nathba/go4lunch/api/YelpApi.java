package com.nathba.go4lunch.api;

import com.nathba.go4lunch.models.YelpBusinessResponse;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class YelpApi {

    private static final String BASE_URL = "https://api.yelp.com/v3/";
    private static final String API_KEY = "0d2PG33ejgOrbat4Vgz0zciSp-v5qzJVd9rrawP_FF2e4aUeb1jFJheNwE5rga_QV5oIEydSQoiUkmQnIMGb1qXCGdMDxw2t7OCGOJNcMNEGnj6E2F0REBxIRaQPZ3Yx";  // Remplace par ta clé API Yelp

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
}