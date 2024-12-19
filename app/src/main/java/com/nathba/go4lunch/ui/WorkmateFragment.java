package com.nathba.go4lunch.ui;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nathba.go4lunch.R;
import com.nathba.go4lunch.application.LunchViewModel;
import com.nathba.go4lunch.application.ViewModelFactory;
import com.nathba.go4lunch.application.WorkmateViewModel;
import com.nathba.go4lunch.di.AppInjector;

import java.util.ArrayList;

/**
 * Fragment to display and manage the list of workmates.
 * Uses WorkmateViewModel and LunchViewModel to observe and interact with workmate and lunch data.
 */
public class WorkmateFragment extends Fragment {

    private WorkmateViewModel workmateViewModel;
    private LunchViewModel lunchViewModel;
    private ViewModelFactory viewModelFactory;
    private RecyclerView recyclerView;
    private WorkmateAdapter workmateAdapter;

    /**
     * Inflates the layout for the fragment and initializes the RecyclerView and ViewModels.
     *
     * @param inflater  LayoutInflater to inflate the fragment's layout.
     * @param container Parent view group.
     * @param savedInstanceState Previously saved state, if any.
     * @return The root view of the fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workmate, container, false);

        // Obtain ViewModelFactory from AppInjector
        viewModelFactory = AppInjector.getInstance().getViewModelFactory();

        // Initialize ViewModels
        workmateViewModel = new ViewModelProvider(this, viewModelFactory).get(WorkmateViewModel.class);
        lunchViewModel = new ViewModelProvider(this, viewModelFactory).get(LunchViewModel.class);

        // Set up RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view_workmates);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Add a divider decoration to separate items
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        Drawable dividerDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.divider_drawable);
        if (dividerDrawable != null) {
            dividerItemDecoration.setDrawable(dividerDrawable);
        }
        recyclerView.addItemDecoration(dividerItemDecoration);

        // Initialize adapter with empty lists
        workmateAdapter = new WorkmateAdapter(new ArrayList<>(), new ArrayList<>());
        recyclerView.setAdapter(workmateAdapter);

        return view;
    }

    /**
     * Observes data changes in ViewModels and updates the adapter accordingly.
     *
     * @param view The fragment's root view.
     * @param savedInstanceState Previously saved state, if any.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Observe lunches and update the adapter
        lunchViewModel.getLunches().observe(getViewLifecycleOwner(), lunches -> {
            if (lunches != null) {
                workmateAdapter.updateLunches(lunches);
            }
        });

        // Observe workmates and update the adapter
        workmateViewModel.getWorkmates().observe(getViewLifecycleOwner(), workmates -> {
            if (workmates != null) {
                workmateAdapter.updateWorkmates(workmates);
            }
        });
    }
}