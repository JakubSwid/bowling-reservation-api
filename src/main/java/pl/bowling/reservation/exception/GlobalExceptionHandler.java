package pl.bowling.reservation.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({LaneDoesntExistException.class, ReservationNotFoundException.class})
    public ProblemDetail handleNotFound(RuntimeException ex, HttpServletRequest request) {
        log.warn("Resource not found. Path: {}. Message: {}", request.getRequestURI(), ex.getMessage());
        return createProblemDetail(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler({LaneAlreadyExistsException.class, OverlappingReservationException.class})
    public ProblemDetail handleConflict(RuntimeException ex, HttpServletRequest request) {
        log.warn("Conflict detected. Path: {}. Message: {}", request.getRequestURI(), ex.getMessage());
        return createProblemDetail(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    private ProblemDetail createProblemDetail(HttpStatus status, String detail, HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }
}