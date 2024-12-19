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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nathba.go4lunch.R;
import com.nathba.go4lunch.application.LunchViewModel;
import com.nathba.go4lunch.application.RestaurantViewModel;
import com.nathba.go4lunch.application.ViewModelFactory;
import com.nathba.go4lunch.application.WorkmateViewModel;
import com.nathba.go4lunch.di.AppInjector;
import com.nathba.go4lunch.models.Lunch;

import org.osmdroid.util.GeoPoint;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Fragment for displaying detailed information about a restaurant.
 * Allows users to view details, like the restaurant, and see which colleagues are joining.
 */
public class RestaurantDetailFragment extends Fragment {

    private WorkmateViewModel workmateViewModel;
    private LunchViewModel lunchViewModel;
    private RestaurantViewModel restaurantViewModel;
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
    private Button likeButton;

    private LinearLayout joiningWorkmatesList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant_detail, container, false);

        // Retrieve data from the bundle
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

        // Update the UI with restaurant details
        displayRestaurantDetails(view);

        // Initialize ViewModels
        viewModelFactory = AppInjector.getInstance().getViewModelFactory();
        workmateViewModel = new ViewModelProvider(this, viewModelFactory).get(WorkmateViewModel.class);
        lunchViewModel = new ViewModelProvider(this, viewModelFactory).get(LunchViewModel.class);
        restaurantViewModel = new ViewModelProvider(this, viewModelFactory).get(RestaurantViewModel.class);

        // Load the list of colleagues joining this restaurant
        joiningWorkmatesList = view.findViewById(R.id.joining_workmates_list);
        loadJoiningWorkmates();

        // Handle the "Add to Lunch" button click
        Button addLunchButton = view.findViewById(R.id.btn_add_lunch);
        addLunchButton.setOnClickListener(v -> addLunchToFirebase());

        // Set up the like button
        likeButton = view.findViewById(R.id.like_button);
        setupLikeButton();

        return view;
    }

    /**
     * Sets up the like button for liking/unliking the restaurant.
     */
    private void setupLikeButton() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();

        // Observe the like state
        restaurantViewModel.isRestaurantLikedByUser(userId, restaurantId).observe(getViewLifecycleOwner(), isLiked -> {
            if (isLiked != null && isLiked) {
                likeButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_green, 0, 0, 0);
            } else {
                likeButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black, 0, 0, 0);
            }
        });

        // Handle like button click
        likeButton.setOnClickListener(v -> toggleLike(userId));
    }

    /**
     * Toggles the like status for the restaurant.
     *
     * @param userId The ID of the current user.
     */
    private void toggleLike(String userId) {
        restaurantViewModel.isRestaurantLikedByUser(userId, restaurantId).observe(getViewLifecycleOwner(), isLiked -> {
            if (isLiked != null && isLiked) {
                restaurantViewModel.removeLike(userId, restaurantId);
                likeButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black, 0, 0, 0);
            } else {
                restaurantViewModel.addLike(userId, restaurantId);
                likeButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_green, 0, 0, 0);
            }
        });
    }

    /**
     * Displays the restaurant details in the UI.
     *
     * @param view The root view of the fragment.
     */
    private void displayRestaurantDetails(View view) {
        TextView restaurantNameView = view.findViewById(R.id.restaurant_name);
        TextView restaurantAddressView = view.findViewById(R.id.restaurant_address);
        ImageView restaurantImageView = view.findViewById(R.id.restaurant_image);
        RatingBar restaurantRatingBar = view.findViewById(R.id.restaurant_rating);
        Button restaurantPhoneButton = view.findViewById(R.id.restaurant_phone_button);
        Button restaurantWebsiteButton = view.findViewById(R.id.restaurant_website_button);
        TextView restaurantHoursView = view.findViewById(R.id.restaurant_hours);

        restaurantNameView.setText(restaurantName);

        // Update the restaurant address
        if (restaurantAddress != null && !restaurantAddress.isEmpty()) {
            restaurantAddressView.setText(restaurantAddress);
        } else {
            restaurantAddressView.setText(R.string.address_not_available);
        }

        // Load the restaurant image
        if (restaurantPhotoUrl != null && !restaurantPhotoUrl.isEmpty()) {
            Glide.with(view).load(restaurantPhotoUrl).into(restaurantImageView);
        } else {
            restaurantImageView.setImageResource(R.drawable.restaurant_image);  // Default image
        }

        // Update the restaurant rating
        if (restaurantRating > 0) {
            restaurantRatingBar.setRating(restaurantRating.floatValue());
        } else {
            restaurantRatingBar.setVisibility(View.GONE);
        }

        // Handle phone button
        if (restaurantPhoneNumber != null && !restaurantPhoneNumber.isEmpty()) {
            restaurantPhoneButton.setText(restaurantPhoneNumber);
            restaurantPhoneButton.setEnabled(true);
            restaurantPhoneButton.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + restaurantPhoneNumber));
                startActivity(intent);
            });
        } else {
            restaurantPhoneButton.setText(R.string.phone_not_available);
            restaurantPhoneButton.setEnabled(false);
        }

        // Handle website button
        if (restaurantWebsite != null && !restaurantWebsite.isEmpty()) {
            restaurantWebsiteButton.setText(R.string.visit_website);
            restaurantWebsiteButton.setEnabled(true);
            restaurantWebsiteButton.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(restaurantWebsite));
                startActivity(intent);
            });
        } else {
            restaurantWebsiteButton.setText(R.string.website_not_available);
            restaurantWebsiteButton.setEnabled(false);
        }

        // Update opening hours
        if (restaurantOpeningHours != null && !restaurantOpeningHours.isEmpty()) {
            restaurantHoursView.setText(restaurantOpeningHours);
        } else {
            restaurantHoursView.setText(R.string.opening_hours_not_available);
        }
    }

    /**
     * Adds the selected restaurant as the user's lunch for today.
     * <p>
     * This method first removes any expired lunches, ensures the lunch time is set to 12:00 PM,
     * deletes the user's previous lunch for the day (if any), and then adds the new lunch to the database.
     * </p>
     */
    private void addLunchToFirebase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String workmateId = currentUser.getUid();

            // Remove expired lunches before proceeding
            lunchViewModel.deleteExpiredLunches().addOnSuccessListener(aVoid -> {
                Log.d("RestaurantDetailFragment", "Expired lunches successfully deleted");

                // Set lunch time to 12:00 PM
                Calendar calendar = Calendar.getInstance();
                if (calendar.get(Calendar.HOUR_OF_DAY) >= 12) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1); // Increment day if it's past noon
                }
                calendar.set(Calendar.HOUR_OF_DAY, 12);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                Date lunchDate = calendar.getTime();

                // Delete previous lunch for the user on this date
                lunchViewModel.deleteUserLunchForDate(workmateId, lunchDate).addOnSuccessListener(aVoid2 -> {
                    Log.d("RestaurantDetailFragment", "Previous lunch deleted for user: " + workmateId);

                    // Add the new lunch
                    String lunchId = UUID.randomUUID().toString();
                    Lunch lunch = new Lunch(lunchId, workmateId, restaurantId, restaurantName, restaurantAddress, lunchDate);

                    lunchViewModel.addLunch(lunch);
                    Log.d("RestaurantDetailFragment", "New lunch added for user: " + workmateId);

                }).addOnFailureListener(e -> Log.e("RestaurantDetailFragment", "Error deleting the previous lunch", e));
            }).addOnFailureListener(e -> Log.e("RestaurantDetailFragment", "Error deleting expired lunches", e));
        }
    }

    /**
     * Loads the list of colleagues who have chosen this restaurant for lunch today
     * and updates the UI accordingly.
     */
    private void loadJoiningWorkmates() {
        // Log the restaurant ID to verify correctness
        Log.d("RestaurantDetailFragment", "Loading workmates for restaurant ID: " + restaurantId);

        // Fetch today's lunches for this restaurant from the ViewModel
        restaurantViewModel.getLunchesForRestaurantToday(restaurantId).observe(getViewLifecycleOwner(), lunches -> {
            if (lunches != null && !lunches.isEmpty()) {
                Log.d("RestaurantDetailFragment", "Found " + lunches.size() + " lunches for this restaurant.");
                displayJoiningWorkmates(lunches);
            } else {
                Log.d("RestaurantDetailFragment", "No lunches found for this restaurant.");
            }
        });
    }

    /**
     * Displays the list of colleagues who have selected this restaurant.
     * <p>
     * Each colleague's information is displayed using a custom layout.
     * The method clears any previous list of colleagues before displaying the new list.
     * </p>
     *
     * @param lunches The list of lunches associated with the restaurant.
     */
    private void displayJoiningWorkmates(List<Lunch> lunches) {
        joiningWorkmatesList.removeAllViews(); // Clear any previous list of workmates

        // Iterate through the lunches to fetch and display each workmate's details
        for (Lunch lunch : lunches) {
            Log.d("RestaurantDetailFragment", "Loading workmate for ID: " + lunch.getWorkmateId());

            // Fetch the workmate details from the ViewModel
            workmateViewModel.getWorkmateById(lunch.getWorkmateId()).observe(getViewLifecycleOwner(), workmate -> {
                if (workmate != null) {
                    Log.d("RestaurantDetailFragment", "Workmate found: " + workmate.getName());

                    // Inflate the layout for the workmate and populate it with their details
                    View workmateView = LayoutInflater.from(getContext()).inflate(R.layout.item_workmate, joiningWorkmatesList, false);

                    TextView nameTextView = workmateView.findViewById(R.id.name_text_view);
                    ImageView photoImageView = workmateView.findViewById(R.id.photo_image_view);

                    nameTextView.setText(workmate.getName());

                    // Load the workmate's photo or set a default image if unavailable
                    if (workmate.getPhotoUrl() != null) {
                        Glide.with(getContext())
                                .load(workmate.getPhotoUrl())
                                .into(photoImageView);
                    } else {
                        photoImageView.setImageResource(R.drawable.profile_picture);  // Default image
                    }

                    joiningWorkmatesList.addView(workmateView);  // Add the workmate view to the list
                } else {
                    Log.e("RestaurantDetailFragment", "Workmate not found for ID: " + lunch.getWorkmateId());
                }
            });
        }
    }
}