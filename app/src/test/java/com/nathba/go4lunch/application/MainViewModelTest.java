package com.nathba.go4lunch.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseUser;
import com.nathba.go4lunch.repository.MainRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MainViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private MainRepository mainRepository;

    @Mock
    private FirebaseUser mockUser;

    private MainViewModel mainViewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        // Configure the behavior of the mock repository
        MutableLiveData<FirebaseUser> liveDataUser = new MutableLiveData<>();
        liveDataUser.setValue(mockUser);

        when(mainRepository.getCurrentUser()).thenReturn(liveDataUser);

        mainViewModel = new MainViewModel(mainRepository);
    }

    @Test
    public void getCurrentUser_shouldReturnCurrentUser() {
        // Act
        FirebaseUser currentUser = mainViewModel.getCurrentUser().getValue();

        // Assert
        assertNotNull(currentUser);
        verify(mainRepository).getCurrentUser();
    }

    @Test
    public void setSelectedNavigationItem_shouldUpdateLiveData() {
        // Arrange
        int navItemId = 1;

        // Act
        mainViewModel.setSelectedNavigationItem(navItemId);

        // Assert
        assertEquals(navItemId, mainViewModel.getSelectedNavigationItem().getValue().intValue());
    }

    @Test
    public void signOut_shouldCallRepositorySignOut() {
        // Act
        mainViewModel.signOut();

        // Assert
        verify(mainRepository).signOut();
    }

    @Test
    public void checkLoginState_shouldInvokeRepositoryCheckLoginState() {
        // Act
        mainViewModel.checkLoginState();

        // Assert
        verify(mainRepository).checkLoginState();
    }
}