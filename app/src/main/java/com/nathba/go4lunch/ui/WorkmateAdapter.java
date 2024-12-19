package com.nathba.go4lunch.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.nathba.go4lunch.R;
import com.nathba.go4lunch.models.Lunch;
import com.nathba.go4lunch.models.Workmate;

import java.util.List;

/**
 * Adapter class for displaying a list of workmates in a RecyclerView.
 * Handles creating and binding view holders for each workmate item.
 */
public class WorkmateAdapter extends RecyclerView.Adapter<WorkmateAdapter.WorkmateViewHolder> {

    private List<Workmate> workmates;
    private List<Lunch> lunches;

    /**
     * Constructor for initializing the adapter with a list of workmates and their lunches.
     *
     * @param workmates List of workmates to display.
     * @param lunches   List of lunches associated with the workmates.
     */
    public WorkmateAdapter(List<Workmate> workmates, List<Lunch> lunches) {
        this.workmates = workmates;
        this.lunches = lunches;
    }

    @NonNull
    @Override
    public WorkmateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workmate, parent, false);
        return new WorkmateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkmateViewHolder holder, int position) {
        Workmate workmate = workmates.get(position);
        holder.bind(workmate, lunches);
    }

    @Override
    public int getItemCount() {
        return workmates.size();
    }

    /**
     * Updates the list of workmates and refreshes the adapter.
     *
     * @param newWorkmates Updated list of workmates.
     */
    public void updateWorkmates(List<Workmate> newWorkmates) {
        this.workmates = newWorkmates;
        notifyDataSetChanged();
    }

    /**
     * Updates the list of lunches and refreshes the adapter.
     *
     * @param newLunches Updated list of lunches.
     */
    public void updateLunches(List<Lunch> newLunches) {
        this.lunches = newLunches;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class for binding workmate data to the corresponding UI components.
     */
    static class WorkmateViewHolder extends RecyclerView.ViewHolder {

        private TextView nameTextView;
        private ImageView photoImageView;

        /**
         * Constructor for initializing the ViewHolder with item views.
         *
         * @param itemView The layout view for a single workmate item.
         */
        public WorkmateViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            photoImageView = itemView.findViewById(R.id.photo_image_view);
        }

        /**
         * Binds the workmate and lunch data to the UI components.
         *
         * @param workmate The workmate data to display.
         * @param lunches  The list of lunches to determine the restaurant selection.
         */
        public void bind(Workmate workmate, List<Lunch> lunches) {
            Context context = itemView.getContext(); // Context for accessing resources
            String text;

            // Default text indicating no restaurant has been selected
            text = context.getString(R.string.no_restaurant_selected, workmate.getName());

            // Check if the workmate has a lunch associated
            if (lunches != null) {
                for (Lunch lunch : lunches) {
                    if (lunch.getWorkmateId().equals(workmate.getWorkmateId())) {
                        text = context.getString(R.string.is_eating_at, workmate.getName(), lunch.getRestaurantName());
                        itemView.setOnClickListener(v -> navigateToRestaurantDetail(context, lunch));
                        break;
                    }
                }
            } else {
                itemView.setOnClickListener(null); // Disable click if no restaurant
            }

            // Set the final text for the nameTextView
            nameTextView.setText(text);

            // Always handle profile image, regardless of lunch
            if (workmate.getPhotoUrl() != null && !workmate.getPhotoUrl().isEmpty()) {
                Glide.with(photoImageView.getContext())
                        .load(workmate.getPhotoUrl())
                        .placeholder(R.drawable.profile_picture) // Placeholder while loading
                        .error(R.drawable.profile_picture)       // Default image on error
                        .transform(new CircleCrop()) // Round image transformation
                        .into(photoImageView);
            } else {
                photoImageView.setImageResource(R.drawable.profile_picture); // Default image if no URL
            }
        }

        /**
         * Navigates to the restaurant detail fragment when a workmate's lunch is clicked.
         *
         * @param context The context used for fragment navigation.
         * @param lunch   The lunch data containing the selected restaurant details.
         */
        private void navigateToRestaurantDetail(Context context, Lunch lunch) {
            Bundle bundle = new Bundle();
            bundle.putString("restaurantId", lunch.getRestaurantId());
            bundle.putString("restaurantName", lunch.getRestaurantName());
            bundle.putString("restaurantAddress", lunch.getRestaurantAddress());

            RestaurantDetailFragment fragment = new RestaurantDetailFragment();
            fragment.setArguments(bundle);

            ((AppCompatActivity) context).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}