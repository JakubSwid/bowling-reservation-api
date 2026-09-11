package pl.bowling.reservation.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.bowling.reservation.entity.Lane;

import java.util.Optional;

public interface LaneRepository extends JpaRepository<Lane, Long> {
    boolean existsByLaneNumber(Integer laneNumber);

    boolean existsByLaneNumberAndIdNot(Integer laneNumber, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Lane l WHERE l.id = :id")
    Optional<Lane> findByIdWithLock(@Param("id") Long id);
}
