package com.nathba.go4lunch.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nathba.go4lunch.R;
import com.nathba.go4lunch.application.RestaurantViewModel;
import com.nathba.go4lunch.application.ViewModelFactory;
import com.nathba.go4lunch.di.AppInjector;
import com.nathba.go4lunch.models.Lunch;
import com.nathba.go4lunch.models.Restaurant;

import org.osmdroid.util.GeoPoint;

import java.util.Date;
import java.util.UUID;

public class RestaurantDetailFragment extends Fragment {

    private RestaurantViewModel restaurantViewModel;
    private ViewModelFactory viewModelFactory;

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

        // Initialiser le ViewModel
        viewModelFactory = AppInjector.getInstance().getViewModelFactory();
        restaurantViewModel = new ViewModelProvider(this, viewModelFactory).get(RestaurantViewModel.class);

        // Gérer le clic sur le bouton "Ajouter à Lunch"
        Button addLunchButton = view.findViewById(R.id.btn_add_lunch);
        addLunchButton.setOnClickListener(v -> addLunchToFirebase());

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

    private void addLunchToFirebase() {
        // Création d'un objet Lunch avec les informations du restaurant et de l'utilisateur actuel
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String lunchId = UUID.randomUUID().toString();  // Générer un ID unique pour le lunch
            String workmateId = currentUser.getUid();
            Date currentDate = new Date();  // Date actuelle

            Lunch lunch = new Lunch(lunchId, workmateId, restaurantId, currentDate);

            // Créer un objet Restaurant avec les informations actuelles
            Restaurant restaurant = new Restaurant(
                    restaurantId,
                    restaurantName,
                    restaurantAddress,
                    restaurantPhotoUrl,
                    restaurantRating,
                    restaurantLocation
            );

            // Ajouter le lunch et le restaurant dans Firebase via le ViewModel
            restaurantViewModel.addLunch(lunch, restaurant);

            // Afficher un message à l'utilisateur pour indiquer que l'ajout a été effectué
            Toast.makeText(getContext(), "Lunch ajouté avec succès", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
        }
    }
}