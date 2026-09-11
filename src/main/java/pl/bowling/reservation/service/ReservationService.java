package pl.bowling.reservation.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import pl.bowling.reservation.dto.CreateReservationRequest;
import pl.bowling.reservation.dto.ReservationResponse;
import pl.bowling.reservation.entity.Lane;
import pl.bowling.reservation.entity.Reservation;
import pl.bowling.reservation.enums.ReservationStatus;
import pl.bowling.reservation.exception.LaneDoesntExistException;
import pl.bowling.reservation.exception.OverlappingReservationException;
import pl.bowling.reservation.exception.ReservationNotFoundException;
import pl.bowling.reservation.repository.LaneRepository;
import pl.bowling.reservation.repository.ReservationRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final LaneRepository laneRepository;

    public ReservationService(ReservationRepository reservationRepository, LaneRepository laneRepository) {
        this.reservationRepository = reservationRepository;
        this.laneRepository = laneRepository;
    }

    @Transactional
    public ReservationResponse createReservation(CreateReservationRequest request) {
        Lane lane = laneRepository.findByIdWithLock(request.laneId())
                .orElseThrow(() -> new LaneDoesntExistException("Lane does not exist"));

        if (!request.startTime().isBefore(request.endTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        if (request.startTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot book a reservation in the past");
        }

        boolean hasOverlaps = reservationRepository.existsOverlapping(request.laneId(), request.startTime(), request.endTime());

        if (hasOverlaps) {
            throw new OverlappingReservationException(
                    "Overlapping reservation for lane " + request.laneId());
        }

        Reservation reservation = new Reservation();
        reservation.setLane(lane);
        reservation.setStartTime(request.startTime());
        reservation.setEndTime(request.endTime());
        reservation.setReservationStatus(ReservationStatus.ACTIVE);
        reservation.setCustomerName(request.customerName());
        reservation.setCustomerEmail(request.customerEmail());

        Reservation saved = reservationRepository.save(reservation);
        return ReservationResponse.from(saved);
    }

    public List<ReservationResponse> getReservationsForLane(Long laneId) {
        if (!laneRepository.existsById(laneId)) {
            throw new LaneDoesntExistException("Lane not found: " + laneId);
        }

        return reservationRepository
                .findByLaneIdAndReservationStatus(laneId, ReservationStatus.ACTIVE)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional
    public void cancelReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id).orElseThrow(
                () -> new ReservationNotFoundException("Reservation not found: " + id));

        reservation.setReservationStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }
}
