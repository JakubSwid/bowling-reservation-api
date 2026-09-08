package pl.bowling.reservation.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.bowling.reservation.enums.ReservationStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name ="reservation")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="lane_id", nullable = false)
    private Lane lane;

    @Column
    @NotBlank
    private LocalDateTime startTime;

    @Column
    @NotBlank
    private LocalDateTime endTime;

    @Column
    @NotNull
    @Enumerated(EnumType.STRING)
    private ReservationStatus reservationStatus;

    @Column
    @NotBlank
    private String customerName;

    @Column
    @NotBlank
    @Email
    private String customerEmail;

    @Version
    private Integer version;
}
