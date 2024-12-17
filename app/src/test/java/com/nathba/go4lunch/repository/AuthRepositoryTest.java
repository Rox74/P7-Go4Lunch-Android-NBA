package com.nathba.go4lunch.repository;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AuthRepositoryTest {

    @Mock
    private FirebaseAuth firebaseAuth;

    @Mock
    private FirebaseUser mockUser;

    @Mock
    private AuthCredential authCredential;

    @Mock
    private GoogleSignInClient googleSignInClient;

    @Mock
    private Task<AuthResult> mockSignInTask;

    @Mock
    private Task<Void> mockRevokeTask;

    @Mock
    private Context mockContext;

    @Mock
    private Observer<FirebaseUser> userObserver;

    private AuthRepository authRepository;

    private AutoCloseable closeable;

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        // Mock initial user state
        when(firebaseAuth.getCurrentUser()).thenReturn(mockUser);

        // Initialize repository
        authRepository = new AuthRepository(firebaseAuth);
    }

    @After
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void getUserLiveData_shouldReturnInitialUser() {
        // Observe userLiveData
        authRepository.getUserLiveData().observeForever(userObserver);

        // Verify initial user is posted
        verify(userObserver).onChanged(mockUser);
    }

    @Test
    public void signInWithCredential_shouldPostUserOnSuccess() {
        // Given
        when(firebaseAuth.signInWithCredential(authCredential)).thenReturn(mockSignInTask);
        when(mockSignInTask.isSuccessful()).thenReturn(true);
        when(firebaseAuth.getCurrentUser()).thenReturn(mockUser);

        // Simuler la réussite de signInWithCredential
        doAnswer(invocation -> {
            OnCompleteListener<AuthResult> listener = invocation.getArgument(0);
            listener.onComplete(mockSignInTask);
            return null;
        }).when(mockSignInTask).addOnCompleteListener(any());

        // Capturer les valeurs émises
        ArgumentCaptor<FirebaseUser> captor = ArgumentCaptor.forClass(FirebaseUser.class);

        // Observer LiveData
        authRepository.getUserLiveData().observeForever(userObserver);

        // Act
        authRepository.signInWithCredential(authCredential);

        // Then
        verify(userObserver, atLeastOnce()).onChanged(captor.capture());

        // Assurer que la valeur attendue est émise
        assertEquals(mockUser, captor.getValue());
    }

    @Test
    public void signInWithCredential_shouldPostNullOnFailure() {
        // Given
        when(firebaseAuth.signInWithCredential(authCredential)).thenReturn(mockSignInTask);
        when(mockSignInTask.isSuccessful()).thenReturn(false);

        // Simulate sign-in failure
        doAnswer(invocation -> {
            OnCompleteListener<AuthResult> listener = invocation.getArgument(0);
            listener.onComplete(mockSignInTask);
            return null;
        }).when(mockSignInTask).addOnCompleteListener(any());

        // Observe userLiveData
        authRepository.getUserLiveData().observeForever(userObserver);

        // Act
        authRepository.signInWithCredential(authCredential);

        // Verify user is null
        verify(userObserver).onChanged(null);
    }
}