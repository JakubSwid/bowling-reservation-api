package pl.bowling.reservation.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.bowling.reservation.dto.CreateLaneRequest;
import pl.bowling.reservation.dto.UpdateLaneRequest;
import pl.bowling.reservation.entity.Lane;
import pl.bowling.reservation.enums.Status;
import pl.bowling.reservation.exception.LaneAlreadyExistsException;
import pl.bowling.reservation.exception.LaneDoesntExistException;
import pl.bowling.reservation.repository.LaneRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LaneServiceTest {

    @Mock
    private LaneRepository repository;

    @InjectMocks
    private LaneService service;

    // CreateLane Tests
    @Test
    void shouldSaveLaneWhenNumberIsFree() {
        // given
        CreateLaneRequest request = new CreateLaneRequest(5);
        when(repository.existsByLaneNumber(5)).thenReturn(false);
        when(repository.save(any(Lane.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Lane result = service.createLane(request);

        // then
        verify(repository).save(any(Lane.class));
        assertThat(result.getLaneNumber()).isEqualTo(5);
    }

    @Test
    void shouldThrowWhenLaneNumberAlreadyExists() {
        // given
        CreateLaneRequest request = new CreateLaneRequest(5);
        when(repository.existsByLaneNumber(5)).thenReturn(true);

        // when and then
        assertThatThrownBy(() -> service.createLane(request))
                .isInstanceOf(LaneAlreadyExistsException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldAssignActiveStatusByDefault() {
        // given
        CreateLaneRequest request = new CreateLaneRequest(7);
        when(repository.existsByLaneNumber(7)).thenReturn(false);

        // when
        service.createLane(request);

        // then
        ArgumentCaptor<Lane> captor = ArgumentCaptor.forClass(Lane.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(Status.ACTIVE);
    }

    @Test
    void shouldReturnWhatRepositoryReturns() {
        // given
        Lane savedLane = new Lane();
        savedLane.setId(42L);
        savedLane.setLaneNumber(5);

        CreateLaneRequest request = new CreateLaneRequest(5);
        when(repository.existsByLaneNumber(5)).thenReturn(false);
        when(repository.save(any(Lane.class))).thenReturn(savedLane);

        // when
        Lane result = service.createLane(request);

        // then
        assertThat(result).isSameAs(savedLane);
    }

    //GetAllLanes test
    @Test
    void shouldReturnAllLanes(){
        //given
        Lane lane1 = new Lane(1L,1,Status.ACTIVE,1);
        Lane lane2 = new Lane(2L,2,Status.ACTIVE,1);
        List<Lane> expectedLanes = List.of(lane1,lane2);

        when(repository.findAll()).thenReturn(expectedLanes);

        //when
        List<Lane> result = service.getAllLanes();

        //then
        assertThat(result).isEqualTo(expectedLanes);
        assertThat(result).hasSize(2);
    }

    //GetLane tests
    @Test
    void shouldReturnLaneWhenExists(){
        //given
        Lane lane1 = new Lane(1L,1,Status.ACTIVE,1);

        when(repository.findById(1L)).thenReturn(Optional.of(lane1));

        //when
        Lane result = service.getLane(1L);

        //then
        assertThat(result).isEqualTo(lane1);
        verify(repository).findById(1L);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void shouldThrowExceptionWhenLaneNotFound(){
        //given
        when(repository.findById(1L)).thenReturn(Optional.empty());

        //when then
        assertThatThrownBy(()-> service.getLane(1L))
                .isInstanceOf(LaneDoesntExistException.class)
                .hasMessageContaining("Lane not found with this id: ");

        verify(repository).findById(1L);

    }
    //Delete test(s)
    @Test
    void shouldDeleteLane(){
        when(repository.existsById(1L)).thenReturn(true);

        service.deleteLane(1L);

        verify(repository).deleteById(1L);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void shouldThrowExceptionWhenLaneToDeleteNotFound(){
        //given
        when(repository.existsById(1L)).thenReturn(false);

        //when then
        assertThatThrownBy(()->service.deleteLane(1L))
                .isInstanceOf(LaneDoesntExistException.class)
                .hasMessageContaining("Lane not found with this id: ");

        verify(repository).existsById(1L);
        verify(repository, never()).deleteById(any());
        verifyNoMoreInteractions(repository);
    }

    //Update Tests
    @Test
    void shouldUpdateLaneHappyPath(){
        //given
        Lane existingLane = new Lane(1L,1,Status.ACTIVE,1);
        when(repository.findById(1L)).thenReturn(Optional.of(existingLane));
        when(repository.existsByLaneNumberAndIdNot(any(),any())).thenReturn(false);

        when(repository.save(any(Lane.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateLaneRequest request = new UpdateLaneRequest(7,Status.OUT_OF_SERVICE);

        //when
        Lane result = service.updateLane(request, 1L);

        //then
        ArgumentCaptor<Lane> captor = ArgumentCaptor.forClass(Lane.class);
        verify(repository).save(captor.capture());

        Lane savedLane = captor.getValue();

        assertThat(savedLane.getLaneNumber()).isEqualTo(7);
        assertThat(savedLane.getStatus()).isEqualTo(Status.OUT_OF_SERVICE);
        assertThat(savedLane).isEqualTo(result);
    }

}