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

public class NotificationRepositoryTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private FirebaseFirestore mockFirestore;

    @Mock
    private CollectionReference mockCollection;

    @Mock
    private Query mockQuery;

    @Mock
    private Observer<NotificationData> observer;

    private NotificationRepository notificationRepository;
    private AutoCloseable closeable;

    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        // Mock Firestore collection
        when(mockFirestore.collection("lunches")).thenReturn(mockCollection);

        // Initialize repository
        notificationRepository = new NotificationRepository(mockFirestore);
    }

    @After
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void getNotificationData_shouldReturnDataSuccessfully() {
        // Given
        String userId = "user123";
        Date today = getToday();

        // Mock Task<QuerySnapshot>
        Task<QuerySnapshot> mockTask = mock(Task.class);

        // Mock Firestore query
        when(mockCollection.whereEqualTo("workmateId", userId)).thenReturn(mockQuery);
        when(mockQuery.whereEqualTo("date", today)).thenReturn(mockQuery);
        when(mockQuery.get()).thenReturn(mockTask);

        // Simule un succès avec des données
        NotificationData mockData = new NotificationData("Restaurant123", "123 Rue de Paris", Arrays.asList("Alice", "Bob"));
        when(mockTask.isSuccessful()).thenReturn(true);

        QuerySnapshot mockQuerySnapshot = mock(QuerySnapshot.class);
        DocumentSnapshot mockDocumentSnapshot = mock(DocumentSnapshot.class);

        when(mockTask.getResult()).thenReturn(mockQuerySnapshot);
        when(mockQuerySnapshot.isEmpty()).thenReturn(false);
        when(mockQuerySnapshot.getDocuments()).thenReturn(Collections.singletonList(mockDocumentSnapshot));
        when(mockDocumentSnapshot.toObject(NotificationData.class)).thenReturn(mockData);

        // Simule l'exécution asynchrone du listener
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

    @Test
    public void getNotificationData_shouldReturnNullWhenNoDataFound() {
        // Given
        String userId = "user123";
        Date today = getToday();

        // Mock Task<QuerySnapshot>
        Task<QuerySnapshot> mockTask = mock(Task.class);

        // Mock Firestore query
        when(mockCollection.whereEqualTo("workmateId", userId)).thenReturn(mockQuery);
        when(mockQuery.whereEqualTo("date", today)).thenReturn(mockQuery);
        when(mockQuery.get()).thenReturn(mockTask);

        // Simule un succès mais sans données
        when(mockTask.isSuccessful()).thenReturn(true);
        QuerySnapshot mockQuerySnapshot = mock(QuerySnapshot.class);

        when(mockTask.getResult()).thenReturn(mockQuerySnapshot);
        when(mockQuerySnapshot.isEmpty()).thenReturn(true);

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

    @Test
    public void getNotificationData_shouldHandleFirestoreError() {
        // Given
        String userId = "user123";
        Date today = getToday();

        // Mock Task<QuerySnapshot>
        Task<QuerySnapshot> mockTask = mock(Task.class);

        // Mock Firestore query
        when(mockCollection.whereEqualTo("workmateId", userId)).thenReturn(mockQuery);
        when(mockQuery.whereEqualTo("date", today)).thenReturn(mockQuery);
        when(mockQuery.get()).thenReturn(mockTask);

        // Simule une erreur dans Firestore
        when(mockTask.isSuccessful()).thenReturn(false);

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
     * Réimplémentation locale de getToday()
     *
     * @return Date à minuit pour aujourd'hui
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