package pl.bowling.reservation.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.bowling.reservation.dto.CreateReservationRequest;
import pl.bowling.reservation.dto.ReservationResponse;
import pl.bowling.reservation.entity.Reservation;
import pl.bowling.reservation.service.ReservationService;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    private final ReservationService service;

    public ReservationController(ReservationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> create(@RequestBody @Valid CreateReservationRequest request) {
        ReservationResponse created = service.createReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(created);
    }

    @GetMapping("/lane/{laneId}")
    public List<ReservationResponse> getByLane(@PathVariable Long laneId) {
        return service.getReservationsForLane(laneId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long id) {
        service.cancelReservation(id);
    }
}