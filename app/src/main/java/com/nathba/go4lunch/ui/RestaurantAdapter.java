package com.nathba.go4lunch.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nathba.go4lunch.models.Restaurant;
import com.nathba.go4lunch.R;

/**
 * Adapter for displaying a list of restaurants in a RecyclerView.
 * Utilizes ListAdapter with DiffUtil for efficient item updates.
 */
public class RestaurantAdapter extends ListAdapter<Restaurant, RestaurantAdapter.RestaurantViewHolder> {

    /**
     * DiffUtil callback for calculating differences between old and new lists.
     */
    private static final DiffUtil.ItemCallback<Restaurant> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Restaurant>() {
                @Override
                public boolean areItemsTheSame(@NonNull Restaurant oldItem, @NonNull Restaurant newItem) {
                    // Check if items have the same unique ID
                    return oldItem.getRestaurantId().equals(newItem.getRestaurantId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Restaurant oldItem, @NonNull Restaurant newItem) {
                    // Check if the contents of the items are the same
                    return oldItem.equals(newItem);
                }
            };

    /**
     * Constructor for the RestaurantAdapter.
     * Initializes the adapter with the DiffUtil callback.
     */
    public RestaurantAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_restaurant, parent, false);
        return new RestaurantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        // Get the restaurant item at the current position and bind it to the ViewHolder
        Restaurant restaurant = getItem(position);
        holder.bind(restaurant);
    }

    /**
     * ViewHolder class to hold and bind views for each restaurant item.
     */
    static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameTextView;
        private final TextView addressTextView;
        private final ImageView photoImageView;
        private final RatingBar ratingBar;

        public RestaurantViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize view references
            nameTextView = itemView.findViewById(R.id.restaurantName);
            addressTextView = itemView.findViewById(R.id.restaurantAddress);
            photoImageView = itemView.findViewById(R.id.restaurantPhoto);
            ratingBar = itemView.findViewById(R.id.restaurantRating);
        }

        /**
         * Binds the restaurant data to the views in the ViewHolder.
         * @param restaurant The restaurant object containing the data to be displayed.
         */
        public void bind(Restaurant restaurant) {
            nameTextView.setText(restaurant.getName());
            addressTextView.setText(restaurant.getAddress());
            ratingBar.setRating((float) restaurant.getRating());

            // Load the restaurant photo using Glide
            Glide.with(photoImageView.getContext())
                    .load(restaurant.getPhotoUrl())
                    .into(photoImageView);
        }
    }
}