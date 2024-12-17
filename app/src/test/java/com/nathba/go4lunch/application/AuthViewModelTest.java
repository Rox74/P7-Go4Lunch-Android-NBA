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

@RunWith(MockitoJUnitRunner.class)
public class AuthViewModelTest {

    @Mock
    private AuthRepository authRepository;

    @Mock
    private FirebaseUser mockUser;

    private AuthViewModel authViewModel;

    @Before
    public void setUp() {
        authViewModel = new AuthViewModel(authRepository);
    }

    @Test
    public void testSignInWithCredential_CallsRepositoryMethod() {
        // Arrange
        AuthCredential mockCredential = Mockito.mock(AuthCredential.class);

        // Act
        authViewModel.signInWithCredential(mockCredential);

        // Assert
        Mockito.verify(authRepository).signInWithCredential(mockCredential);
    }
}