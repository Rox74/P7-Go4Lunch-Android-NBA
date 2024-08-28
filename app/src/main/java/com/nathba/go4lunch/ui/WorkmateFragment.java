package com.nathba.go4lunch.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.nathba.go4lunch.R;
import com.nathba.go4lunch.application.WorkmateViewModel;
import com.nathba.go4lunch.models.Workmate;

import java.util.ArrayList;

/**
 * Fragment to display and manage the list of workmates.
 * Uses WorkmateViewModel to observe and interact with the workmate data.
 */
public class WorkmateFragment extends Fragment {

    private WorkmateViewModel workmateViewModel;
    private RecyclerView recyclerView;
    private WorkmateAdapter workmateAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workmate, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_workmates);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        workmateAdapter = new WorkmateAdapter(new ArrayList<>());
        recyclerView.setAdapter(workmateAdapter);

        workmateViewModel = new ViewModelProvider(this).get(WorkmateViewModel.class);

        // Observe changes in the list of workmates
        workmateViewModel.getWorkmates().observe(getViewLifecycleOwner(), workmates -> {
            if (workmates != null) {
                workmateAdapter.updateWorkmates(workmates);
            } else {
                // Handle the case where workmates list is null
                Toast.makeText(getContext(), "No workmates found", Toast.LENGTH_SHORT).show();
            }
        });

        // Add a test workmate
        addTestWorkmate();

        return view;
    }

    /**
     * Adds a test workmate based on the current logged-in user.
     * This is typically used for development and testing.
     */
    private void addTestWorkmate() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            String userName = auth.getCurrentUser().getDisplayName();
            String userEmail = auth.getCurrentUser().getEmail();
            String photoUrl = auth.getCurrentUser().getPhotoUrl() != null ? auth.getCurrentUser().getPhotoUrl().toString() : null;

            Workmate workmate = new Workmate(userId, userName, userEmail, photoUrl);
            workmateViewModel.addWorkmate(workmate);
        } else {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }
}