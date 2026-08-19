package pl.bowling.reservation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.bowling.reservation.entity.Lane;
import pl.bowling.reservation.repository.LaneRepository;

import java.util.List;

@RestController
@RequestMapping("/api/lanes")
public class LaneController {
    private final LaneRepository repository;

    public LaneController(LaneRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Lane> getAll() {
        return repository.findAll();
    }
}