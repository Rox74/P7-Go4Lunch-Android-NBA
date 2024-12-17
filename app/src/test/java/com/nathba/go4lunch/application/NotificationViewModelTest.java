package com.nathba.go4lunch.application;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.nathba.go4lunch.models.NotificationData;
import com.nathba.go4lunch.repository.NotificationRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

public class NotificationViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private Observer<NotificationData> notificationDataObserver;

    private NotificationViewModel notificationViewModel;

    private AutoCloseable closeable;

    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        notificationViewModel = new NotificationViewModel(notificationRepository);
    }

    @After
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void getNotificationData_shouldReturnDataSuccessfully() {
        // Given
        String userId = "user123";
        NotificationData mockNotificationData = new NotificationData(
                "Le Jules Verne", "123 rue de Paris", Arrays.asList("Alice", "Bob")
        );

        MutableLiveData<NotificationData> liveData = new MutableLiveData<>(mockNotificationData);
        when(notificationRepository.getNotificationData(userId)).thenReturn(liveData);

        // Observe the LiveData
        notificationViewModel.getNotificationData(userId).observeForever(notificationDataObserver);

        // When
        LiveData<NotificationData> result = notificationViewModel.getNotificationData(userId);

        // Then
        assertNotNull("LiveData should not be null", result);
        verify(notificationRepository).getNotificationData(userId);
        verify(notificationDataObserver).onChanged(mockNotificationData);
    }

    @Test
    public void getNotificationData_shouldNotFetchAgainIfAlreadyFetched() {
        // Given
        String userId = "user123";
        NotificationData mockNotificationData = new NotificationData(
                "Le Jules Verne", "123 rue de Paris", Arrays.asList("Alice", "Bob")
        );

        MutableLiveData<NotificationData> liveData = new MutableLiveData<>(mockNotificationData);
        when(notificationRepository.getNotificationData(userId)).thenReturn(liveData);

        // First call
        notificationViewModel.getNotificationData(userId).observeForever(notificationDataObserver);
        notificationViewModel.getNotificationData(userId);

        // Verify repository called only once
        verify(notificationRepository, times(1)).getNotificationData(userId);
    }

    @Test
    public void getNotificationData_withNullData_shouldHandleGracefully() {
        // Given
        String userId = "user123";
        MutableLiveData<NotificationData> liveData = new MutableLiveData<>(null);
        when(notificationRepository.getNotificationData(userId)).thenReturn(liveData);

        // Observe the LiveData
        notificationViewModel.getNotificationData(userId).observeForever(notificationDataObserver);

        // When
        notificationViewModel.getNotificationData(userId);

        // Then
        verify(notificationRepository).getNotificationData(userId);
        verify(notificationDataObserver).onChanged(null);
    }
}