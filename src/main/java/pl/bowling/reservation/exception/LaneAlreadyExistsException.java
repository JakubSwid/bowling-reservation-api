package pl.bowling.reservation.exception;

public class LaneAlreadyExistsException extends RuntimeException {

    public LaneAlreadyExistsException(Integer laneNumber){
        super("Lane with number "+ laneNumber +" already exists");
    }
    public LaneAlreadyExistsException(String message) {
        super(message);
    }
}
