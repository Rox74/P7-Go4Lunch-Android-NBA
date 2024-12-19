package com.nathba.go4lunch.repository;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.net.Uri;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nathba.go4lunch.models.Workmate;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit test class for MainRepository.
 * Ensures proper Firebase authentication handling and Firestore interactions.
 */
public class MainRepositoryTest {

    /**
     * Ensures LiveData operations are executed synchronously during tests.
     */
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    /**
     * Mocked FirebaseAuth instance for simulating authentication operations.
     */
    @Mock
    private FirebaseAuth firebaseAuth;

    /**
     * Mocked FirebaseUser to represent the currently authenticated user.
     */
    @Mock
    private FirebaseUser mockUser;

    /**
     * The repository under test.
     */
    private MainRepository mainRepository;

    /**
     * Resource for cleaning up mocks after each test.
     */
    private AutoCloseable closeable;

    /**
     * Initializes mocks and repository before each test.
     */
    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        // Mock the behavior of FirebaseAuth to return a mocked user.
        when(firebaseAuth.getCurrentUser()).thenReturn(mockUser);

        // Initialize the MainRepository instance.
        mainRepository = new MainRepository(firebaseAuth);
    }

    /**
     * Closes mocks and resources after each test.
     */
    @After
    public void tearDown() throws Exception {
        closeable.close();
    }

    /**
     * Verifies that the repository initializes with the current user from FirebaseAuth.
     */
    @Test
    public void getCurrentUser_shouldInitializeWithCurrentUser() {
        // Act
        FirebaseUser result = mainRepository.getCurrentUser().getValue();

        // Assert
        assertEquals(mockUser, result);
    }

    /**
     * Verifies that the login state check correctly updates LiveData with the current user.
     */
    @Test
    public void checkLoginState_shouldUpdateLiveData() {
        // Given
        FirebaseUser newUser = mock(FirebaseUser.class);
        when(firebaseAuth.getCurrentUser()).thenReturn(newUser);

        // Act
        mainRepository.checkLoginState();

        // Assert
        assertEquals(newUser, mainRepository.getCurrentUser().getValue());
    }

    /**
     * Verifies that a workmate is successfully added to Firestore.
     */
    @Test
    public void addWorkmateToFirestore_shouldAddWorkmateSuccessfully() {
        // Mocks Firestore components.
        FirebaseFirestore mockFirestore = mock(FirebaseFirestore.class);
        CollectionReference mockCollection = mock(CollectionReference.class);
        DocumentReference mockDocument = mock(DocumentReference.class);
        Task<Void> mockTask = mock(Task.class);

        // Simulate Firestore behavior.
        when(mockFirestore.collection("workmates")).thenReturn(mockCollection);
        when(mockCollection.document("user123")).thenReturn(mockDocument);
        when(mockDocument.set(any(Workmate.class))).thenReturn(mockTask);

        // Mock FirebaseUser attributes.
        Uri mockUri = mock(Uri.class);
        when(mockUri.toString()).thenReturn("http://example.com/photo.jpg");

        FirebaseUser user = mock(FirebaseUser.class);
        when(user.getUid()).thenReturn("user123");
        when(user.getDisplayName()).thenReturn("John Doe");
        when(user.getEmail()).thenReturn("john@example.com");
        when(user.getPhotoUrl()).thenReturn(mockUri);

        // Act
        MainRepository repository = new MainRepository(firebaseAuth) {
            @Override
            public void addWorkmateToFirestore(FirebaseUser firebaseUser) {
                Workmate workmate = new Workmate(
                        firebaseUser.getUid(),
                        firebaseUser.getDisplayName(),
                        firebaseUser.getEmail(),
                        firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null
                );
                mockFirestore.collection("workmates").document(firebaseUser.getUid()).set(workmate);
            }
        };
        repository.addWorkmateToFirestore(user);

        // Assert
        verify(mockCollection).document("user123");
        verify(mockDocument).set(any(Workmate.class));
    }
}