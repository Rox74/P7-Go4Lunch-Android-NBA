package com.nathba.go4lunch.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.nathba.go4lunch.R;
import com.nathba.go4lunch.application.RestaurantViewModel;
import com.nathba.go4lunch.models.Lunch;
import com.nathba.go4lunch.models.Restaurant;
import com.nathba.go4lunch.repository.RepositoryCallback;
import com.nathba.go4lunch.repository.RestaurantRepository;

import org.osmdroid.util.GeoPoint;

import java.util.Date;
import java.util.UUID;

public class RestaurantDetailFragment extends Fragment {

    private RestaurantRepository restaurantRepository;
    private Restaurant restaurant;
    private String restaurantId;
    private String restaurantName;
    private GeoPoint restaurantLocation;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant_detail, container, false);

        // Récupérer les données passées via le bundle
        if (getArguments() != null) {
            restaurantId = getArguments().getString("restaurantId");
            restaurantName = getArguments().getString("restaurantName");
            double latitude = getArguments().getDouble("latitude");
            double longitude = getArguments().getDouble("longitude");
            restaurantLocation = new GeoPoint(latitude, longitude);  // Créer l'objet GeoPoint avec les coordonnées
        }

        // Appeler getRestaurantDetails avec les bonnes informations
        RestaurantRepository restaurantRepository = new RestaurantRepository();
        restaurantRepository.getRestaurantDetails(restaurantId, restaurantLocation, restaurantName, new RepositoryCallback<Restaurant>() {
            @Override
            public void onSuccess(Restaurant restaurant) {
                // Mise à jour de l'UI avec les détails du restaurant
                updateUI(restaurant);
            }

            @Override
            public void onError(Throwable t) {
                // Gérer les erreurs (afficher un message d'erreur à l'utilisateur par exemple)
            }
        });

        return view;
    }

    private void updateUI(Restaurant restaurant) {
        // TODO : Méthode pour mettre à jour l'interface utilisateur avec les détails du restaurant
    }

    private void displayRestaurantDetails(View view) {
        if (restaurant == null) {
            Log.e("RestaurantDetailFragment", "Restaurant is null, cannot display details.");
            return;
        }

        TextView restaurantName = view.findViewById(R.id.restaurant_name);
        TextView restaurantAddress = view.findViewById(R.id.restaurant_address);
        ImageView restaurantImage = view.findViewById(R.id.restaurant_image);
        RatingBar restaurantRating = view.findViewById(R.id.restaurant_rating);

        restaurantName.setText(restaurant.getName());
        restaurantAddress.setText(restaurant.getAddress());
        restaurantRating.setRating((float) restaurant.getRating());

        // Charger l'image avec Glide
        Glide.with(view)
                .load(restaurant.getPhotoUrl())
                .into(restaurantImage);
    }
}