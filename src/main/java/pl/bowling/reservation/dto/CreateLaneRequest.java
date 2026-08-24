package pl.bowling.reservation.dto;

import jakarta.validation.constraints.Positive;

public record CreateLaneRequest(@Positive Integer laneNumber) {
}
