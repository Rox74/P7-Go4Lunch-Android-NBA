package com.nathba.go4lunch.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.nathba.go4lunch.models.Lunch;
import com.nathba.go4lunch.models.Restaurant;
import com.nathba.go4lunch.repository.LunchRepository;
import com.nathba.go4lunch.repository.RestaurantRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

public class RestaurantViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private LunchRepository lunchRepository;

    private RestaurantViewModel restaurantViewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        restaurantViewModel = new RestaurantViewModel(restaurantRepository, lunchRepository);
    }

    @Test
    public void testGetLunchesForRestaurantToday() {
        String restaurantId = "123";
        MutableLiveData<List<Lunch>> lunchesLiveData = new MutableLiveData<>();
        List<Lunch> lunches = new ArrayList<>();
        lunches.add(new Lunch("1", "workmate1", restaurantId, new java.util.Date()));
        lunchesLiveData.setValue(lunches);

        // Mock the lunchRepository behavior
        when(lunchRepository.getLunchesForRestaurantToday(restaurantId)).thenReturn(lunchesLiveData);

        // Observe the result from the ViewModel
        LiveData<List<Lunch>> result = restaurantViewModel.getLunchesForRestaurantToday(restaurantId);

        assertNotNull(result);
        assertEquals(1, result.getValue().size());
        assertEquals("workmate1", result.getValue().get(0).getWorkmateId());

        // Verify the interaction with the repository
        verify(lunchRepository).getLunchesForRestaurantToday(restaurantId);
    }

    @Test
    public void testAddLunch() {
        Lunch lunch = new Lunch("1", "workmate1", "restaurant123", new java.util.Date());

        // No need to mock since it's a void method
        restaurantViewModel.addLunch(lunch);

        // Verify the method in the repository is called
        verify(lunchRepository).addLunch(lunch);
    }

    @Test
    public void testGetRestaurants() {
        double latitude = 48.8566;
        double longitude = 2.3522;

        // Prepare mocked data
        MutableLiveData<List<Restaurant>> restaurantsLiveData = new MutableLiveData<>();
        List<Restaurant> restaurants = new ArrayList<>();
        restaurants.add(new Restaurant("1", "Restaurant A", "Address 1", "", 4.5, null, "", "", "", false));
        restaurantsLiveData.setValue(restaurants);

        // Mock the repository behavior
        when(restaurantRepository.getRestaurants(latitude, longitude)).thenReturn(restaurantsLiveData);

        // Call the method in the ViewModel
        LiveData<List<Restaurant>> result = restaurantViewModel.getRestaurants(latitude, longitude);

        assertNotNull(result);
        assertEquals(1, result.getValue().size());
        assertEquals("Restaurant A", result.getValue().get(0).getName());

        // Verify the interaction with the repository
        verify(restaurantRepository).getRestaurants(latitude, longitude);
    }

    @Test
    public void testAddRestaurant() {
        Restaurant restaurant = new Restaurant("1", "Restaurant A", "Address 1", "", 4.5, null, "", "", "", false);

        // Call the method in the ViewModel
        restaurantViewModel.addRestaurant(restaurant);

        // Verify the interaction with the repository
        verify(restaurantRepository).addRestaurantToFirestore(restaurant);
    }

    @Test
    public void testGetLunchCountForRestaurant() {
        String restaurantId = "restaurant123";
        MutableLiveData<Integer> lunchCountLiveData = new MutableLiveData<>();
        lunchCountLiveData.setValue(5);

        // Mock the repository behavior
        when(lunchRepository.getLunchCountForRestaurant(restaurantId)).thenReturn(lunchCountLiveData);

        // Call the method in the ViewModel
        LiveData<Integer> result = restaurantViewModel.getLunchCountForRestaurant(restaurantId);

        assertNotNull(result);
        assertEquals(5, result.getValue().intValue());

        // Verify the interaction with the repository
        verify(lunchRepository).getLunchCountForRestaurant(restaurantId);
    }
}