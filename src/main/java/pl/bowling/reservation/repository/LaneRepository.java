package pl.bowling.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.bowling.reservation.entity.Lane;

public interface LaneRepository extends JpaRepository<Lane, Long> {
    boolean existsByLaneNumber(Integer laneNumber);
}
