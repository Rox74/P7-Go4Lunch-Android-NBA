package com.nathba.go4lunch.application;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.android.gms.tasks.Task;
import com.nathba.go4lunch.models.Lunch;
import com.nathba.go4lunch.repository.LunchRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Unit test class for the LunchViewModel.
 * Ensures proper interactions with the LunchRepository and validates ViewModel behavior.
 */
public class LunchViewModelTest {

    /**
     * Rule to allow LiveData to work synchronously during tests.
     */
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    /**
     * Mocked instance of LunchRepository to simulate repository operations.
     */
    @Mock
    private LunchRepository lunchRepository;

    /**
     * Instance of LunchViewModel under test.
     */
    private LunchViewModel lunchViewModel;

    /**
     * AutoCloseable for closing mock resources.
     */
    private AutoCloseable closeable;

    /**
     * Mock observer for observing lists of lunches.
     */
    @Mock
    private Observer<List<Lunch>> lunchesObserver;

    /**
     * Mock observer for observing single lunch data.
     */
    @Mock
    private Observer<Lunch> lunchObserver;

    /**
     * MutableLiveData to simulate repository LiveData responses.
     */
    private MutableLiveData<List<Lunch>> lunchesLiveData;

    /**
     * Sets up the test environment by initializing mocks and the ViewModel.
     */
    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        lunchViewModel = new LunchViewModel(lunchRepository);

        // Initialize mock LiveData
        lunchesLiveData = new MutableLiveData<>();
    }

    /**
     * Closes mock resources after each test.
     */
    @After
    public void tearDown() throws Exception {
        closeable.close();
    }

    /**
     * Tests that `getLunches()` correctly observes and retrieves lunch data when available.
     */
    @Test
    public void testGetLunches_withData() {
        // Given
        List<Lunch> lunchList = new ArrayList<>();
        lunchList.add(new Lunch("1", "workmate1", "restaurant123", "Le 123", "3 rue de Paris", new Date()));
        lunchesLiveData.setValue(lunchList);

        when(lunchRepository.getLunches()).thenReturn(lunchesLiveData);

        // When
        lunchViewModel.getLunches().observeForever(lunchesObserver);

        // Then
        verify(lunchesObserver).onChanged(lunchList);
    }

    /**
     * Tests that `getLunches()` correctly handles cases where no lunch data is available.
     */
    @Test
    public void testGetLunches_withNoData() {
        // Given
        lunchesLiveData.setValue(new ArrayList<>());
        when(lunchRepository.getLunches()).thenReturn(lunchesLiveData);

        // When
        lunchViewModel.getLunches().observeForever(lunchesObserver);

        // Then
        verify(lunchesObserver).onChanged(new ArrayList<>());
    }

    /**
     * Tests that adding a lunch calls the appropriate repository method.
     */
    @Test
    public void testAddLunch() {
        // Given
        Lunch lunch = new Lunch("1", "workmate1", "restaurant123", "Le 123", "3 rue de Paris", new Date());

        // When
        lunchViewModel.addLunch(lunch);

        // Then
        verify(lunchRepository).addLunch(lunch);
    }

    /**
     * Tests that adding a null lunch does not call the repository method.
     */
    @Test
    public void testAddLunch_withNullData() {
        // When
        lunchViewModel.addLunch(null);

        // Then
        verify(lunchRepository, never()).addLunch(any());
    }

    /**
     * Tests that deleting a user's lunch for a specific date calls the repository method.
     */
    @Test
    public void testDeleteUserLunchForDate() {
        // Given
        String workmateId = "workmate1";
        Date date = new Date();
        Task<Void> mockTask = mock(Task.class);

        when(lunchRepository.deleteUserLunchForDate(workmateId, date)).thenReturn(mockTask);

        // When
        Task<Void> result = lunchViewModel.deleteUserLunchForDate(workmateId, date);

        // Then
        verify(lunchRepository).deleteUserLunchForDate(workmateId, date);
        assertEquals(mockTask, result);
    }

    /**
     * Tests that deleting expired lunches calls the repository method.
     */
    @Test
    public void testDeleteExpiredLunches() {
        // Given
        Task<Void> mockTask = mock(Task.class);

        when(lunchRepository.deleteExpiredLunches()).thenReturn(mockTask);

        // When
        Task<Void> result = lunchViewModel.deleteExpiredLunches();

        // Then
        verify(lunchRepository).deleteExpiredLunches();
        assertEquals(mockTask, result);
    }

    /**
     * Tests that `getUserLunchForToday()` retrieves lunch data when available.
     */
    @Test
    public void testGetUserLunchForToday_withData() {
        // Given
        String userId = "workmate1";
        Lunch lunch = new Lunch("1", "workmate1", "restaurant123", "Le 123", "3 rue de Paris", new Date());
        MutableLiveData<Lunch> lunchLiveData = new MutableLiveData<>(lunch);

        when(lunchRepository.getUserLunchForToday(userId)).thenReturn(lunchLiveData);

        // When
        lunchViewModel.getUserLunchForToday(userId).observeForever(lunchObserver);

        // Then
        verify(lunchObserver).onChanged(lunch);
    }

    /**
     * Tests that `getUserLunchForToday()` handles cases where no lunch data is available.
     */
    @Test
    public void testGetUserLunchForToday_withNoData() {
        // Given
        String userId = "workmate1";
        MutableLiveData<Lunch> lunchLiveData = new MutableLiveData<>(null);

        when(lunchRepository.getUserLunchForToday(userId)).thenReturn(lunchLiveData);

        // When
        lunchViewModel.getUserLunchForToday(userId).observeForever(lunchObserver);

        // Then
        verify(lunchObserver).onChanged(null);
    }
}