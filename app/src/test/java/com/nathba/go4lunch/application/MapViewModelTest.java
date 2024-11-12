package com.nathba.go4lunch.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.location.Location;

import androidx.lifecycle.LiveData;

import com.nathba.go4lunch.models.Restaurant;
import com.nathba.go4lunch.repository.LunchRepository;
import com.nathba.go4lunch.repository.MapRepository;
import com.nathba.go4lunch.repository.RestaurantRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

import org.mockito.MockitoAnnotations;

public class MapViewModelTest {

    @Mock
    private MapRepository mapRepository;

    @Mock
    private LunchRepository lunchRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    private MapViewModel mapViewModel;

    @Mock
    Location mockLocation;

    private AutoCloseable closeable;

    @Before
    public void setup() {
        // Initialisation des mocks avec AutoCloseable
        closeable = MockitoAnnotations.openMocks(this);

        // Configuration des mocks pour la localisation
        when(mockLocation.getLatitude()).thenReturn(48.8566);
        when(mockLocation.getLongitude()).thenReturn(2.3522);

        // Initialisation de MapViewModel avec les repositories mockés
        mapViewModel = new MapViewModel(mapRepository, lunchRepository, restaurantRepository);
    }

    @After
    public void tearDown() throws Exception {
        // Fermeture des mocks pour libérer les ressources
        closeable.close();
    }

    @Test
    public void setUserLocation_shouldUpdateUserLocation() {
        // Met à jour la localisation de l'utilisateur avec le mock de Location
        mapViewModel.setUserLocation(mockLocation);

        // Vérifie que la localisation est bien mise à jour
        LiveData<Location> locationLiveData = mapViewModel.getUserLocation();
        assertNotNull(locationLiveData.getValue());
        assertEquals(mockLocation, locationLiveData.getValue());
    }

    @Test
    public void loadRestaurants_shouldCallRepository() {
        // Simule le chargement des restaurants avec des coordonnées
        double latitude = 48.8566;
        double longitude = 2.3522;

        mapViewModel.loadRestaurants(latitude, longitude);

        // Vérifie que la méthode du repository est appelée avec les bonnes coordonnées
        verify(mapRepository).loadRestaurants(latitude, longitude);
    }

    @Test
    public void getSelectedRestaurant_shouldReturnSelectedRestaurant() {
        // Simule la sélection d'un restaurant
        Restaurant restaurant = new Restaurant("1", "Le Jules Verne", "Paris", "", 4.5, new GeoPoint(48.858844, 2.294351), "", "", "", false);
        mapViewModel.fetchRestaurantDetails(restaurant.getRestaurantId(), restaurant.getLocation(), restaurant.getName());

        // Vérifie que le restaurant sélectionné est bien retourné
        LiveData<Restaurant> selectedRestaurant = mapViewModel.getSelectedRestaurant();
        assertNotNull(selectedRestaurant);
    }
}