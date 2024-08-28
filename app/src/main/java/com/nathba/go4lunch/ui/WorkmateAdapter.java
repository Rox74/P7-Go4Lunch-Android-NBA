package com.nathba.go4lunch.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.nathba.go4lunch.R;
import com.nathba.go4lunch.models.Lunch;
import com.nathba.go4lunch.models.Restaurant;
import com.nathba.go4lunch.models.Workmate;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter class for displaying a list of workmates in a RecyclerView.
 * Handles creating and binding view holders for each workmate item.
 */
public class WorkmateAdapter extends RecyclerView.Adapter<WorkmateAdapter.WorkmateViewHolder> {

    private List<Workmate> workmates;

    /**
     * Constructor for WorkmateAdapter.
     *
     * @param workmates The initial list of workmates to display.
     */
    public WorkmateAdapter(List<Workmate> workmates) {
        this.workmates = workmates;
    }

    @NonNull
    @Override
    public WorkmateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for the individual workmate item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workmate, parent, false);
        return new WorkmateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkmateViewHolder holder, int position) {
        // Bind data to the view holder
        Workmate workmate = workmates.get(position);
        holder.bind(workmate);
    }

    @Override
    public int getItemCount() {
        // Return the total number of workmates
        return workmates.size();
    }

    /**
     * Updates the list of workmates and notifies the adapter of the change.
     *
     * @param newWorkmates The new list of workmates.
     */
    public void updateWorkmates(List<Workmate> newWorkmates) {
        this.workmates = newWorkmates;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class for holding the views for each workmate item.
     */
    static class WorkmateViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;
        private TextView emailTextView;
        private ImageView photoImageView;

        public WorkmateViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            emailTextView = itemView.findViewById(R.id.email_text_view);
            photoImageView = itemView.findViewById(R.id.photo_image_view);
        }

        /**
         * Binds the workmate data to the views in the ViewHolder.
         *
         * @param workmate The workmate object to bind.
         */
        public void bind(Workmate workmate) {
            nameTextView.setText(workmate.getName());
            emailTextView.setText(workmate.getEmail());

            if (workmate.getPhotoUrl() != null && !workmate.getPhotoUrl().isEmpty()) {
                // Load the workmate's photo using Glide
                Glide.with(photoImageView.getContext())
                        .load(workmate.getPhotoUrl())
                        .into(photoImageView);
            } else {
                // Set a default profile picture if no photo URL is available
                photoImageView.setImageResource(R.drawable.profile_picture);
            }
        }
    }
}