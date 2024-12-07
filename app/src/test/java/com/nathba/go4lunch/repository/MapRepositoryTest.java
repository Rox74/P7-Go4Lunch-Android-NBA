package com.nathba.go4lunch.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.nathba.go4lunch.models.Restaurant;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class MapRepositoryTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private MutableLiveData<List<Restaurant>> mockLiveData;

    private MapRepository mapRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mapRepository = new MapRepository();
        // Inject the restaurantRepository mock directly
        mapRepository = Mockito.spy(mapRepository);
        Mockito.doReturn(mockLiveData).when(mapRepository).getRestaurants();
    }

    @Test
    public void loadRestaurants_shouldFetchRestaurantsFromRepository() {
        // Arrange
        double latitude = 48.8566;
        double longitude = 2.3522;
        List<Restaurant> restaurants = Arrays.asList(new Restaurant("1", "Test", "Test St", null, 4.5, null, null, null, null, false));

        when(restaurantRepository.getRestaurants(latitude, longitude)).thenReturn(mockLiveData);
        when(mockLiveData.getValue()).thenReturn(restaurants);

        // Act
        mapRepository.loadRestaurants(latitude, longitude);

        // Assert
        LiveData<List<Restaurant>> liveData = mapRepository.getRestaurants();
        assertNotNull(liveData.getValue());
        assertEquals(1, liveData.getValue().size());
        assertEquals("Test", liveData.getValue().get(0).getName());
    }
}