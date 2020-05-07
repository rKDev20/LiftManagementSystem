package interfaces;

import enums.Direction;
import models.ServiceRequest;

public interface LiftInterface {
    ServiceRequest getNextOutsideFloor(int currentFloor, Direction currentDirection, int lift);
    void serviced(ServiceRequest request);
}
