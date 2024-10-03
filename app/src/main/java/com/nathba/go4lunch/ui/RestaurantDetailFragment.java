package com.nathba.go4lunch.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.nathba.go4lunch.R;

import org.osmdroid.util.GeoPoint;

public class RestaurantDetailFragment extends Fragment {

    private String restaurantId;
    private String restaurantName;
    private String restaurantAddress;
    private String restaurantPhotoUrl;
    private Double restaurantRating;
    private GeoPoint restaurantLocation;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant_detail, container, false);

        // Récupérer les données passées via le bundle
        if (getArguments() != null) {
            restaurantId = getArguments().getString("restaurantId");
            restaurantName = getArguments().getString("restaurantName");
            restaurantAddress = getArguments().getString("restaurantAddress");
            restaurantPhotoUrl = getArguments().getString("restaurantPhotoUrl");
            restaurantRating = getArguments().getDouble("restaurantRating");
            double latitude = getArguments().getDouble("latitude");
            double longitude = getArguments().getDouble("longitude");
            restaurantLocation = new GeoPoint(latitude, longitude);
        }

        // Mettre à jour l'UI avec les infos du restaurant
        displayRestaurantDetails(view);

        return view;
    }

    private void displayRestaurantDetails(View view) {
        TextView restaurantNameView = view.findViewById(R.id.restaurant_name);
        TextView restaurantAddressView = view.findViewById(R.id.restaurant_address);
        ImageView restaurantImageView = view.findViewById(R.id.restaurant_image);
        RatingBar restaurantRatingBar = view.findViewById(R.id.restaurant_rating);

        restaurantNameView.setText(restaurantName);

        // Si l'adresse est disponible, l'afficher, sinon montrer un message
        if (restaurantAddress != null && !restaurantAddress.isEmpty()) {
            restaurantAddressView.setText(restaurantAddress);
        } else {
            restaurantAddressView.setText("Address not available");
        }

        // Si l'image est disponible, la charger avec Glide
        if (restaurantPhotoUrl != null && !restaurantPhotoUrl.isEmpty()) {
            Glide.with(view)
                    .load(restaurantPhotoUrl)
                    .into(restaurantImageView);
        } else {
            restaurantImageView.setImageResource(R.drawable.restaurant_image);  // Image par défaut si indisponible
        }

        // Mettre à jour la note, ou masquer si elle n'est pas disponible
        if (restaurantRating > 0) {
            restaurantRatingBar.setRating(restaurantRating.floatValue());
        } else {
            restaurantRatingBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Rating not available", Toast.LENGTH_SHORT).show();
        }
    }
}