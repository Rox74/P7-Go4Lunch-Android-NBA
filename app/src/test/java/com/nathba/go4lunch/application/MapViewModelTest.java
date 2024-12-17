package com.nathba.go4lunch.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.location.Location;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.nathba.go4lunch.models.Lunch;
import com.nathba.go4lunch.models.Restaurant;
import com.nathba.go4lunch.repository.LunchRepository;
import com.nathba.go4lunch.repository.MapRepository;
import com.nathba.go4lunch.repository.RepositoryCallback;
import com.nathba.go4lunch.repository.RestaurantRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.osmdroid.util.GeoPoint;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.mockito.MockitoAnnotations;

public class MapViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private MapRepository mapRepository;

    @Mock
    private LunchRepository lunchRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private Observer<Restaurant> selectedRestaurantObserver;

    @Mock
    private Observer<List<Restaurant>> detailedRestaurantsObserver;

    @Mock
    private Observer<List<Lunch>> lunchesTodayObserver;

    private MapViewModel mapViewModel;

    private AutoCloseable closeable;

    @Before
    public void setup() {
        closeable = MockitoAnnotations.openMocks(this);
        mapViewModel = new MapViewModel(mapRepository, lunchRepository, restaurantRepository);
    }

    @After
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void loadRestaurants_shouldCallRepositoryWithCorrectCoordinates() {
        // Given
        double latitude = 48.8566;
        double longitude = 2.3522;

        // When
        mapViewModel.loadRestaurants(latitude, longitude);

        // Then
        verify(mapRepository).loadRestaurants(latitude, longitude);
    }

    @Test
    public void fetchRestaurantDetails_shouldReturnSelectedRestaurant() {
        // Given
        Restaurant restaurant = new Restaurant("1", "Le Jules Verne", "Paris", "", 4.5, new GeoPoint(48.858844, 2.294351), "", "", "", false);

        doAnswer(invocation -> {
            RepositoryCallback<Restaurant> callback = invocation.getArgument(3);
            callback.onSuccess(restaurant);
            return null;
        }).when(restaurantRepository).getRestaurantDetails(eq("1"), any(), eq("Le Jules Verne"), any());

        // Observe selectedRestaurant
        mapViewModel.getSelectedRestaurant().observeForever(selectedRestaurantObserver);

        // When
        mapViewModel.fetchRestaurantDetails(restaurant.getRestaurantId(), restaurant.getLocation(), restaurant.getName());

        // Then
        verify(selectedRestaurantObserver).onChanged(restaurant);
    }

    @Test
    public void fetchRestaurantDetails_onError_shouldReturnBasicRestaurant() {
        // Given
        String restaurantId = "1";
        String restaurantName = "Le Jules Verne";
        GeoPoint location = new GeoPoint(48.858844, 2.294351);

        doAnswer(invocation -> {
            RepositoryCallback<Restaurant> callback = invocation.getArgument(3);
            callback.onError(new Exception("Error"));
            return null;
        }).when(restaurantRepository).getRestaurantDetails(eq(restaurantId), eq(location), eq(restaurantName), any());

        Restaurant basicRestaurant = new Restaurant(restaurantId, restaurantName, "", "", 0.0, location, "", "", "", false);

        // Observe selectedRestaurant
        mapViewModel.getSelectedRestaurant().observeForever(selectedRestaurantObserver);

        // When
        mapViewModel.fetchRestaurantDetails(restaurantId, location, restaurantName);

        // Then
        verify(selectedRestaurantObserver).onChanged(basicRestaurant);
    }

    @Test
    public void fetchRestaurantsDetailsIfNeeded_shouldCallRepositoryAndUpdateLiveData() {
        // Given
        List<Restaurant> restaurants = Arrays.asList(
                new Restaurant("1", "Restaurant 1", "Address 1", "", 4.5, new GeoPoint(48.8566, 2.3522), "", "", "", false),
                new Restaurant("2", "Restaurant 2", "Address 2", "", 4.0, new GeoPoint(48.8570, 2.3530), "", "", "", false)
        );

        MutableLiveData<List<Restaurant>> detailedRestaurantsLiveData = new MutableLiveData<>(restaurants);
        when(restaurantRepository.fetchRestaurantsBulk(restaurants)).thenReturn(detailedRestaurantsLiveData);

        // Observe detailedRestaurants
        mapViewModel.getDetailedRestaurants().observeForever(detailedRestaurantsObserver);

        // When
        mapViewModel.fetchRestaurantsDetailsIfNeeded(restaurants);

        // Then
        verify(detailedRestaurantsObserver).onChanged(restaurants);
    }

    @Test
    public void loadLunchesToday_shouldCallRepositoryAndUpdateLiveData() {
        // Given
        List<Lunch> lunches = Arrays.asList(
                new Lunch("1", "workmate1", "restaurant123", "Le Jules Verne", "Paris", new Date())
        );
        MutableLiveData<List<Lunch>> lunchesLiveData = new MutableLiveData<>(lunches);
        when(lunchRepository.getLunchesToday()).thenReturn(lunchesLiveData);

        // Observe lunchesToday
        mapViewModel.getLunchesToday().observeForever(lunchesTodayObserver);

        // When
        mapViewModel.loadLunchesToday();

        // Then
        verify(lunchesTodayObserver).onChanged(lunches);
    }
}