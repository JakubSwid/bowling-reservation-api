package pl.bowling.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.bowling.reservation.entity.Reservation;
import pl.bowling.reservation.enums.ReservationStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByLaneIdAndReservationStatus(Long laneId, ReservationStatus status);

    @Query("""
    SELECT r from Reservation as r
    WHERE r.lane.id = :laneId
    AND r.reservationStatus = 'ACTIVE'
    AND r.startTime < :endTime
    AND r.endTime > :startTime
""")
    List<Reservation> findOverlapping(
            @Param("laneId") Long laneId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}
