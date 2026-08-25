package pl.bowling.reservation.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.bowling.reservation.dto.CreateLaneRequest;
import pl.bowling.reservation.entity.Lane;
import pl.bowling.reservation.enums.Status;
import pl.bowling.reservation.exception.LaneAlreadyExistsException;
import pl.bowling.reservation.repository.LaneRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LaneServiceTest {

    @Mock
    private LaneRepository repository;

    @InjectMocks
    private LaneService service;

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
        assertThrows(LaneAlreadyExistsException.class, () -> service.createLane(request));
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
}