package com.nathba.go4lunch;

import static org.mockito.Mockito.verify;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.nathba.go4lunch.application.WorkmateViewModel;
import com.nathba.go4lunch.models.Workmate;
import com.nathba.go4lunch.repository.WorkmateRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.ArrayList;

public class WorkmateViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private WorkmateRepository workmateRepository;

    private WorkmateViewModel workmateViewModel;
    private MutableLiveData<List<Workmate>> workmateLiveData;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        workmateLiveData = new MutableLiveData<>();
        workmateLiveData.setValue(new ArrayList<>());

        when(workmateRepository.getWorkmates()).thenReturn(workmateLiveData);

        workmateViewModel = new WorkmateViewModel(workmateRepository);
    }

    @Test
    public void getWorkmates_returnsWorkmateLiveData() {
        assertEquals(workmateLiveData.getValue(), workmateViewModel.getWorkmates().getValue());
    }

    @Test
    public void addWorkmate_callsRepositoryAddWorkmate() {
        Workmate workmate = new Workmate();
        workmateViewModel.addWorkmate(workmate);
        verify(workmateRepository).addWorkmate(workmate);
    }
}