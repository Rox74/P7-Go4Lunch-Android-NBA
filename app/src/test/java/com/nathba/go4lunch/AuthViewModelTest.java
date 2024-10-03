package com.nathba.go4lunch;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseUser;
import com.nathba.go4lunch.application.AuthViewModel;
import com.nathba.go4lunch.repository.AuthRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class AuthViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private AuthRepository authRepository;

    @Mock
    private FirebaseUser firebaseUser;

    private AuthViewModel authViewModel;
    private MutableLiveData<FirebaseUser> userLiveData;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        userLiveData = new MutableLiveData<>();
        userLiveData.setValue(firebaseUser);

        when(authRepository.getUserLiveData()).thenReturn(userLiveData);

        authViewModel = new AuthViewModel(authRepository);
    }

    @Test
    public void signInWithCredential_callsRepositorySignIn() {
        AuthCredential credential = Mockito.mock(AuthCredential.class);
        authViewModel.signInWithCredential(credential);
        verify(authRepository).signInWithCredential(credential);
    }

    @Test
    public void getUserLiveData_returnsCurrentUser() {
        assertEquals(userLiveData.getValue(), firebaseUser);
    }

    @Test
    public void signOut_callsRepositorySignOut() {
        authViewModel.signOut();
        verify(authRepository).signOut();
    }
}