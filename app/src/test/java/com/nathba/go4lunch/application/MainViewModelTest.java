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

/**
 * Unit test class for MainViewModel.
 * Verifies the interaction between MainViewModel and its repositories.
 */
public class MainViewModelTest {

    /**
     * Rule to allow LiveData to execute synchronously during tests.
     */
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    /**
     * Mocked instance of MainRepository to simulate repository operations.
     */
    @Mock
    private MainRepository mainRepository;

    /**
     * Mocked instance of AuthRepository to handle authentication-related operations.
     */
    @Mock
    private AuthRepository authRepository;

    /**
     * Mocked instance of FirebaseUser representing a logged-in user.
     */
    @Mock
    private FirebaseUser mockUser;

    /**
     * Mocked instance of Context for repository operations requiring a context.
     */
    @Mock
    private Context mockContext;

    /**
     * Instance of MainViewModel under test.
     */
    private MainViewModel mainViewModel;

    /**
     * AutoCloseable resource to close mocks after each test.
     */
    private AutoCloseable closeable;

    /**
     * Sets up the test environment by initializing mocks and configuring repository behaviors.
     */
    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        // Configure the behavior of the mock repository
        MutableLiveData<FirebaseUser> liveDataUser = new MutableLiveData<>();
        liveDataUser.setValue(mockUser);

        when(mainRepository.getCurrentUser()).thenReturn(liveDataUser);

        mainViewModel = new MainViewModel(mainRepository, authRepository);
    }

    /**
     * Cleans up resources after each test.
     */
    @After
    public void tearDown() throws Exception {
        closeable.close();
    }

    /**
     * Verifies that `getCurrentUser` retrieves the current user from the repository.
     */
    @Test
    public void getCurrentUser_shouldReturnCurrentUser() {
        // Act
        FirebaseUser currentUser = mainViewModel.getCurrentUser().getValue();

        // Assert
        assertNotNull("Current user should not be null", currentUser);
        verify(mainRepository).getCurrentUser();
    }

    /**
     * Tests that setting the selected navigation item updates the corresponding LiveData.
     */
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

    /**
     * Verifies that `checkLoginState` invokes the repository method to check the user's login state.
     */
    @Test
    public void checkLoginState_shouldInvokeRepositoryCheckLoginState() {
        // Act
        mainViewModel.checkLoginState();

        // Assert
        verify(mainRepository).checkLoginState();
    }

    /**
     * Verifies that adding a workmate to Firestore calls the appropriate repository method.
     */
    @Test
    public void addWorkmateToFirestore_shouldCallRepositoryAddWorkmate() {
        // Act
        mainViewModel.addWorkmateToFirestore(mockUser);

        // Assert
        verify(mainRepository).addWorkmateToFirestore(mockUser);
    }

    /**
     * Verifies that signing out calls the repository method to revoke access and sign out.
     */
    @Test
    public void signOut_shouldCallRevokeAccessAndSignOut() {
        // Act
        mainViewModel.signOut(mockContext);

        // Assert
        verify(authRepository).revokeAccessAndSignOut(mockContext);
    }
}