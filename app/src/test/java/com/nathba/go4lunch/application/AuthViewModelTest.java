package com.nathba.go4lunch.application;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseUser;
import com.nathba.go4lunch.repository.AuthRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit test class for the AuthViewModel.
 * Ensures proper interactions with the AuthRepository and validates ViewModel behavior.
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthViewModelTest {

    /**
     * Mocked instance of the AuthRepository to simulate authentication operations.
     */
    @Mock
    private AuthRepository authRepository;

    /**
     * Mocked instance of FirebaseUser to simulate a logged-in user.
     */
    @Mock
    private FirebaseUser mockUser;

    /**
     * Instance of AuthViewModel under test.
     */
    private AuthViewModel authViewModel;

    /**
     * Sets up the test environment by initializing the AuthViewModel with the mocked repository.
     */
    @Before
    public void setUp() {
        authViewModel = new AuthViewModel(authRepository);
    }

    /**
     * Verifies that the `signInWithCredential` method in the repository is called when the
     * corresponding ViewModel method is invoked.
     */
    @Test
    public void testSignInWithCredential_CallsRepositoryMethod() {
        // Arrange: Create a mock AuthCredential instance
        AuthCredential mockCredential = Mockito.mock(AuthCredential.class);

        // Act: Call the ViewModel method
        authViewModel.signInWithCredential(mockCredential);

        // Assert: Verify that the repository method was called with the same credential
        Mockito.verify(authRepository).signInWithCredential(mockCredential);
    }
}