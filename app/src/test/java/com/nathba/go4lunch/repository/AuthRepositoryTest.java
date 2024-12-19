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

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
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

/**
 * Unit test class for the AuthRepository.
 * Verifies interactions with FirebaseAuth and LiveData behavior.
 */
public class AuthRepositoryTest {

    /**
     * Mocked instance of FirebaseAuth for authentication operations.
     */
    @Mock
    private FirebaseAuth firebaseAuth;

    /**
     * Mocked FirebaseUser representing a signed-in user.
     */
    @Mock
    private FirebaseUser mockUser;

    /**
     * Mocked AuthCredential used for signing in.
     */
    @Mock
    private AuthCredential authCredential;

    /**
     * Mocked GoogleSignInClient for Google authentication.
     */
    @Mock
    private GoogleSignInClient googleSignInClient;

    /**
     * Mocked Task for sign-in operations.
     */
    @Mock
    private Task<AuthResult> mockSignInTask;

    /**
     * Mocked Task for revoking access.
     */
    @Mock
    private Task<Void> mockRevokeTask;

    /**
     * Mocked Context used for authentication-related operations.
     */
    @Mock
    private Context mockContext;

    /**
     * Mocked observer for observing user LiveData.
     */
    @Mock
    private Observer<FirebaseUser> userObserver;

    /**
     * Instance of the repository under test.
     */
    private AuthRepository authRepository;

    /**
     * AutoCloseable resource for cleaning up mocks after each test.
     */
    private AutoCloseable closeable;

    /**
     * Rule for running LiveData-related tasks synchronously.
     */
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    /**
     * Sets up mocks and initializes the repository before each test.
     */
    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        // Mock initial user state
        when(firebaseAuth.getCurrentUser()).thenReturn(mockUser);

        // Initialize repository
        authRepository = new AuthRepository(firebaseAuth);
    }

    /**
     * Cleans up mocks and resources after each test.
     */
    @After
    public void tearDown() throws Exception {
        closeable.close();
    }

    /**
     * Tests that `getUserLiveData` initially posts the current user.
     */
    @Test
    public void getUserLiveData_shouldReturnInitialUser() {
        // Observe userLiveData
        authRepository.getUserLiveData().observeForever(userObserver);

        // Verify initial user is posted
        verify(userObserver).onChanged(mockUser);
    }

    /**
     * Tests that `signInWithCredential` posts the user to LiveData upon successful sign-in.
     */
    @Test
    public void signInWithCredential_shouldPostUserOnSuccess() {
        // Given
        when(firebaseAuth.signInWithCredential(authCredential)).thenReturn(mockSignInTask);
        when(mockSignInTask.isSuccessful()).thenReturn(true);
        when(firebaseAuth.getCurrentUser()).thenReturn(mockUser);

        // Simulate successful sign-in
        doAnswer(invocation -> {
            OnCompleteListener<AuthResult> listener = invocation.getArgument(0);
            listener.onComplete(mockSignInTask);
            return null;
        }).when(mockSignInTask).addOnCompleteListener(any());

        // Capture emitted values
        ArgumentCaptor<FirebaseUser> captor = ArgumentCaptor.forClass(FirebaseUser.class);

        // Observe LiveData
        authRepository.getUserLiveData().observeForever(userObserver);

        // Act
        authRepository.signInWithCredential(authCredential);

        // Then
        verify(userObserver, atLeastOnce()).onChanged(captor.capture());

        // Ensure the expected value is emitted
        assertEquals(mockUser, captor.getValue());
    }

    /**
     * Tests that `signInWithCredential` posts null to LiveData upon failure.
     */
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