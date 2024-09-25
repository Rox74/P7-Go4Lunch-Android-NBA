package com.nathba.go4lunch.application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.nathba.go4lunch.models.Lunch;
import com.nathba.go4lunch.repository.LunchRepository;

import java.util.List;

/**
 * ViewModel class for managing lunch-related data.
 * It provides access to a list of lunches and allows adding new lunches.
 * This class communicates with the LunchRepository to perform data operations.
 */
public class LunchViewModel extends ViewModel {

    // Repository for managing lunch data
    private final LunchRepository lunchRepository;

    // LiveData to hold the list of lunches
    private final LiveData<List<Lunch>> lunches;

    /**
     * Constructor for LunchViewModel.
     * Initializes the LunchRepository and fetches the list of lunches.
     */
    public LunchViewModel(LunchRepository lunchRepository) {
        this.lunchRepository = lunchRepository;
        this.lunches = lunchRepository.getLunches();
    }

    /**
     * Returns LiveData object containing the list of lunches.
     * Observers can subscribe to this LiveData to receive updates on the list of lunches.
     *
     * @return LiveData<List<Lunch>> - LiveData containing the list of lunches.
     */
    public LiveData<List<Lunch>> getLunches() {
        return lunches;
    }

    /**
     * Adds a new lunch item to the repository.
     * This method will update the list of lunches, and the LiveData will be updated accordingly.
     *
     * @param lunch The Lunch object to be added.
     */
    public void addLunch(Lunch lunch) {
        lunchRepository.addLunch(lunch); // Add the lunch item to the repository
    }
}