package com.nathba.go4lunch.repository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.nathba.go4lunch.models.NotificationData;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

/**
 * Unit test class for NotificationRepository.
 * Verifies the behavior of Firestore queries to fetch notification data.
 */
public class NotificationRepositoryTest {

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
     * Mocked CollectionReference for accessing the "lunches" collection.
     */
    @Mock
    private CollectionReference mockCollection;

    /**
     * Mocked Query object for simulating filtered Firestore queries.
     */
    @Mock
    private Query mockQuery;

    /**
     * Observer for observing LiveData changes during tests.
     */
    @Mock
    private Observer<NotificationData> observer;

    /**
     * The repository under test.
     */
    private NotificationRepository notificationRepository;

    /**
     * Resource for cleaning up mocks after each test.
     */
    private AutoCloseable closeable;

    /**
     * Initializes mocks and the repository before each test.
     */
    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        // Mock the Firestore collection
        when(mockFirestore.collection("lunches")).thenReturn(mockCollection);

        // Initialize the repository
        notificationRepository = new NotificationRepository(mockFirestore);
    }

    /**
     * Closes mocks and resources after each test.
     */
    @After
    public void tearDown() throws Exception {
        closeable.close();
    }

    /**
     * Verifies that notification data is returned successfully from Firestore.
     */
    @Test
    public void getNotificationData_shouldReturnDataSuccessfully() {
        // Given
        String userId = "user123";
        Date today = getToday();

        // Mock Firestore query behavior and data
        Task<QuerySnapshot> mockTask = mock(Task.class);
        NotificationData mockData = new NotificationData("Restaurant123", "123 Rue de Paris", Arrays.asList("Alice", "Bob"));
        QuerySnapshot mockQuerySnapshot = mock(QuerySnapshot.class);
        DocumentSnapshot mockDocumentSnapshot = mock(DocumentSnapshot.class);

        when(mockCollection.whereEqualTo("workmateId", userId)).thenReturn(mockQuery);
        when(mockQuery.whereEqualTo("date", today)).thenReturn(mockQuery);
        when(mockQuery.get()).thenReturn(mockTask);
        when(mockTask.isSuccessful()).thenReturn(true);
        when(mockTask.getResult()).thenReturn(mockQuerySnapshot);
        when(mockQuerySnapshot.isEmpty()).thenReturn(false);
        when(mockQuerySnapshot.getDocuments()).thenReturn(Collections.singletonList(mockDocumentSnapshot));
        when(mockDocumentSnapshot.toObject(NotificationData.class)).thenReturn(mockData);

        // Simulate Firestore listener execution
        doAnswer(invocation -> {
            OnCompleteListener<QuerySnapshot> listener = invocation.getArgument(0);
            listener.onComplete(mockTask);
            return null;
        }).when(mockTask).addOnCompleteListener(any());

        // Observe LiveData
        LiveData<NotificationData> liveData = notificationRepository.getNotificationData(userId);
        liveData.observeForever(observer);

        // Then
        verify(observer).onChanged(mockData);
    }

    /**
     * Verifies that null is returned when no data is found in Firestore.
     */
    @Test
    public void getNotificationData_shouldReturnNullWhenNoDataFound() {
        // Given
        String userId = "user123";
        Date today = getToday();

        // Mock Firestore query behavior with no results
        Task<QuerySnapshot> mockTask = mock(Task.class);
        QuerySnapshot mockQuerySnapshot = mock(QuerySnapshot.class);

        when(mockCollection.whereEqualTo("workmateId", userId)).thenReturn(mockQuery);
        when(mockQuery.whereEqualTo("date", today)).thenReturn(mockQuery);
        when(mockQuery.get()).thenReturn(mockTask);
        when(mockTask.isSuccessful()).thenReturn(true);
        when(mockTask.getResult()).thenReturn(mockQuerySnapshot);
        when(mockQuerySnapshot.isEmpty()).thenReturn(true);

        // Simulate Firestore listener execution
        doAnswer(invocation -> {
            OnCompleteListener<QuerySnapshot> listener = invocation.getArgument(0);
            listener.onComplete(mockTask);
            return null;
        }).when(mockTask).addOnCompleteListener(any());

        // Observe LiveData
        LiveData<NotificationData> liveData = notificationRepository.getNotificationData(userId);
        liveData.observeForever(observer);

        // Then
        verify(observer).onChanged(null);
    }

    /**
     * Verifies that errors in Firestore are handled gracefully and null is returned.
     */
    @Test
    public void getNotificationData_shouldHandleFirestoreError() {
        // Given
        String userId = "user123";
        Date today = getToday();

        // Mock Firestore query behavior with an error
        Task<QuerySnapshot> mockTask = mock(Task.class);

        when(mockCollection.whereEqualTo("workmateId", userId)).thenReturn(mockQuery);
        when(mockQuery.whereEqualTo("date", today)).thenReturn(mockQuery);
        when(mockQuery.get()).thenReturn(mockTask);
        when(mockTask.isSuccessful()).thenReturn(false);

        // Simulate Firestore listener execution
        doAnswer(invocation -> {
            OnCompleteListener<QuerySnapshot> listener = invocation.getArgument(0);
            listener.onComplete(mockTask);
            return null;
        }).when(mockTask).addOnCompleteListener(any());

        // Observe LiveData
        LiveData<NotificationData> liveData = notificationRepository.getNotificationData(userId);
        liveData.observeForever(observer);

        // Then
        verify(observer).onChanged(null);
    }

    /**
     * Utility method to get the current date set to midnight.
     *
     * @return Date representing today at midnight.
     */
    private Date getToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}