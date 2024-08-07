package com.nathba.go4lunch.api;

import com.nathba.go4lunch.models.OverpassResponse;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class OverpassApi {

    private static final String BASE_URL = "https://overpass-api.de/api/";

    private final OverpassService service;

    public OverpassApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(OverpassService.class);
    }

    public Call<OverpassResponse> getRestaurants(String query) {
        return service.getRestaurants(query);
    }

    private interface OverpassService {
        @GET("interpreter")
        Call<OverpassResponse> getRestaurants(@Query("data") String query);
    }
}