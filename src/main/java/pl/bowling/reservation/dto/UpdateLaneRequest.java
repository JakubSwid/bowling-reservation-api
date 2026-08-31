package pl.bowling.reservation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import pl.bowling.reservation.enums.Status;

public record UpdateLaneRequest(@NotNull @Positive Integer laneNumber,
                             @NotNull Status status) {
}
