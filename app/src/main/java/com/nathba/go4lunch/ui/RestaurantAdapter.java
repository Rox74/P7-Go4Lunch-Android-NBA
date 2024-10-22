package com.nathba.go4lunch.ui;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nathba.go4lunch.application.RestaurantViewModel;
import com.nathba.go4lunch.models.Restaurant;
import com.nathba.go4lunch.R;

public class RestaurantAdapter extends ListAdapter<Restaurant, RestaurantAdapter.RestaurantViewHolder> {

    private final RestaurantViewModel restaurantViewModel;

    /**
     * Constructor for the RestaurantAdapter.
     * Initializes the adapter with the DiffUtil callback and ViewModel.
     */
    public RestaurantAdapter(RestaurantViewModel restaurantViewModel) {
        super(DIFF_CALLBACK);
        this.restaurantViewModel = restaurantViewModel;
    }

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

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_restaurant, parent, false);
        return new RestaurantViewHolder(view, restaurantViewModel);
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
        private final TextView lunchCountTextView; // TextView pour le nombre de lunchs
        private final RestaurantViewModel restaurantViewModel;

        public RestaurantViewHolder(@NonNull View itemView, RestaurantViewModel restaurantViewModel) {
            super(itemView);
            this.restaurantViewModel = restaurantViewModel;

            // Initialize view references
            nameTextView = itemView.findViewById(R.id.restaurantName);
            addressTextView = itemView.findViewById(R.id.restaurantAddress);
            photoImageView = itemView.findViewById(R.id.restaurantPhoto);
            ratingBar = itemView.findViewById(R.id.restaurantRating);
            lunchCountTextView = itemView.findViewById(R.id.lunchCount); // Assurez-vous que cet ID est dans le layout XML
        }

        public void bind(Restaurant restaurant) {
            nameTextView.setText(restaurant.getName());
            addressTextView.setText(restaurant.getAddress());
            ratingBar.setRating((float) restaurant.getRating());

            // Load the restaurant photo using Glide
            Glide.with(photoImageView.getContext())
                    .load(restaurant.getPhotoUrl())
                    .into(photoImageView);

            // Observer le nombre de lunchs pour ce restaurant
            if (restaurantViewModel != null) {
                restaurantViewModel.getLunchCountForRestaurant(restaurant.getRestaurantId()).observe((LifecycleOwner) itemView.getContext(), count -> {
                    lunchCountTextView.setText(itemView.getContext().getString(R.string.lunch_count, count)); // Met à jour le TextView
                });
            } else {
                // Log ou gérer le cas où le ViewModel est null
                Log.e("RestaurantAdapter", "RestaurantViewModel is null");
            }
        }
    }
}