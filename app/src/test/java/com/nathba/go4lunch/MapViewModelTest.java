package com.nathba.go4lunch;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.location.Location;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.nathba.go4lunch.application.MapViewModel;
import com.nathba.go4lunch.models.Lunch;
import com.nathba.go4lunch.models.Restaurant;
import com.nathba.go4lunch.repository.LunchRepository;
import com.nathba.go4lunch.repository.MapRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.ArrayList;

public class MapViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private MapRepository mapRepository;

    @Mock
    private LunchRepository lunchRepository;

    private MapViewModel mapViewModel;
    private MutableLiveData<List<Restaurant>> restaurantLiveData;
    private MutableLiveData<List<Lunch>> lunchLiveData;
    private MutableLiveData<Location> userLocationLiveData;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        restaurantLiveData = new MutableLiveData<>();
        lunchLiveData = new MutableLiveData<>();
        userLocationLiveData = new MutableLiveData<>();

        when(mapRepository.getRestaurants()).thenReturn(restaurantLiveData);
        when(lunchRepository.getLunches()).thenReturn(lunchLiveData);

        mapViewModel = new MapViewModel(mapRepository, lunchRepository);
    }

    @Test
    public void getRestaurants_returnsRestaurantLiveData() {
        List<Restaurant> restaurants = new ArrayList<>();
        restaurantLiveData.setValue(restaurants);

        assertEquals(restaurants, mapViewModel.getRestaurants().getValue());
    }

    @Test
    public void getLunches_returnsLunchLiveData() {
        List<Lunch> lunches = new ArrayList<>();
        lunchLiveData.setValue(lunches);

        assertEquals(lunches, mapViewModel.getLunches().getValue());
    }

    @Test
    public void setUserLocation_updatesLiveData() {
        Location location = new Location("mock_provider");
        mapViewModel.setUserLocation(location);

        assertEquals(location, mapViewModel.getUserLocation().getValue());
    }
}