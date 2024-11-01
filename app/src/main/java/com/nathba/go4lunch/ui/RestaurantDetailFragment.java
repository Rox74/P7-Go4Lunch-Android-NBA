package com.nathba.go4lunch.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.nathba.go4lunch.application.WorkmateViewModel;
import com.nathba.go4lunch.di.AppInjector;
import com.nathba.go4lunch.models.Lunch;
import com.nathba.go4lunch.models.Restaurant;
import com.nathba.go4lunch.models.Workmate;
import com.nathba.go4lunch.notification.NotificationScheduler;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class RestaurantDetailFragment extends Fragment {

    private RestaurantViewModel restaurantViewModel;
    private WorkmateViewModel workmateViewModel;
    private ViewModelFactory viewModelFactory;

    private String restaurantId;
    private String restaurantName;
    private String restaurantAddress;
    private String restaurantPhotoUrl;
    private String restaurantPhoneNumber;
    private String restaurantWebsite;
    private String restaurantOpeningHours;
    private Double restaurantRating;
    private GeoPoint restaurantLocation;


    private LinearLayout joiningWorkmatesList;

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
            restaurantPhoneNumber = getArguments().getString("restaurantPhoneNumber");
            restaurantWebsite = getArguments().getString("restaurantWebsite");
            restaurantOpeningHours = getArguments().getString("restaurantOpeningHours");

            double latitude = getArguments().getDouble("latitude");
            double longitude = getArguments().getDouble("longitude");
            restaurantLocation = new GeoPoint(latitude, longitude);
        }

        // Mettre à jour l'UI avec les infos du restaurant
        displayRestaurantDetails(view);

        // Initialiser le ViewModel
        viewModelFactory = AppInjector.getInstance().getViewModelFactory();
        restaurantViewModel = new ViewModelProvider(this, viewModelFactory).get(RestaurantViewModel.class);
        workmateViewModel = new ViewModelProvider(this, viewModelFactory).get(WorkmateViewModel.class);

        // Récupérer la liste des workmates qui rejoignent ce restaurant
        joiningWorkmatesList = view.findViewById(R.id.joining_workmates_list);
        loadJoiningWorkmates();

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

        // Boutons pour le téléphone et le site web
        Button restaurantPhoneButton = view.findViewById(R.id.restaurant_phone_button);
        Button restaurantWebsiteButton = view.findViewById(R.id.restaurant_website_button);

        // TextView pour les horaires d'ouverture
        TextView restaurantHoursView = view.findViewById(R.id.restaurant_hours);

        restaurantNameView.setText(restaurantName);

        // Mise à jour de l'adresse
        if (restaurantAddress != null && !restaurantAddress.isEmpty()) {
            restaurantAddressView.setText(restaurantAddress);
        } else {
            restaurantAddressView.setText("Address not available");
        }

        // Mise à jour de l'image
        if (restaurantPhotoUrl != null && !restaurantPhotoUrl.isEmpty()) {
            Glide.with(view).load(restaurantPhotoUrl).into(restaurantImageView);
        } else {
            restaurantImageView.setImageResource(R.drawable.restaurant_image);  // Image par défaut
        }

        // Mise à jour de la note
        if (restaurantRating > 0) {
            restaurantRatingBar.setRating(restaurantRating.floatValue());
        } else {
            restaurantRatingBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Rating not available", Toast.LENGTH_SHORT).show();
        }

        // Mise à jour du bouton téléphone
        if (restaurantPhoneNumber != null && !restaurantPhoneNumber.isEmpty()) {
            restaurantPhoneButton.setText(restaurantPhoneNumber);
            restaurantPhoneButton.setEnabled(true);
            restaurantPhoneButton.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + restaurantPhoneNumber));
                startActivity(intent);
            });
        } else {
            restaurantPhoneButton.setText("Phone not available");
            restaurantPhoneButton.setEnabled(false);
        }

        // Mise à jour du bouton site web
        if (restaurantWebsite != null && !restaurantWebsite.isEmpty()) {
            restaurantWebsiteButton.setText("Visit Website");
            restaurantWebsiteButton.setEnabled(true);
            restaurantWebsiteButton.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(restaurantWebsite));
                startActivity(intent);
            });
        } else {
            restaurantWebsiteButton.setText("Website not available");
            restaurantWebsiteButton.setEnabled(false);
        }

        // Mise à jour des horaires d'ouverture
        if (restaurantOpeningHours != null && !restaurantOpeningHours.isEmpty()) {
            restaurantHoursView.setText(restaurantOpeningHours);
        } else {
            restaurantHoursView.setText("Opening hours not available"); // Message d'indisponibilité
        }
    }

    private void addLunchToFirebase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String lunchId = UUID.randomUUID().toString();  // Générer un ID unique pour le lunch
            String workmateId = currentUser.getUid();
            Date currentDate = new Date();  // Date actuelle

            Lunch lunch = new Lunch(lunchId, workmateId, restaurantId, currentDate);

            // Ajouter le lunch dans Firebase via LunchRepository
            restaurantViewModel.addLunch(lunch);
            Log.d("RestaurantDetailFragment", "Lunch ajouté pour l'utilisateur : " + workmateId);

            // Ajouter le restaurant dans Firebase via RestaurantRepository
            Restaurant restaurant = new Restaurant(
                    restaurantId,
                    restaurantName,
                    restaurantAddress,
                    restaurantPhotoUrl,
                    restaurantRating != null ? restaurantRating : 0.0,
                    restaurantLocation,
                    restaurantPhoneNumber != null ? restaurantPhoneNumber : "",
                    restaurantWebsite != null ? restaurantWebsite : "",
                    restaurantOpeningHours != null ? restaurantOpeningHours : "",
                    new ArrayList<>()
            );
            restaurantViewModel.addRestaurant(restaurant);
            Log.d("RestaurantDetailFragment", "Restaurant ajouté : " + restaurantName);

            // Récupérer la liste des collègues rejoignant ce restaurant
            restaurantViewModel.getLunchesForRestaurantToday(restaurantId).observe(getViewLifecycleOwner(), lunches -> {
                List<String> colleaguesNames = new ArrayList<>();

                // Boucle pour chaque lunch pour récupérer les noms de collègues
                for (Lunch colleagueLunch : lunches) {
                    workmateViewModel.getWorkmateById(colleagueLunch.getWorkmateId()).observe(getViewLifecycleOwner(), workmate -> {
                        if (workmate != null) {
                            colleaguesNames.add(workmate.getName());
                            Log.d("RestaurantDetailFragment", "Collègue ajouté à la liste : " + workmate.getName());

                            // Vérifie si tous les collègues sont ajoutés avant d'envoyer la notification
                            if (colleaguesNames.size() == lunches.size()) {
                                Log.d("RestaurantDetailFragment", "Tous les collègues récupérés. Envoi de la notification.");
                                NotificationScheduler.sendImmediateNotification(requireContext(), restaurantName, restaurantAddress, colleaguesNames);
                            }
                        } else {
                            Log.e("RestaurantDetailFragment", "Impossible de récupérer les informations pour le collègue avec ID : " + colleagueLunch.getWorkmateId());
                        }
                    });
                }
            });

            Toast.makeText(getContext(), "Lunch et restaurant ajoutés avec succès", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadJoiningWorkmates() {
        // Log le restaurant ID pour vérifier qu'il est correct
        Log.d("RestaurantDetailFragment", "Loading workmates for restaurant ID: " + restaurantId);

        // Appel au ViewModel pour récupérer les lunchs du jour pour ce restaurant
        restaurantViewModel.getLunchesForRestaurantToday(restaurantId).observe(getViewLifecycleOwner(), lunches -> {
            if (lunches != null && !lunches.isEmpty()) {
                Log.d("RestaurantDetailFragment", "Found " + lunches.size() + " lunches for this restaurant.");
                displayJoiningWorkmates(lunches);
            } else {
                Log.d("RestaurantDetailFragment", "No lunches found for this restaurant.");
                Toast.makeText(getContext(), "Aucun collègue n'a choisi ce restaurant", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayJoiningWorkmates(List<Lunch> lunches) {
        joiningWorkmatesList.removeAllViews(); // Nettoyer la liste précédente

        for (Lunch lunch : lunches) {
            Log.d("RestaurantDetailFragment", "Loading workmate for ID: " + lunch.getWorkmateId());
            // Récupérer les détails du workmate depuis le Lunch (via son ID)
            workmateViewModel.getWorkmateById(lunch.getWorkmateId()).observe(getViewLifecycleOwner(), workmate -> {
                if (workmate != null) {
                    Log.d("RestaurantDetailFragment", "Workmate found: " + workmate.getName());
                    // Créer une vue pour chaque workmate et l'ajouter à la liste
                    View workmateView = LayoutInflater.from(getContext()).inflate(R.layout.item_workmate, joiningWorkmatesList, false);

                    TextView nameTextView = workmateView.findViewById(R.id.name_text_view);
                    ImageView photoImageView = workmateView.findViewById(R.id.photo_image_view);

                    nameTextView.setText(workmate.getName());
                    if (workmate.getPhotoUrl() != null) {
                        Glide.with(getContext())
                                .load(workmate.getPhotoUrl())
                                .into(photoImageView);
                    } else {
                        photoImageView.setImageResource(R.drawable.profile_picture);  // Image par défaut
                    }

                    joiningWorkmatesList.addView(workmateView);  // Ajouter la vue du workmate à la liste
                } else {
                    Log.e("RestaurantDetailFragment", "Workmate not found for ID: " + lunch.getWorkmateId());
                }
            });
        }
    }
}