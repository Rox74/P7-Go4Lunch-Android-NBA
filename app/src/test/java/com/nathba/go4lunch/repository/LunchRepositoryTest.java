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

public class LunchRepositoryTest {

    @Mock
    private FirebaseFirestore mockFirestore;

    @Mock
    private CollectionReference mockCollection;

    @Mock
    private QuerySnapshot mockQuerySnapshot;

    @Mock
    private DocumentSnapshot mockDocumentSnapshot;

    @Mock
    private Observer<List<Lunch>> observer;

    private LunchRepository lunchRepository;
    private AutoCloseable closeable;

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        when(mockFirestore.collection("lunches")).thenReturn(mockCollection);

        lunchRepository = new LunchRepository(mockFirestore);
    }

    @After
    public void tearDown() throws Exception {
        closeable.close();
    }

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