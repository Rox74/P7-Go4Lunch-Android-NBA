package com.nathba.go4lunch.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AuthRepositoryTest {

    @Mock
    private FirebaseAuth firebaseAuth;

    @Mock
    private Task<AuthResult> mockTask;

    @Mock
    private FirebaseUser mockUser;

    private AuthRepository authRepository;

    @Before
    public void setUp() {
        authRepository = new AuthRepository(firebaseAuth);
    }

    @Test
    public void testSignInWithCredential_Success() {
        // Arrange
        AuthCredential credential = Mockito.mock(AuthCredential.class);
        Mockito.when(firebaseAuth.signInWithCredential(credential)).thenReturn(mockTask);
        Mockito.when(mockTask.isSuccessful()).thenReturn(true);
        Mockito.when(firebaseAuth.getCurrentUser()).thenReturn(mockUser);

        // Act
        authRepository.signInWithCredential(credential);

        // Assert
        assertEquals(mockUser, authRepository.getUserLiveData().getValue());
    }

    @Test
    public void testSignInWithCredential_Failure() {
        // Arrange
        AuthCredential credential = Mockito.mock(AuthCredential.class);
        Mockito.when(firebaseAuth.signInWithCredential(credential)).thenReturn(mockTask);
        Mockito.when(mockTask.isSuccessful()).thenReturn(false);

        // Act
        authRepository.signInWithCredential(credential);

        // Assert
        assertNull(authRepository.getUserLiveData().getValue());
    }

    @Test
    public void testSignOut_ClearsCurrentUser() {
        // Act
        authRepository.signOut();

        // Assert
        assertNull(authRepository.getUserLiveData().getValue());
    }
}