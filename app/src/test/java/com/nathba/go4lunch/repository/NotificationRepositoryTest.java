package com.nathba.go4lunch.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import androidx.lifecycle.LiveData;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.nathba.go4lunch.models.NotificationData;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

@RunWith(MockitoJUnitRunner.class)
public class NotificationRepositoryTest {

    @Mock
    private FirebaseFirestore firestore;

    @Mock
    private CollectionReference collectionReference;

    @Mock
    private Query query;

    @Mock
    private Task<QuerySnapshot> task;

    private NotificationRepository notificationRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        notificationRepository = new NotificationRepository(firestore);
        when(firestore.collection("lunches")).thenReturn(collectionReference);
    }

    @Test
    public void getNotificationData_shouldReturnData_whenLunchExists() {
        // Arrange
        String userId = "testUser";
        Date today = new Date();
        NotificationData mockData = new NotificationData("Test Restaurant", "123 Test St", Arrays.asList("Alice", "Bob"));

        DocumentSnapshot documentSnapshot = Mockito.mock(DocumentSnapshot.class);
        when(documentSnapshot.toObject(NotificationData.class)).thenReturn(mockData);

        QuerySnapshot querySnapshot = Mockito.mock(QuerySnapshot.class);
        when(querySnapshot.getDocuments()).thenReturn(Collections.singletonList(documentSnapshot));

        when(collectionReference.whereEqualTo("workmateId", userId)).thenReturn(query);
        when(query.whereEqualTo("date", today)).thenReturn(query);
        when(query.get()).thenReturn(task);
        when(task.isSuccessful()).thenReturn(true);
        when(task.getResult()).thenReturn(querySnapshot);

        // Act
        LiveData<NotificationData> liveData = notificationRepository.getNotificationData(userId);

        // Assert
        liveData.observeForever(data -> {
            assertNotNull(data);
            assertEquals("Test Restaurant", data.getRestaurantName());
            assertEquals("123 Test St", data.getRestaurantAddress());
            assertEquals(2, data.getColleaguesNames().size());
        });
    }
}