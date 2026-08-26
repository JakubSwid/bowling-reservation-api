package pl.bowling.reservation.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(LaneAlreadyExistsException.class)
    public ProblemDetail handleLaneAlreadyExists(
            LaneAlreadyExistsException ex,
            HttpServletRequest request
    ) {

        log.warn("Save conflict. Path: {}. Message: {}", request.getRequestURI(), ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());

        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", request.getRequestURI());

        return problemDetail;
    }

    @ExceptionHandler(LaneDoesntExistException.class)
    public ProblemDetail handleLaneDoesntExist(
            LaneDoesntExistException ex,
            HttpServletRequest request
    ) {

        log.warn("Lane not found. Path: {}. Message: {}", request.getRequestURI(), ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());

        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", request.getRequestURI());

        return problemDetail;
    }
}
