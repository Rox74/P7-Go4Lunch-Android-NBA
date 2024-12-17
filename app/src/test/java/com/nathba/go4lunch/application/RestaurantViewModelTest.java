package com.nathba.go4lunch.application;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.nathba.go4lunch.models.Lunch;
import com.nathba.go4lunch.models.Restaurant;
import com.nathba.go4lunch.repository.LunchRepository;
import com.nathba.go4lunch.repository.RestaurantRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class RestaurantViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private LunchRepository lunchRepository;

    @Mock
    private Observer<List<Restaurant>> restaurantObserver;

    @Mock
    private Observer<List<Lunch>> lunchObserver;

    private RestaurantViewModel restaurantViewModel;

    private AutoCloseable closeable;

    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        restaurantViewModel = new RestaurantViewModel(restaurantRepository, lunchRepository);
    }

    @After
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void testGetLunchesForRestaurantToday() {
        // Given
        String restaurantId = "123";
        List<Lunch> lunches = Arrays.asList(
                new Lunch("1", "workmate1", "restaurant123", "Le 123", "3 rue de Paris", new Date())
        );
        MutableLiveData<List<Lunch>> lunchesLiveData = new MutableLiveData<>(lunches);

        when(lunchRepository.getLunchesForRestaurantToday(restaurantId)).thenReturn(lunchesLiveData);

        // Observe the result
        restaurantViewModel.getLunchesForRestaurantToday(restaurantId).observeForever(lunchObserver);

        // Then
        verify(lunchRepository).getLunchesForRestaurantToday(restaurantId);
        verify(lunchObserver).onChanged(lunches);
    }

    @Test
    public void testAddLunch() {
        // Given
        Lunch lunch = new Lunch("1", "workmate1", "restaurant123", "Le 123", "3 rue de Paris", new Date());

        // When
        restaurantViewModel.addLunch(lunch);

        // Then
        verify(lunchRepository).addLunch(lunch);
    }

    @Test
    public void testGetRestaurants() {
        // Given
        double latitude = 48.8566;
        double longitude = 2.3522;
        List<Restaurant> restaurants = Arrays.asList(
                new Restaurant("1", "Restaurant A", "Address 1", "", 4.5, null, "", "", "", false)
        );
        MutableLiveData<List<Restaurant>> restaurantsLiveData = new MutableLiveData<>(restaurants);

        when(restaurantRepository.getRestaurants(latitude, longitude)).thenReturn(restaurantsLiveData);

        // Observe the result
        restaurantViewModel.getRestaurants(latitude, longitude).observeForever(restaurantObserver);

        // Then
        verify(restaurantRepository).getRestaurants(latitude, longitude);
        verify(restaurantObserver).onChanged(restaurants);
    }

    @Test
    public void testAddRestaurant() {
        // Given
        Restaurant restaurant = new Restaurant("1", "Restaurant A", "Address 1", "", 4.5, null, "", "", "", false);

        // When
        restaurantViewModel.addRestaurant(restaurant);

        // Then
        verify(restaurantRepository).addRestaurantToFirestore(restaurant);
    }

    @Test
    public void testFetchRestaurantDetailsBulk() {
        // Given
        List<Restaurant> restaurants = Arrays.asList(
                new Restaurant("1", "Restaurant A", "Address A", "", 4.5, null, "", "", "", false),
                new Restaurant("2", "Restaurant B", "Address B", "", 4.0, null, "", "", "", false)
        );
        MutableLiveData<List<Restaurant>> detailedRestaurantsLiveData = new MutableLiveData<>(restaurants);

        when(restaurantRepository.fetchRestaurantsBulk(restaurants)).thenReturn(detailedRestaurantsLiveData);

        // Observe the result
        restaurantViewModel.fetchRestaurantDetailsBulk(restaurants).observeForever(restaurantObserver);

        // Then
        verify(restaurantRepository).fetchRestaurantsBulk(restaurants);
        verify(restaurantObserver).onChanged(restaurants);
    }

    @Test
    public void testGetDetailedRestaurants() {
        // Given
        List<Restaurant> cachedRestaurants = Arrays.asList(
                new Restaurant("1", "Restaurant A", "Address A", "", 4.5, null, "", "", "", false)
        );
        MutableLiveData<List<Restaurant>> cachedRestaurantsLiveData = new MutableLiveData<>(cachedRestaurants);

        when(restaurantRepository.getCachedRestaurants()).thenReturn(cachedRestaurantsLiveData);

        // Observe the result
        restaurantViewModel.getDetailedRestaurants().observeForever(restaurantObserver);

        // Then
        verify(restaurantRepository).getCachedRestaurants();
        verify(restaurantObserver).onChanged(cachedRestaurants);
    }
}