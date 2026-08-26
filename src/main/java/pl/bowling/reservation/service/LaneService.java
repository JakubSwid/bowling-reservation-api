package pl.bowling.reservation.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import pl.bowling.reservation.dto.CreateLaneRequest;
import pl.bowling.reservation.entity.Lane;
import pl.bowling.reservation.enums.Status;
import pl.bowling.reservation.exception.LaneAlreadyExistsException;
import pl.bowling.reservation.exception.LaneDoesntExistException;
import pl.bowling.reservation.repository.LaneRepository;

import java.util.List;
import java.util.Optional;

@Service
public class LaneService {

    private final LaneRepository repository;

    public LaneService(LaneRepository repository){
        this.repository = repository;
    }

    public List<Lane> getAllLanes(){
        return repository.findAll();
    }


    public Lane createLane( CreateLaneRequest request){

        if(repository.existsByLaneNumber(request.laneNumber()))
            throw new LaneAlreadyExistsException(request.laneNumber());

        Lane lane = new Lane();
        lane.setStatus(Status.ACTIVE);
        lane.setLaneNumber(request.laneNumber());
        return repository.save(lane);
    }

    public Lane getLane(Long id){
        return repository.findById(id)
                .orElseThrow(()-> new LaneDoesntExistException("Lane not found with this id: " + id));


    }

    @Transactional
    public void deleteLane(Long id){

        if(!repository.existsById(id))
            throw new LaneDoesntExistException("Lane not found with this id: " + id);

        repository.deleteById(id);
    }
}
