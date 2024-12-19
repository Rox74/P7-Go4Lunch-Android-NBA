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

/**
 * Unit test class for the RestaurantViewModel.
 * Validates interactions with RestaurantRepository and LunchRepository
 * and ensures correct LiveData updates.
 */
public class RestaurantViewModelTest {

    /**
     * Rule to execute LiveData tasks synchronously in test cases.
     */
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    /**
     * Mocked repository for managing restaurant data.
     */
    @Mock
    private RestaurantRepository restaurantRepository;

    /**
     * Mocked repository for managing lunch data.
     */
    @Mock
    private LunchRepository lunchRepository;

    /**
     * Mocked observer for observing restaurant data.
     */
    @Mock
    private Observer<List<Restaurant>> restaurantObserver;

    /**
     * Mocked observer for observing lunch data.
     */
    @Mock
    private Observer<List<Lunch>> lunchObserver;

    /**
     * ViewModel instance under test.
     */
    private RestaurantViewModel restaurantViewModel;

    /**
     * AutoCloseable resource to release mocks after each test.
     */
    private AutoCloseable closeable;

    /**
     * Initializes the test environment, mocks, and ViewModel instance.
     */
    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        restaurantViewModel = new RestaurantViewModel(restaurantRepository, lunchRepository);
    }

    /**
     * Cleans up resources and mocks after each test.
     */
    @After
    public void tearDown() throws Exception {
        closeable.close();
    }

    /**
     * Tests that `getLunchesForRestaurantToday` retrieves lunch data for a specific restaurant
     * and updates the observer.
     */
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

    /**
     * Tests that `addLunch` invokes the repository method to add lunch data.
     */
    @Test
    public void testAddLunch() {
        // Given
        Lunch lunch = new Lunch("1", "workmate1", "restaurant123", "Le 123", "3 rue de Paris", new Date());

        // When
        restaurantViewModel.addLunch(lunch);

        // Then
        verify(lunchRepository).addLunch(lunch);
    }

    /**
     * Tests that `getRestaurants` fetches restaurant data based on location
     * and updates the observer.
     */
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

    /**
     * Tests that `addRestaurant` adds a new restaurant to Firestore through the repository.
     */
    @Test
    public void testAddRestaurant() {
        // Given
        Restaurant restaurant = new Restaurant("1", "Restaurant A", "Address 1", "", 4.5, null, "", "", "", false);

        // When
        restaurantViewModel.addRestaurant(restaurant);

        // Then
        verify(restaurantRepository).addRestaurantToFirestore(restaurant);
    }

    /**
     * Tests that `fetchRestaurantDetailsBulk` fetches detailed restaurant data
     * and updates the observer with the results.
     */
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

    /**
     * Tests that `getDetailedRestaurants` retrieves cached restaurant data
     * and updates the observer.
     */
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