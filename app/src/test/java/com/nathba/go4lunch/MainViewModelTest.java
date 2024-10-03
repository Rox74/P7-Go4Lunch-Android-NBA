package com.nathba.go4lunch;

import static org.mockito.Mockito.verify;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.google.firebase.auth.FirebaseUser;
import com.nathba.go4lunch.application.MainViewModel;
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
    private FirebaseUser firebaseUser;

    private MainViewModel mainViewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mainViewModel = new MainViewModel(mainRepository);
    }

    @Test
    public void checkLoginState_callsRepositoryCheckLoginState() {
        mainViewModel.checkLoginState();
        verify(mainRepository).checkLoginState();
    }

    @Test
    public void signOut_callsRepositorySignOut() {
        mainViewModel.signOut();
        verify(mainRepository).signOut();
    }
}