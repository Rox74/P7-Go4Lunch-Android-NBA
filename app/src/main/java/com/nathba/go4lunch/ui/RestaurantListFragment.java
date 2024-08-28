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

/**
 * A Fragment that displays a list of restaurants using a RecyclerView.
 * The list is provided by the RestaurantViewModel and displayed with the RestaurantAdapter.
 */
public class RestaurantListFragment extends Fragment {

    private RestaurantViewModel restaurantViewModel;
    private RecyclerView recyclerView;
    private RestaurantAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_restaurant_list, container, false);

        // Initialize the RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RestaurantAdapter();
        recyclerView.setAdapter(adapter);

        // Initialize the ViewModel
        restaurantViewModel = new ViewModelProvider(this).get(RestaurantViewModel.class);

        // Observe the restaurant data from the ViewModel
        restaurantViewModel.getRestaurants().observe(getViewLifecycleOwner(), restaurants -> {
            // Update the adapter with the new list of restaurants
            adapter.submitList(restaurants);
        });

        return view;
    }
}