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

public class RestaurantListFragment extends Fragment {

    private RestaurantViewModel restaurantViewModel;
    private RecyclerView recyclerView;
    private RestaurantAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant_list, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);

        // Obtenir le ViewModel
        ViewModelFactory viewModelFactory = AppInjector.getInstance().getViewModelFactory();
        restaurantViewModel = new ViewModelProvider(this, viewModelFactory).get(RestaurantViewModel.class);

        // Configurer l'adapter avec le ViewModel
        adapter = new RestaurantAdapter(restaurantViewModel);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Observer les changements dans les restaurants détaillés
        observeDetailedRestaurants();

        return view;
    }

    private void observeDetailedRestaurants() {
        // Observer directement la liste des restaurants détaillés
        restaurantViewModel.getDetailedRestaurants().observe(getViewLifecycleOwner(), detailedRestaurants -> {
            if (detailedRestaurants != null && !detailedRestaurants.isEmpty()) {
                // Mettre à jour l'adaptateur avec les restaurants détaillés
                adapter.submitList(detailedRestaurants);
            }
        });
    }
}