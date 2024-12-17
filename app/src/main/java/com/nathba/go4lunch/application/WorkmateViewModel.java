package com.nathba.go4lunch.application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.nathba.go4lunch.models.Workmate;
import com.nathba.go4lunch.repository.WorkmateRepository;

import java.util.List;

/**
 * ViewModel class for managing workmate-related data.
 * <p>
 * This class interacts with the {@link WorkmateRepository} to fetch, observe, and update workmate information.
 * It exposes data as {@link LiveData} so the UI can observe and react to changes automatically.
 */
public class WorkmateViewModel extends ViewModel {

    /** Repository responsible for managing workmate data operations. */
    private final WorkmateRepository workmateRepository;

    /** LiveData holding the list of all workmates. */
    private final LiveData<List<Workmate>> workmates;

    /**
     * Constructor for {@link WorkmateViewModel}.
     * <p>
     * Initializes the ViewModel with an instance of {@link WorkmateRepository}
     * and retrieves the list of workmates from the repository.
     *
     * @param workmateRepository The repository responsible for managing workmate data.
     */
    public WorkmateViewModel(WorkmateRepository workmateRepository) {
        this.workmateRepository = workmateRepository;
        this.workmates = workmateRepository.getWorkmates();
    }

    /**
     * Retrieves a {@link LiveData} object containing the list of all workmates.
     * <p>
     * Observers can subscribe to this LiveData to be notified whenever the list of workmates is updated.
     *
     * @return LiveData containing a list of {@link Workmate} objects.
     */
    public LiveData<List<Workmate>> getWorkmates() {
        return workmates;
    }

    /**
     * Retrieves a single workmate by their unique ID.
     * <p>
     * This method fetches a specific workmate details from the repository using their ID.
     *
     * @param workmateId The unique identifier of the workmate.
     * @return LiveData containing the {@link Workmate} object corresponding to the given ID.
     */
    public LiveData<Workmate> getWorkmateById(String workmateId) {
        return workmateRepository.getWorkmateById(workmateId);
    }

    /**
     * Adds a new workmate to the repository.
     * <p>
     * This method delegates the addition of a workmate to the {@link WorkmateRepository}.
     *
     * @param workmate The {@link Workmate} object to be added.
     */
    public void addWorkmate(Workmate workmate) {
        workmateRepository.addWorkmate(workmate);
    }
}