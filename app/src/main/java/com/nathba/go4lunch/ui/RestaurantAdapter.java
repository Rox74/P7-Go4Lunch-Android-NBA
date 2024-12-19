package com.nathba.go4lunch.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nathba.go4lunch.application.RestaurantViewModel;
import com.nathba.go4lunch.models.Restaurant;
import com.nathba.go4lunch.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying a list of restaurants in a RecyclerView.
 * Uses the {@link RestaurantViewModel} to observe restaurant-related data and handle interactions.
 */
public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder> {

    private final List<Restaurant> restaurantList = new ArrayList<>();
    private final RestaurantViewModel restaurantViewModel;

    /**
     * Constructor for the RestaurantAdapter.
     *
     * @param restaurantViewModel The ViewModel used to observe restaurant-related data.
     */
    public RestaurantAdapter(RestaurantViewModel restaurantViewModel) {
        this.restaurantViewModel = restaurantViewModel;
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_restaurant, parent, false);
        return new RestaurantViewHolder(view, restaurantViewModel);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        Restaurant restaurant = restaurantList.get(position);
        holder.bind(restaurant);
    }

    @Override
    public int getItemCount() {
        return restaurantList.size();
    }

    /**
     * Updates the list of restaurants and refreshes the RecyclerView.
     *
     * @param newRestaurants The new list of restaurants to display.
     */
    public void submitList(List<Restaurant> newRestaurants) {
        restaurantList.clear();
        if (newRestaurants != null) {
            restaurantList.addAll(newRestaurants);
        }
        notifyDataSetChanged(); // Refresh the display
    }

    /**
     * ViewHolder class for displaying individual restaurant items in the RecyclerView.
     */
    static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameTextView;
        private final TextView addressTextView;
        private final ImageView photoImageView;
        private final RatingBar ratingBar;
        private final TextView lunchCountTextView;
        private final RestaurantViewModel restaurantViewModel;

        /**
         * Constructor for the RestaurantViewHolder.
         *
         * @param itemView The view representing the individual restaurant item.
         * @param restaurantViewModel The ViewModel used to observe restaurant-related data.
         */
        public RestaurantViewHolder(@NonNull View itemView, RestaurantViewModel restaurantViewModel) {
            super(itemView);
            this.restaurantViewModel = restaurantViewModel;

            nameTextView = itemView.findViewById(R.id.restaurantName);
            addressTextView = itemView.findViewById(R.id.restaurantAddress);
            photoImageView = itemView.findViewById(R.id.restaurantPhoto);
            ratingBar = itemView.findViewById(R.id.restaurantRating);
            lunchCountTextView = itemView.findViewById(R.id.lunchCount);
        }

        /**
         * Binds a restaurant's data to the ViewHolder.
         * Updates the UI with the restaurant's details and observes lunch count for the restaurant.
         *
         * @param restaurant The restaurant to display.
         */
        public void bind(Restaurant restaurant) {
            nameTextView.setText(restaurant.getName());
            addressTextView.setText(restaurant.getAddress());
            ratingBar.setRating((float) restaurant.getRating());

            Glide.with(photoImageView.getContext())
                    .load(restaurant.getPhotoUrl())
                    .into(photoImageView);

            if (restaurantViewModel != null) {
                // Observe lunch data to display the count of participants
                restaurantViewModel.getLunchesForRestaurantToday(restaurant.getRestaurantId())
                        .observe((LifecycleOwner) itemView.getContext(), lunches -> {
                            int count = lunches != null ? lunches.size() : 0;
                            lunchCountTextView.setText(itemView.getContext().getString(R.string.lunch_count, count));
                        });
            }

            // Set an OnClickListener to open the restaurant details
            itemView.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString("restaurantId", restaurant.getRestaurantId());
                bundle.putString("restaurantName", restaurant.getName());
                bundle.putString("restaurantAddress", restaurant.getAddress());
                bundle.putString("restaurantPhotoUrl", restaurant.getPhotoUrl());
                bundle.putDouble("restaurantRating", restaurant.getRating());
                bundle.putDouble("latitude", restaurant.getLocation().getLatitude());
                bundle.putDouble("longitude", restaurant.getLocation().getLongitude());
                bundle.putString("restaurantPhoneNumber", restaurant.getPhoneNumber());
                bundle.putString("restaurantWebsite", restaurant.getYelpUrl());

                RestaurantDetailFragment fragment = new RestaurantDetailFragment();
                fragment.setArguments(bundle);

                ((AppCompatActivity) v.getContext()).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            });
        }
    }
}