package com.nathba.go4lunch.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

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

public class LunchViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private LunchRepository lunchRepository;

    private LunchViewModel lunchViewModel;
    private AutoCloseable closeable;

    @Mock
    private Observer<List<Lunch>> lunchesObserver;

    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        lunchViewModel = new LunchViewModel(lunchRepository);
    }

    @After
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void testGetLunches_withData() {
        // Mocked data for lunches
        MutableLiveData<List<Lunch>> lunchesLiveData = new MutableLiveData<>();
        List<Lunch> lunches = new ArrayList<>();
        Lunch lunch = new Lunch("1", "workmate1", "restaurant123", "Le 123", "3 rue de Paris", new java.util.Date());
        lunches.add(lunch);
        lunchesLiveData.setValue(lunches);

        // Mock the behavior of lunchRepository
        when(lunchRepository.getLunches()).thenReturn(lunchesLiveData);

        // Observe the LiveData and attach mock Observer
        lunchViewModel.getLunches().observeForever(lunchesObserver);

        lunchViewModel.getLunches();

        // Verify observer received the data
        verify(lunchesObserver).onChanged(lunches);

        // Verify that getLunches is called only once
        //verify(lunchRepository, times(1)).getLunches();

        // Clean up observer
        lunchViewModel.getLunches().removeObserver(lunchesObserver);
    }

    @Test
    public void testGetLunches_withNoData() {
        // Mock empty LiveData response
        MutableLiveData<List<Lunch>> emptyLiveData = new MutableLiveData<>(new ArrayList<>());
        when(lunchRepository.getLunches()).thenReturn(emptyLiveData);

        // Attach observer and test empty data scenario
        lunchViewModel.getLunches().observeForever(lunchesObserver);

        // Verify observer receives empty list
        verify(lunchesObserver).onChanged(new ArrayList<>());

        // Clean up observer
        lunchViewModel.getLunches().removeObserver(lunchesObserver);
    }

    @Test
    public void testAddLunch() {
        Lunch lunch = new Lunch("1", "workmate1", "restaurant123", "Le 123", "3 rue de Paris", new java.util.Date());

        // Perform action
        lunchViewModel.addLunch(lunch);

        // Verify that the method in the repository was called correctly
        verify(lunchRepository).addLunch(lunch);
    }

    @Test
    public void testAddLunch_withNullData() {
        // Attempt to add a null lunch entry
        lunchViewModel.addLunch(null);

        // Verify that addLunch was not called due to null input
        verify(lunchRepository, never()).addLunch(any());
    }
}