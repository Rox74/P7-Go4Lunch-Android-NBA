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

    public void updateWorkmates(List<Workmate> newWorkmates) {
        this.workmates = newWorkmates;
        notifyDataSetChanged();
    }

    public void updateLunches(List<Lunch> newLunches) {
        this.lunches = newLunches;
        notifyDataSetChanged();
    }

    static class WorkmateViewHolder extends RecyclerView.ViewHolder {

        private TextView nameTextView;
        private ImageView photoImageView;

        public WorkmateViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            photoImageView = itemView.findViewById(R.id.photo_image_view);
        }

        public void bind(Workmate workmate, List<Lunch> lunches) {
            Context context = itemView.getContext(); // Contexte nécessaire pour accéder aux ressources
            String text;

            // Par défaut, texte indiquant qu'aucun restaurant n'a été sélectionné
            text = context.getString(R.string.no_restaurant_selected, workmate.getName());

            // Vérifiez si le workmate a un déjeuner correspondant
            if (lunches != null) {
                for (Lunch lunch : lunches) {
                    if (lunch.getWorkmateId().equals(workmate.getWorkmateId())) {
                        text = context.getString(R.string.is_eating_at, workmate.getName(), lunch.getRestaurantName());
                        itemView.setOnClickListener(v -> navigateToRestaurantDetail(context, lunch));
                        break;
                    }
                }
            } else {
                itemView.setOnClickListener(null); // Aucun clic actif si aucun restaurant
            }

            // Définir le texte final pour le nameTextView
            nameTextView.setText(text);

            // Toujours gérer l'image de profil, indépendamment du déjeuner
            if (workmate.getPhotoUrl() != null && !workmate.getPhotoUrl().isEmpty()) {
                Glide.with(photoImageView.getContext())
                        .load(workmate.getPhotoUrl())
                        .placeholder(R.drawable.profile_picture) // Placeholder pendant le chargement
                        .error(R.drawable.profile_picture)       // Image par défaut en cas d'erreur
                        .transform(new CircleCrop()) // Transformation en image ronde
                        .into(photoImageView);
            } else {
                photoImageView.setImageResource(R.drawable.profile_picture); // Image par défaut si aucun lien
            }
        }

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