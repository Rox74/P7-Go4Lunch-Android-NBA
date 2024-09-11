package com.nathba.go4lunch.api;

import static android.provider.Settings.System.getString;

import com.nathba.go4lunch.R;
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
    private static final String API_KEY = "Lc4xrr7yphZ7lumiau0zyezlk2GkZET0I6qgChcqqFH61b--sEsI1-6kK3LayDGTlJVy9ujYMqKw0K3C2N5wtJKfKEdKsZVJyNX1DjJCg1no68r29B_gjpUNzrjZZnYx";  // Remplace par ta clé API Yelp

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