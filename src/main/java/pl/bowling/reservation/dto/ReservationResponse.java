package pl.bowling.reservation.dto;

import pl.bowling.reservation.entity.Reservation;

import java.time.LocalDateTime;

public record ReservationResponse(

        Long id,

        Long laneId,

        Integer laneNumber,

        LocalDateTime startTime,

        LocalDateTime endTime,

        String status,

        String customerName,

        String customerEmail

) {

    public static ReservationResponse from(Reservation r) {

        return new ReservationResponse(

                r.getId(),

                r.getLane().getId(),

                r.getLane().getLaneNumber(),

                r.getStartTime(),

                r.getEndTime(),

                r.getReservationStatus().name(),

                r.getCustomerName(),

                r.getCustomerEmail()

        );

    }

}


