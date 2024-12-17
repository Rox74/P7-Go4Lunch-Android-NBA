package com.nathba.go4lunch.application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.nathba.go4lunch.models.Lunch;
import com.nathba.go4lunch.repository.LunchRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * ViewModel class responsible for managing lunch-related data and operations.
 * <p>
 * It serves as an intermediary between the UI and the {@link LunchRepository},
 * providing data to the UI as {@link LiveData} and handling user actions such as
 * adding, retrieving, and deleting lunch entries.
 */
public class LunchViewModel extends ViewModel {

    /** Repository responsible for performing lunch data operations with Firebase. */
    private final LunchRepository lunchRepository;

    /** LiveData holding the list of all lunches. */
    private final LiveData<List<Lunch>> lunches;

    /**
     * Constructor for {@link LunchViewModel}.
     * <p>
     * Initializes the {@link LunchRepository} and fetches the current list of lunches.
     *
     * @param lunchRepository The repository responsible for accessing lunch data.
     */
    public LunchViewModel(LunchRepository lunchRepository) {
        this.lunchRepository = lunchRepository;
        this.lunches = lunchRepository.getLunches();
    }

    /**
     * Returns a {@link LiveData} object containing the list of all lunches.
     * <p>
     * If no lunches are available, it ensures that an empty list is returned as a fallback.
     *
     * @return LiveData containing the list of {@link Lunch} objects.
     */
    public LiveData<List<Lunch>> getLunches() {
        return lunchRepository.getLunches() != null ?
                lunchRepository.getLunches() : new MutableLiveData<>(new ArrayList<>());
    }

    /**
     * Adds a new lunch entry to the database.
     * <p>
     * This method checks for null values before delegating the operation to the repository.
     *
     * @param lunch The {@link Lunch} object representing the lunch to be added.
     */
    public void addLunch(Lunch lunch) {
        if (lunch != null) {
            lunchRepository.addLunch(lunch);
        }
    }

    /**
     * Deletes lunch entries for a specific user on a given date to avoid duplicates.
     * <p>
     * This operation is delegated to the repository, which performs the deletion on Firebase.
     *
     * @param workmateId The unique identifier of the user whose lunch is to be deleted.
     * @param date       The date of the lunch to be deleted.
     * @return A {@link Task<Void>} representing the asynchronous deletion process.
     */
    public Task<Void> deleteUserLunchForDate(String workmateId, Date date) {
        return lunchRepository.deleteUserLunchForDate(workmateId, date);
    }

    /**
     * Deletes all expired lunches from the database.
     * <p>
     * Lunch entries are considered expired if their date is earlier than the current date.
     *
     * @return A {@link Task<Void>} representing the asynchronous cleanup process.
     */
    public Task<Void> deleteExpiredLunches() {
        return lunchRepository.deleteExpiredLunches();
    }

    /**
     * Retrieves the lunch entry for a specific user for the current day.
     * <p>
     * This method queries the repository to fetch the lunch associated with the given user ID.
     *
     * @param userId The unique identifier of the user whose lunch is to be retrieved.
     * @return A {@link LiveData} containing the {@link Lunch} object for the current day.
     */
    public LiveData<Lunch> getUserLunchForToday(String userId) {
        return lunchRepository.getUserLunchForToday(userId);
    }
}