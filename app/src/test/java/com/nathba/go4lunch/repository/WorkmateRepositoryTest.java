package com.nathba.go4lunch.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import androidx.lifecycle.LiveData;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.nathba.go4lunch.models.Workmate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class WorkmateRepositoryTest {

    @Mock
    private FirebaseFirestore firestore;

    @Mock
    private CollectionReference workmatesCollection;

    @Mock
    private DocumentReference documentReference;

    @Mock
    private Task<DocumentSnapshot> documentTask;

    @Mock
    private DocumentSnapshot documentSnapshot;

    private WorkmateRepository workmateRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        workmateRepository = new WorkmateRepository(firestore);
        when(firestore.collection("workmates")).thenReturn(workmatesCollection);
    }

    @Test
    public void getWorkmateById_shouldReturnWorkmate() {
        // Arrange
        String workmateId = "123";
        Workmate mockWorkmate = new Workmate("123", "Alice", "alice@example.com", "url");

        when(workmatesCollection.document(workmateId)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(documentTask);
        when(documentTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            OnSuccessListener<DocumentSnapshot> listener = invocation.getArgument(0);
            when(documentSnapshot.toObject(Workmate.class)).thenReturn(mockWorkmate);
            listener.onSuccess(documentSnapshot);
            return documentTask;
        });

        // Act
        LiveData<Workmate> liveData = workmateRepository.getWorkmateById(workmateId);

        // Assert
        liveData.observeForever(retrievedWorkmate -> {
            assertNotNull(retrievedWorkmate);
            assertEquals("Alice", retrievedWorkmate.getName());
        });
    }
}