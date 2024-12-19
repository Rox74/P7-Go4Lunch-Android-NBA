package com.nathba.go4lunch.application;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.nathba.go4lunch.models.Workmate;
import com.nathba.go4lunch.repository.WorkmateRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

/**
 * Unit test class for the WorkmateViewModel.
 * Ensures interactions with WorkmateRepository and correct LiveData behavior.
 */
public class WorkmateViewModelTest {

    /**
     * Rule to execute LiveData tasks synchronously in test cases.
     */
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    /**
     * Mocked repository for managing workmate data.
     */
    @Mock
    private WorkmateRepository workmateRepository;

    /**
     * Mocked observer for observing a list of workmates.
     */
    @Mock
    private Observer<List<Workmate>> workmatesObserver;

    /**
     * Mocked observer for observing a single workmate.
     */
    @Mock
    private Observer<Workmate> workmateObserver;

    /**
     * ViewModel instance under test.
     */
    private WorkmateViewModel workmateViewModel;

    /**
     * AutoCloseable resource to release mocks after each test.
     */
    private AutoCloseable closeable;

    /**
     * Initializes mocks and the ViewModel before each test.
     */
    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        // Mock the LiveData returned by WorkmateRepository
        MutableLiveData<List<Workmate>> workmatesLiveData = new MutableLiveData<>();
        when(workmateRepository.getWorkmates()).thenReturn(workmatesLiveData);

        // Initialize ViewModel
        workmateViewModel = new WorkmateViewModel(workmateRepository);
    }

    /**
     * Cleans up resources and mocks after each test.
     */
    @After
    public void tearDown() throws Exception {
        closeable.close();
    }

    /**
     * Tests that `getWorkmateById` retrieves a specific workmate and updates the observer.
     */
    @Test
    public void getWorkmateById_shouldReturnWorkmate() {
        // Given
        String workmateId = "1";
        Workmate workmate = new Workmate("1", "Alice", "alice@example.com", "Le Jules Verne");
        MutableLiveData<Workmate> workmateLiveData = new MutableLiveData<>(workmate);

        // Mock repository
        when(workmateRepository.getWorkmateById(workmateId)).thenReturn(workmateLiveData);

        // Observe the LiveData
        workmateViewModel.getWorkmateById(workmateId).observeForever(workmateObserver);

        // Then
        verify(workmateRepository).getWorkmateById(workmateId);
        verify(workmateObserver).onChanged(workmate);
    }

    /**
     * Tests that `addWorkmate` invokes the repository method to add a new workmate.
     */
    @Test
    public void addWorkmate_shouldCallRepositoryAddWorkmate() {
        // Given
        Workmate newWorkmate = new Workmate("3", "Charlie", "charlie@example.com", "Cafe de Paris");

        // When
        workmateViewModel.addWorkmate(newWorkmate);

        // Then
        verify(workmateRepository).addWorkmate(newWorkmate);
    }
}