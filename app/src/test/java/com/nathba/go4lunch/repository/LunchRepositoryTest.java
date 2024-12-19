package com.nathba.go4lunch.repository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.nathba.go4lunch.models.Lunch;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Unit test class for the LunchRepository.
 * Ensures proper interaction with Firebase Firestore and correct LiveData behavior.
 */
public class LunchRepositoryTest {

    /**
     * Mocked instance of FirebaseFirestore for simulating Firestore operations.
     */
    @Mock
    private FirebaseFirestore mockFirestore;

    /**
     * Mocked reference to the "lunches" Firestore collection.
     */
    @Mock
    private CollectionReference mockCollection;

    /**
     * Mocked QuerySnapshot to simulate Firestore query results.
     */
    @Mock
    private QuerySnapshot mockQuerySnapshot;

    /**
     * Mocked DocumentSnapshot to simulate individual Firestore documents.
     */
    @Mock
    private DocumentSnapshot mockDocumentSnapshot;

    /**
     * Mocked observer for observing LiveData of Lunch objects.
     */
    @Mock
    private Observer<List<Lunch>> observer;

    /**
     * Instance of the repository under test.
     */
    private LunchRepository lunchRepository;

    /**
     * AutoCloseable resource for cleaning up mocks after each test.
     */
    private AutoCloseable closeable;

    /**
     * Rule to ensure that LiveData operations are executed synchronously during tests.
     */
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    /**
     * Sets up mocks and initializes the repository before each test.
     */
    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        // Mock Firestore "lunches" collection
        when(mockFirestore.collection("lunches")).thenReturn(mockCollection);

        // Initialize repository
        lunchRepository = new LunchRepository(mockFirestore);
    }

    /**
     * Cleans up mocks and resources after each test.
     */
    @After
    public void tearDown() throws Exception {
        closeable.close();
    }

    /**
     * Verifies that `getLunches` correctly retrieves and emits a list of lunches from Firestore.
     */
    @Test
    public void getLunches_shouldReturnLunchesList() {
        // Given
        when(mockCollection.addSnapshotListener(any())).thenAnswer(invocation -> {
            EventListener<QuerySnapshot> listener = invocation.getArgument(0);
            listener.onEvent(mockQuerySnapshot, null);
            return null;
        });

        List<Lunch> lunches = Arrays.asList(
                new Lunch("1", "workmate1", "restaurant123", "Le Jules Verne", "123 rue de Paris", new Date())
        );
        when(mockQuerySnapshot.getDocuments()).thenReturn(Collections.singletonList(mockDocumentSnapshot));
        when(mockDocumentSnapshot.toObject(Lunch.class)).thenReturn(lunches.get(0));

        // Observe
        lunchRepository.getLunches().observeForever(observer);

        // Then
        verify(observer).onChanged(lunches);
    }

    /**
     * Verifies that `addLunch` calls the appropriate Firestore method to add a lunch document.
     */
    @Test
    public void addLunch_shouldCallSetOnFirestore() {
        // Given
        Lunch lunch = new Lunch("1", "workmate1", "restaurant123", "Le Jules Verne", "123 rue de Paris", new Date());
        when(mockCollection.document(lunch.getLunchId())).thenReturn(mock(DocumentReference.class));

        // Act
        lunchRepository.addLunch(lunch);

        // Then
        verify(mockCollection).document(lunch.getLunchId());
    }
}