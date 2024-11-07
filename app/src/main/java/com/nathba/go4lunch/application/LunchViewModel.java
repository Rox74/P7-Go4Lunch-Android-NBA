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

    public LiveData<List<Lunch>> getLunches() {
        // Retourne toujours un LiveData, même si aucun lunch n'est disponible
        return lunchRepository.getLunches() != null ? lunchRepository.getLunches() : new MutableLiveData<>(new ArrayList<>());
    }

    public void addLunch(Lunch lunch) {
        if (lunch != null) {
            lunchRepository.addLunch(lunch);
        }
    }

    /**
     * Supprime les lunchs d'un utilisateur pour une date donnée afin d'éviter les doublons.
     *
     * @param workmateId L'identifiant de l'utilisateur.
     * @param date       La date du lunch à supprimer.
     * @return Une tâche qui complète la suppression.
     */
    public Task<Void> deleteUserLunchForDate(String workmateId, Date date) {
        return lunchRepository.deleteUserLunchForDate(workmateId, date);
    }

    /**
     * Supprime tous les lunchs périmés dans Firebase.
     *
     * @return Une tâche qui complète la suppression des lunchs périmés.
     */
    public Task<Void> deleteExpiredLunches() {
        return lunchRepository.deleteExpiredLunches();
    }
}