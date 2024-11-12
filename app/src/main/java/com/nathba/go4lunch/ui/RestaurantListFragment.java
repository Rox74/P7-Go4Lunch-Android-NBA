package com.nathba.go4lunch.ui;

import android.os.Bundle;
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
import com.nathba.go4lunch.application.RestaurantViewModel;
import com.nathba.go4lunch.application.ViewModelFactory;
import com.nathba.go4lunch.di.AppInjector;
import com.nathba.go4lunch.models.Restaurant;

import java.util.ArrayList;
import java.util.List;

public class RestaurantListFragment extends Fragment implements Searchable {

    private RestaurantViewModel restaurantViewModel;
    private RecyclerView recyclerView;
    private RestaurantAdapter adapter;

    private List<Restaurant> fullRestaurantList = new ArrayList<>();
    private List<Restaurant> filteredRestaurantList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant_list, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);

        ViewModelFactory viewModelFactory = AppInjector.getInstance().getViewModelFactory();
        restaurantViewModel = new ViewModelProvider(this, viewModelFactory).get(RestaurantViewModel.class);

        // Passer le ViewModel à l'adaptateur
        adapter = new RestaurantAdapter(restaurantViewModel);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        observeDetailedRestaurants();

        return view;
    }

    private void observeDetailedRestaurants() {
        restaurantViewModel.getDetailedRestaurants().observe(getViewLifecycleOwner(), detailedRestaurants -> {
            if (detailedRestaurants != null) {
                fullRestaurantList.clear();
                fullRestaurantList.addAll(detailedRestaurants);

                filteredRestaurantList.clear();
                filteredRestaurantList.addAll(detailedRestaurants);

                adapter.submitList(filteredRestaurantList); // Mettre à jour l'adaptateur
            }
        });
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

        adapter.submitList(filteredRestaurantList); // Mettre à jour l'affichage
    }
}