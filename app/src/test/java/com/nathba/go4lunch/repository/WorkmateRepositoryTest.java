package com.nathba.go4lunch.repository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.nathba.go4lunch.models.Workmate;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

/**
 * Unit test class for WorkmateRepository.
 * Validates interactions with Firestore for managing workmates data.
 */
public class WorkmateRepositoryTest {

    /**
     * Ensures LiveData operations are executed synchronously during tests.
     */
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    /**
     * Mocked Firestore instance for simulating database operations.
     */
    @Mock
    private FirebaseFirestore mockFirestore;

    /**
     * Mocked CollectionReference for the "workmates" collection.
     */
    @Mock
    private CollectionReference mockCollection;

    /**
     * Mocked DocumentReference for simulating document-level Firestore operations.
     */
    @Mock
    private DocumentReference mockDocumentReference;

    /**
     * Mocked Task for simulating Firestore operations.
     */
    @Mock
    private Task<DocumentSnapshot> mockDocumentTask;

    /**
     * Mocked Task for void operations in Firestore.
     */
    @Mock
    private Task<Void> mockVoidTask;

    /**
     * Instance of WorkmateRepository being tested.
     */
    private WorkmateRepository workmateRepository;

    /**
     * Resource for cleaning up mocks after each test.
     */
    private AutoCloseable closeable;

    /**
     * Sets up the test environment by initializing mocks and the repository.
     */
    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        when(mockFirestore.collection("workmates")).thenReturn(mockCollection);
        workmateRepository = new WorkmateRepository(mockFirestore);
    }

    /**
     * Cleans up resources and mocks after each test.
     */
    @After
    public void tearDown() throws Exception {
        closeable.close();
    }

    /**
     * Tests that `getWorkmates` retrieves a list of workmates successfully.
     */
    @Test
    public void getWorkmates_shouldReturnWorkmatesSuccessfully() {
        // Given
        MutableLiveData<List<Workmate>> liveData = new MutableLiveData<>();
        List<Workmate> mockWorkmates = Arrays.asList(
                new Workmate("1", "John Doe", "john@example.com", "http://photo.com/john"),
                new Workmate("2", "Jane Doe", "jane@example.com", "http://photo.com/jane")
        );

        QuerySnapshot mockSnapshot = mock(QuerySnapshot.class);
        DocumentSnapshot mockDoc1 = mock(DocumentSnapshot.class);
        DocumentSnapshot mockDoc2 = mock(DocumentSnapshot.class);

        when(mockDoc1.toObject(Workmate.class)).thenReturn(mockWorkmates.get(0));
        when(mockDoc2.toObject(Workmate.class)).thenReturn(mockWorkmates.get(1));
        when(mockSnapshot.getDocuments()).thenReturn(Arrays.asList(mockDoc1, mockDoc2));

        doAnswer(invocation -> {
            EventListener<QuerySnapshot> listener = invocation.getArgument(0);
            listener.onEvent(mockSnapshot, null);
            return null;
        }).when(mockCollection).addSnapshotListener(any());

        // Observe the LiveData
        LiveData<List<Workmate>> result = workmateRepository.getWorkmates();
        Observer<List<Workmate>> observer = mock(Observer.class);
        result.observeForever(observer);

        // Then
        verify(observer).onChanged(mockWorkmates);
    }

    /**
     * Tests that `addWorkmate` correctly interacts with Firestore.
     */
    @Test
    public void addWorkmate_shouldInvokeFirestoreSet() {
        // Given
        Workmate workmate = new Workmate("1", "John Doe", "john@example.com", "http://photo.com/john");
        when(mockCollection.document(workmate.getWorkmateId())).thenReturn(mockDocumentReference);
        when(mockDocumentReference.set(workmate)).thenReturn(mockVoidTask);

        // Act
        workmateRepository.addWorkmate(workmate);

        // Then
        verify(mockDocumentReference).set(workmate);
    }

    /**
     * Tests that `getWorkmateById` retrieves a specific workmate successfully.
     */
    @Test
    public void getWorkmateById_shouldReturnWorkmateSuccessfully() {
        // Given
        String workmateId = "1";
        Workmate mockWorkmate = new Workmate(workmateId, "John Doe", "john@example.com", "http://photo.com/john");

        Task<DocumentSnapshot> mockTask = mock(Task.class);
        DocumentSnapshot mockDocumentSnapshot = mock(DocumentSnapshot.class);

        when(mockCollection.document(workmateId)).thenReturn(mockDocumentReference);
        when(mockDocumentReference.get()).thenReturn(mockTask);

        // Simulate success
        doAnswer(invocation -> {
            OnSuccessListener<DocumentSnapshot> listener = invocation.getArgument(0);
            when(mockDocumentSnapshot.exists()).thenReturn(true);
            when(mockDocumentSnapshot.toObject(Workmate.class)).thenReturn(mockWorkmate);
            listener.onSuccess(mockDocumentSnapshot);
            return mockTask;
        }).when(mockTask).addOnSuccessListener(any());

        // Observe LiveData
        LiveData<Workmate> result = workmateRepository.getWorkmateById(workmateId);
        Observer<Workmate> observer = mock(Observer.class);
        result.observeForever(observer);

        // Then
        verify(observer).onChanged(mockWorkmate);
    }

    /**
     * Tests that `getWorkmateById` returns null when no workmate is found.
     */
    @Test
    public void getWorkmateById_shouldReturnNullWhenWorkmateNotFound() {
        // Given
        String workmateId = "1";

        Task<DocumentSnapshot> mockTask = mock(Task.class);
        DocumentSnapshot mockDocumentSnapshot = mock(DocumentSnapshot.class);

        when(mockCollection.document(workmateId)).thenReturn(mockDocumentReference);
        when(mockDocumentReference.get()).thenReturn(mockTask);

        // Simulate success but no document found
        doAnswer(invocation -> {
            OnSuccessListener<DocumentSnapshot> listener = invocation.getArgument(0);
            when(mockDocumentSnapshot.exists()).thenReturn(false);
            listener.onSuccess(mockDocumentSnapshot);
            return mockTask;
        }).when(mockTask).addOnSuccessListener(any());

        // Observe LiveData
        LiveData<Workmate> result = workmateRepository.getWorkmateById(workmateId);
        Observer<Workmate> observer = mock(Observer.class);
        result.observeForever(observer);

        // Then
        verify(observer).onChanged(null);
    }

    /**
     * Tests that `getWorkmateById` handles Firestore errors gracefully.
     */
    @Test
    public void getWorkmateById_shouldHandleFirestoreError() {
        // Given
        String workmateId = "1";

        // Mock Task<DocumentSnapshot>
        Task<DocumentSnapshot> mockTask = mock(Task.class);

        when(mockCollection.document(workmateId)).thenReturn(mockDocumentReference);
        when(mockDocumentReference.get()).thenReturn(mockTask);

        // Simulate Firestore failure with an exception
        doAnswer(invocation -> {
            OnFailureListener failureListener = invocation.getArgument(0);
            failureListener.onFailure(new Exception("Firestore error"));
            return mockTask;
        }).when(mockTask).addOnFailureListener(any());

        doAnswer(invocation -> mockTask).when(mockTask).addOnSuccessListener(any());

        // Observe LiveData
        LiveData<Workmate> result = workmateRepository.getWorkmateById(workmateId);
        Observer<Workmate> observer = mock(Observer.class);
        result.observeForever(observer);

        // Then
        verify(observer).onChanged(null);
    }
}