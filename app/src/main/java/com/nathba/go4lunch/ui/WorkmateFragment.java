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
import com.nathba.go4lunch.application.LunchViewModel;
import com.nathba.go4lunch.application.ViewModelFactory;
import com.nathba.go4lunch.application.WorkmateViewModel;
import com.nathba.go4lunch.di.AppInjector;
import com.nathba.go4lunch.models.Workmate;

import java.util.ArrayList;

/**
 * Fragment to display and manage the list of workmates.
 * Uses WorkmateViewModel to observe and interact with the workmate data.
 */
public class WorkmateFragment extends Fragment {

    private WorkmateViewModel workmateViewModel;
    private LunchViewModel lunchViewModel;
    private ViewModelFactory viewModelFactory;
    private RecyclerView recyclerView;
    private WorkmateAdapter workmateAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workmate, container, false);

        // Obtain ViewModelFactory from AppInjector
        viewModelFactory = AppInjector.getInstance().getViewModelFactory();

        // Initialize ViewModels
        workmateViewModel = new ViewModelProvider(this, viewModelFactory).get(WorkmateViewModel.class);
        lunchViewModel = new ViewModelProvider(this, viewModelFactory).get(LunchViewModel.class);

        recyclerView = view.findViewById(R.id.recycler_view_workmates);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize adapter with empty lists
        workmateAdapter = new WorkmateAdapter(new ArrayList<>(), new ArrayList<>());
        recyclerView.setAdapter(workmateAdapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Observe lunches
        lunchViewModel.getLunches().observe(getViewLifecycleOwner(), lunches -> {
            if (lunches != null) {
                workmateAdapter.updateLunches(lunches);
            }
        });

        // Observe workmates
        workmateViewModel.getWorkmates().observe(getViewLifecycleOwner(), workmates -> {
            if (workmates != null) {
                workmateAdapter.updateWorkmates(workmates);
            }
        });
    }
}