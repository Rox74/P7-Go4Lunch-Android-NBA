package com.nathba.go4lunch.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MainRepositoryTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private FirebaseAuth firebaseAuth;

    @Mock
    private FirebaseUser mockUser;

    private MainRepository mainRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        // Initialize the repository with the mocked FirebaseAuth instance
        mainRepository = new MainRepository(firebaseAuth);

        // Configure the behavior of the mocked FirebaseAuth
        when(firebaseAuth.getCurrentUser()).thenReturn(mockUser);
    }

    @Test
    public void checkLoginState_shouldUpdateCurrentUser() {
        // Act
        mainRepository.checkLoginState();

        // Assert
        assertEquals(mockUser, mainRepository.getCurrentUser().getValue());
    }
}