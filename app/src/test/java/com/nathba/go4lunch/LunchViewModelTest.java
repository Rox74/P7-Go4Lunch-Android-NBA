package com.nathba.go4lunch;

import static org.mockito.Mockito.verify;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.nathba.go4lunch.application.LunchViewModel;
import com.nathba.go4lunch.models.Lunch;
import com.nathba.go4lunch.repository.LunchRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.ArrayList;

public class LunchViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private LunchRepository lunchRepository;

    private LunchViewModel lunchViewModel;
    private MutableLiveData<List<Lunch>> lunchLiveData;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        lunchLiveData = new MutableLiveData<>();
        lunchLiveData.setValue(new ArrayList<>());

        when(lunchRepository.getLunches()).thenReturn(lunchLiveData);

        lunchViewModel = new LunchViewModel(lunchRepository);
    }

    @Test
    public void getLunches_returnsLiveData() {
        assertEquals(lunchLiveData.getValue(), lunchViewModel.getLunches().getValue());
    }

    @Test
    public void addLunch_callsRepositoryAddLunch() {
        Lunch lunch = new Lunch();
        lunchViewModel.addLunch(lunch);
        verify(lunchRepository).addLunch(lunch);
    }
}