package pl.bowling.reservation.exception;

public class LaneDoesntExistException extends RuntimeException {
    public LaneDoesntExistException(String message) {
        super(message);
    }
}
