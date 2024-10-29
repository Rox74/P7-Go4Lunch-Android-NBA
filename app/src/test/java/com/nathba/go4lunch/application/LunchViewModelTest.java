package com.nathba.go4lunch.application;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.nathba.go4lunch.models.Lunch;
import com.nathba.go4lunch.repository.LunchRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

public class LunchViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private LunchRepository lunchRepository;

    private LunchViewModel lunchViewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        lunchViewModel = new LunchViewModel(lunchRepository);
    }

    @Test
    public void testGetLunches() {
        // Mocked data for lunches
        MutableLiveData<List<Lunch>> lunchesLiveData = new MutableLiveData<>();
        List<Lunch> lunches = new ArrayList<>();
        lunches.add(new Lunch("1", "workmate1", "restaurant1", new java.util.Date()));
        lunchesLiveData.setValue(lunches);

        // Mock the behavior of lunchRepository
        when(lunchRepository.getLunches()).thenReturn(lunchesLiveData);

        // Call the method in the ViewModel
        LiveData<List<Lunch>> result = lunchViewModel.getLunches();

        // Assert that the returned data is not null and contains the correct number of lunch entries
        assertNotNull(result);
        assertEquals(1, result.getValue().size());
        assertEquals("workmate1", result.getValue().get(0).getWorkmateId());

        // Verify the interaction with the repository
        verify(lunchRepository).getLunches();
    }

    @Test
    public void testAddLunch() {
        Lunch lunch = new Lunch("1", "workmate1", "restaurant123", new java.util.Date());

        // No need to mock since it's a void method
        lunchViewModel.addLunch(lunch);

        // Verify that the method in the repository was called correctly
        verify(lunchRepository).addLunch(lunch);
    }
}