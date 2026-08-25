package pl.bowling.reservation.service;

import jakarta.validation.constraints.Positive;
import org.springframework.stereotype.Service;
import pl.bowling.reservation.dto.CreateLaneRequest;
import pl.bowling.reservation.entity.Lane;
import pl.bowling.reservation.enums.Status;
import pl.bowling.reservation.exception.LaneAlreadyExistsException;
import pl.bowling.reservation.repository.LaneRepository;

import java.util.List;

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
}
