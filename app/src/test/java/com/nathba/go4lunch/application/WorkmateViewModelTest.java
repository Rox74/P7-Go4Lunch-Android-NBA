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

public class WorkmateViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private WorkmateRepository workmateRepository;

    @Mock
    private Observer<List<Workmate>> workmatesObserver;

    @Mock
    private Observer<Workmate> workmateObserver;

    private WorkmateViewModel workmateViewModel;

    private AutoCloseable closeable;

    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        // Mock the LiveData returned by WorkmateRepository
        MutableLiveData<List<Workmate>> workmatesLiveData = new MutableLiveData<>();
        when(workmateRepository.getWorkmates()).thenReturn(workmatesLiveData);

        // Initialize ViewModel
        workmateViewModel = new WorkmateViewModel(workmateRepository);
    }

    @After
    public void tearDown() throws Exception {
        closeable.close();
    }

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