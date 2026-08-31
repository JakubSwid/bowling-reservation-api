package pl.bowling.reservation.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.bowling.reservation.dto.CreateLaneRequest;
import pl.bowling.reservation.dto.UpdateLaneRequest;
import pl.bowling.reservation.entity.Lane;
import pl.bowling.reservation.service.LaneService;

import java.util.List;

@RestController
@RequestMapping("/api/lanes")
public class LaneController {
    private final LaneService service;

    public LaneController(LaneService service) {
        this.service = service;
    }

    @GetMapping
    public List<Lane> getAll() {
        return service.getAllLanes();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Lane> getLane(@PathVariable Long id){
        Lane requestedLane = service.getLane(id);
        return ResponseEntity.status(HttpStatus.OK).body(requestedLane);
    }

    @PostMapping
    public ResponseEntity<Lane> addLane(@RequestBody @Valid CreateLaneRequest laneNumber){
        Lane createdLane = service.createLane(laneNumber);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLane);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLane(@PathVariable Long id){
        service.deleteLane(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Lane> updateLane(@RequestBody @Valid UpdateLaneRequest request, @PathVariable Long id){
        Lane updatedLane = service.updateLane(request, id);
        return ResponseEntity.status(HttpStatus.OK).body(updatedLane);
    }
}
