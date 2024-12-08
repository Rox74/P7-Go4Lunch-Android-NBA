package com.nathba.go4lunch.ui;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nathba.go4lunch.R;
import com.nathba.go4lunch.application.MapViewModel;
import com.nathba.go4lunch.application.RestaurantViewModel;
import com.nathba.go4lunch.application.ViewModelFactory;
import com.nathba.go4lunch.di.AppInjector;
import com.nathba.go4lunch.models.Restaurant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RestaurantListFragment extends Fragment implements Searchable {

    private RestaurantViewModel restaurantViewModel;
    private MapViewModel mapViewModel; // Le ViewModel pour la localisation
    private RecyclerView recyclerView;
    private RestaurantAdapter adapter;

    private List<Restaurant> fullRestaurantList = new ArrayList<>();
    private List<Restaurant> filteredRestaurantList = new ArrayList<>();
    private Location userLocation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant_list, container, false);

        // Utilisez le ViewModelFactory pour instancier MapViewModel et RestaurantViewModel
        ViewModelFactory viewModelFactory = AppInjector.getInstance().getViewModelFactory();
        mapViewModel = new ViewModelProvider(this, viewModelFactory).get(MapViewModel.class);
        restaurantViewModel = new ViewModelProvider(this, viewModelFactory).get(RestaurantViewModel.class);

        // Initialisation de la RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        adapter = new RestaurantAdapter(restaurantViewModel);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Observer les données
        observeDetailedRestaurants();
        observeUserLocation();

        return view;
    }

    private void observeDetailedRestaurants() {
        if (restaurantViewModel != null) {
            restaurantViewModel.getDetailedRestaurants().observe(getViewLifecycleOwner(), detailedRestaurants -> {
                if (detailedRestaurants != null) {
                    fullRestaurantList.clear();
                    fullRestaurantList.addAll(detailedRestaurants);

                    filteredRestaurantList.clear();
                    filteredRestaurantList.addAll(detailedRestaurants);

                    adapter.submitList(filteredRestaurantList);
                } else {
                    Log.e("RestaurantListFragment", "No detailed restaurants available");
                }
            });
        } else {
            Log.e("RestaurantListFragment", "RestaurantViewModel is null");
        }
    }

    private void observeUserLocation() {
        if (mapViewModel != null) {
            mapViewModel.getUserLocation().observe(getViewLifecycleOwner(), location -> {
                if (location != null) {
                    userLocation = location; // Mettre à jour la localisation de l'utilisateur
                } else {
                    Log.e("RestaurantListFragment", "User location is null");
                }
            });
        } else {
            Log.e("RestaurantListFragment", "MapViewModel is null");
        }
    }

    @Override
    public void onSearch(String query) {
        filteredRestaurantList.clear();

        if (query == null || query.isEmpty()) {
            filteredRestaurantList.addAll(fullRestaurantList);
        } else {
            for (Restaurant restaurant : fullRestaurantList) {
                if (restaurant.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredRestaurantList.add(restaurant);
                }
            }
        }

        adapter.submitList(filteredRestaurantList);
    }

    @Override
    public void onSort(String criterion) {
        switch (criterion) {
            case "distance":
                if (userLocation != null) {
                    // Tri par distance
                    Collections.sort(filteredRestaurantList, new Comparator<Restaurant>() {
                        @Override
                        public int compare(Restaurant r1, Restaurant r2) {
                            float[] result1 = new float[1];
                            float[] result2 = new float[1];

                            Location.distanceBetween(
                                    userLocation.getLatitude(), userLocation.getLongitude(),
                                    r1.getLocation().getLatitude(), r1.getLocation().getLongitude(),
                                    result1
                            );

                            Location.distanceBetween(
                                    userLocation.getLatitude(), userLocation.getLongitude(),
                                    r2.getLocation().getLatitude(), r2.getLocation().getLongitude(),
                                    result2
                            );

                            return Float.compare(result1[0], result2[0]); // Tri par distance croissante
                        }
                    });
                }
                break;
            case "stars":
                // Tri par nombre d'étoiles (rating) décroissant
                Collections.sort(filteredRestaurantList, new Comparator<Restaurant>() {
                    @Override
                    public int compare(Restaurant r1, Restaurant r2) {
                        return Double.compare(r2.getRating(), r1.getRating());
                    }
                });
                break;
            case "a_to_z":
                // Tri par ordre alphabétique croissant (A-Z)
                Collections.sort(filteredRestaurantList, new Comparator<Restaurant>() {
                    @Override
                    public int compare(Restaurant r1, Restaurant r2) {
                        return r1.getName().compareTo(r2.getName());
                    }
                });
                break;
            case "z_to_a":
                // Tri par ordre alphabétique décroissant (Z-A)
                Collections.sort(filteredRestaurantList, new Comparator<Restaurant>() {
                    @Override
                    public int compare(Restaurant r1, Restaurant r2) {
                        return r2.getName().compareTo(r1.getName());
                    }
                });
                break;
        }

        adapter.submitList(new ArrayList<>(filteredRestaurantList)); // Rafraîchir l'affichage
    }
}