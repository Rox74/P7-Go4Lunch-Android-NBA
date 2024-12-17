package com.nathba.go4lunch.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseUser;
import com.nathba.go4lunch.repository.AuthRepository;
import com.nathba.go4lunch.repository.MainRepository;

import org.junit.After;
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
    private AuthRepository authRepository;

    @Mock
    private FirebaseUser mockUser;

    @Mock
    private Context mockContext;

    private MainViewModel mainViewModel;

    private AutoCloseable closeable;

    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        // Configure the behavior of the mock repository
        MutableLiveData<FirebaseUser> liveDataUser = new MutableLiveData<>();
        liveDataUser.setValue(mockUser);

        when(mainRepository.getCurrentUser()).thenReturn(liveDataUser);

        mainViewModel = new MainViewModel(mainRepository, authRepository);
    }

    @After
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void getCurrentUser_shouldReturnCurrentUser() {
        // Act
        FirebaseUser currentUser = mainViewModel.getCurrentUser().getValue();

        // Assert
        assertNotNull("Current user should not be null", currentUser);
        verify(mainRepository).getCurrentUser();
    }

    @Test
    public void setSelectedNavigationItem_shouldUpdateLiveData() {
        // Arrange
        int navItemId = 1;

        // Act
        mainViewModel.setSelectedNavigationItem(navItemId);

        // Assert
        assertEquals("LiveData should update with navigation ID",
                navItemId,
                mainViewModel.getSelectedNavigationItem().getValue().intValue());
    }

    @Test
    public void checkLoginState_shouldInvokeRepositoryCheckLoginState() {
        // Act
        mainViewModel.checkLoginState();

        // Assert
        verify(mainRepository).checkLoginState();
    }

    @Test
    public void addWorkmateToFirestore_shouldCallRepositoryAddWorkmate() {
        // Act
        mainViewModel.addWorkmateToFirestore(mockUser);

        // Assert
        verify(mainRepository).addWorkmateToFirestore(mockUser);
    }

    @Test
    public void signOut_shouldCallRevokeAccessAndSignOut() {
        // Act
        mainViewModel.signOut(mockContext);

        // Assert
        verify(authRepository).revokeAccessAndSignOut(mockContext);
    }
}