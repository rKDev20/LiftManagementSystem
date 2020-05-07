package models;

import enums.Direction;

import static constants.Constants.NONE;
import static enums.Direction.STATIONARY;

public class ServiceRequest {
    /**
     * Floor where the requested originated.
     */
    private int floor;
    /**
     * enums.Direction of the request. Can be GOING_UP,GOING_DOWN,STATIONARY.
     */
    private Direction direction;
    /**
     * Denotes which lift has agreed to service this request. core.Lift id which has selected it, else NONE if not yet selected.
     */
    private int selectedBy;

    public ServiceRequest(int floor, Direction direction) {
        this.floor = floor;
        this.direction = direction;
        selectedBy = NONE;
    }

    public ServiceRequest(int floor, Direction direction, int selectedBy) {
        this.floor = floor;
        this.direction = direction;
        this.selectedBy = selectedBy;
    }

    public void setSelected(int selectedBy) {
        this.selectedBy = selectedBy;
    }

    public ServiceRequest(int floor) {
        this.floor = floor;
        direction = STATIONARY;
        selectedBy = NONE;
    }

    public int getFloor() {
        return floor;
    }

    public Direction getDirection() {
        return direction;
    }

    /**
     * For two instances to equal, floor and selectedBy must be same. enums.Direction can either be same or direction of parameter must be STATIONARY.
     *
     * @param request Request to be compared with.
     * @return true if equal, else false.
     */
    public boolean equals(ServiceRequest request) {
//        System.out.println("Equality check : " + request.toString() + toString());
        if (request.floor == floor && (request.direction == direction || request.direction == STATIONARY) && request.selectedBy == selectedBy) {
            request.direction = direction;
//            System.out.println("Equality check : " + request.toString() + toString());
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "(" + floor + "," + direction + ", " + selectedBy + ")";
    }

    /**
     * Check if this request has been selected by given lift/if it hasn't been selected by any lift.
     *
     * @param lift core.Lift id to be verified against
     * @return true if it is selected by given lift or it hasn't been selected at all, false otherwise.
     */
    public boolean isSelected(int lift) {
        return selectedBy == NONE || selectedBy == lift;
    }
}
