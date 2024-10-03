package com.nathba.go4lunch.application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.nathba.go4lunch.models.Workmate;
import com.nathba.go4lunch.repository.WorkmateRepository;

import java.util.List;

/**
 * ViewModel class for managing workmate data.
 * It interacts with the WorkmateRepository to fetch and update workmate information.
 */
public class WorkmateViewModel extends ViewModel {

    // Repository for managing workmate data
    private final WorkmateRepository workmateRepository;

    // LiveData to hold the list of workmates
    private final LiveData<List<Workmate>> workmates;

    /**
     * Constructor for WorkmateViewModel.
     * Initializes the WorkmateRepository instance and fetches the list of workmates.
     */
    public WorkmateViewModel(WorkmateRepository workmateRepository) {
        this.workmateRepository = workmateRepository;
        this.workmates = workmateRepository.getWorkmates();
    }

    /**
     * Returns LiveData object containing the list of workmates.
     * Observers can subscribe to this LiveData to receive updates when the workmate list changes.
     *
     * @return LiveData<List<Workmate>> - LiveData containing the list of workmates.
     */
    public LiveData<List<Workmate>> getWorkmates() {
        return workmates;
    }

    public LiveData<Workmate> getWorkmateById(String workmateId) {
        return workmateRepository.getWorkmateById(workmateId);
    }

    /**
     * Adds a new workmate to the repository.
     *
     * @param workmate The workmate to be added.
     */
    public void addWorkmate(Workmate workmate) {
        workmateRepository.addWorkmate(workmate);
    }
}