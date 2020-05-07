package core;

import enums.Direction;
import interfaces.LiftControllerInterface;
import interfaces.LiftInterface;
import interfaces.ControllerInterface;
import models.ServiceRequest;

import java.util.concurrent.Semaphore;

import static constants.Constants.NONE;
import static constants.Constants.NO_OF_FLOORS;
import static enums.Direction.*;

/**
 * Manages the movement of individual lift.
 */
public class Lift implements LiftControllerInterface {
    private final String TAG;

    private int liftNumber;
    private Direction status;
    private int[] serviceList;
    private LiftInterface liftInterface;
    private Semaphore lift;
    private ControllerInterface controller;
    private int changeDirection;

    /**
     * @param no              Lift id.
     * @param lift            Semaphore for synchronisation between lifts and the manager while accessing queue of requests.
     * @param controller      Interface to give directions to the controller.
     * @param liftInterface   Interface to give directions to manager.
     */
    public Lift(int no, Semaphore lift, ControllerInterface controller, LiftInterface liftInterface) {
        TAG = "core.Lift" + no;
        this.liftNumber = no;
        this.controller = controller;
        this.liftInterface = liftInterface;
        this.lift = lift;
        serviceList = new int[NO_OF_FLOORS];
        for (int i = 0; i < NO_OF_FLOORS; i++)
            serviceList[i] = NONE;
        status = STATIONARY;
        changeDirection = NONE;
    }

    /**
     * Called when the inside button if the lift is pressed, so that the lift can decide its next stop.
     *
     * @param floor Floor of button pressed.
     */
    public void service(int floor) {
        serviceList[floor] = 1;
        decideNextStop();
    }

    /**
     * Called when a floor/service request has been serviced.
     *
     * @param i         Floor serviced.
     * @param direction enums.Direction of the request serviced.
     */
    private void serviced(int i, Direction direction) {
        serviceList[i] = NONE;
        controller.disableInsideButton(liftNumber, i);
        liftInterface.serviced(new ServiceRequest(i, direction, liftNumber));

    }

    /**
     * Get the direction in which lift is moving.
     * @return enums.Direction in which lift is moving.
     */
    public Direction getStatus() {
        return status;
    }

    /**
     * Set the new direction of the lift.
     * @param status New direction of the lift.
     */
    public void setStatus(Direction status) {
        Util.Log(TAG, "Status set : " + status);
        this.status = status;
        controller.changeDirection(liftNumber, status);
    }

    /**
     * Check if lift is stationary.
     * @return true if it is stationary, false otherwise.
     */
    public boolean isStationary() {
        return (status == STATIONARY);
    }

    /**
     * Utility function to decide which direction should be moved to reach from source to destination
     * @param source Source floor.
     * @param destination Destination floor.
     * @return enums.Direction in which it should move.
     */
    private Direction getDirection(int source, int destination) {
        if (source == destination)
            return STATIONARY;
        return destination - source < 0 ? GOING_DOWN : GOING_UP;
    }

    /**
     * Get the nearest floor which needs to be serviced according to the direction.
     * @param direction GOING_UP if inside button above current position is required, GOING_DOWN otherwise.
     * @return Nearest floor to be serviced.
     */
    private int getNextInside(Direction direction) {
        int currentPosition = controller.getCurrentPosition(liftNumber);
        if (direction == GOING_UP) {
            for (int i = currentPosition; i < NO_OF_FLOORS; i++) {
                if (serviceList[i] != NONE)
                    return i;
            }
        } else {
            for (int i = currentPosition; i >= 0; i--) {
                if (serviceList[i] != NONE)
                    return i;
            }
        }
        return NONE;
    }

    /**
     * Check if the lift can be serviced immediately:
     * 1. If the gate is open, current floor and direction is same as requested floor and direction, reopen the gate.
     * 2. If the the lift is stationary and is present at the floor, just open the gate.
     * @param request Request to be checked.
     * @return true if the request matched the above criteria, false otherwise.
     */
    boolean serviceImmediately(ServiceRequest request) {
        if (controller.isGateOpen(liftNumber) && request.getFloor() == controller.getCurrentPosition(liftNumber) && getStatus() == request.getDirection()) {
            controller.reOpen(liftNumber);
            return true;
        } else if (isStationary() && request.getFloor() == controller.getCurrentPosition(liftNumber)) {
            controller.openLift(liftNumber);
            setStatus(request.getDirection());
            return true;
        } else return false;
    }

    /**
     * Decide next stop of the lift and inform it to the controller.
     * //TODO
     */
    void decideNextStop() {
        if (!controller.isGateOpen(liftNumber)) {
            try {
                lift.acquire();
                int nextStop;
                int currentPosition = controller.getCurrentPosition(liftNumber);
                int nextInternalUp = getNextInside(GOING_UP);
                int nextInternalDown = getNextInside(GOING_DOWN);
                if (getStatus() == STATIONARY) {
                    Util.Log(TAG, "Stationary", true);
                    ServiceRequest nextExternal = liftInterface.getNextOutsideFloor(currentPosition, STATIONARY, liftNumber);
                    if (nextInternalUp == NONE && nextInternalDown == NONE && nextExternal == null)
                        nextStop = NONE;
                    else {
                        if (nextInternalUp != NONE || nextInternalDown != NONE) {
                            if (nextInternalUp != NONE && nextInternalDown != NONE) {
                                nextStop = Math.min(nextInternalUp, nextInternalDown);
                            } else if (nextInternalUp != NONE)
                                nextStop = nextInternalUp;
                            else nextStop = nextInternalDown;
                            setStatus(getDirection(currentPosition, nextStop));
                        } else {
                            nextStop = nextExternal.getFloor();
                            nextExternal.setSelected(liftNumber);
                            setStatus(getDirection(currentPosition, nextStop));
                            if (getStatus() != nextExternal.getDirection())
                                changeDirection = nextStop;
                        }
                    }
                } else if (getStatus() == GOING_UP) {
                    Util.Log(TAG, "Going up", true);
                    ServiceRequest tmp = liftInterface.getNextOutsideFloor(currentPosition, GOING_UP, liftNumber);
                    Util.Log(TAG, "next : " + tmp);
                    if (tmp == null) {
                        if (nextInternalUp != NONE)
                            nextStop = nextInternalUp;
                        else nextStop = nextInternalDown;
                    } else {
                        int nextExternal = tmp.getFloor();
                        if (nextInternalUp != NONE && tmp.getDirection() == GOING_UP && nextExternal >= currentPosition) {
                            if (nextInternalUp < nextExternal)
                                nextStop = nextInternalUp;
                            else {
                                nextStop = nextExternal;
                                tmp.setSelected(liftNumber);
                            }
                        } else if (nextInternalUp != NONE)
                            nextStop = nextInternalUp;
                        else if (tmp.getDirection() == GOING_UP && nextExternal >= currentPosition) {
                            nextStop = nextExternal;
                            tmp.setSelected(liftNumber);
                        } else if (tmp.getDirection() == GOING_DOWN && nextExternal >= currentPosition) {
                            nextStop = nextExternal;
                            tmp.setSelected(liftNumber);
                            changeDirection = nextStop;
                        } else {
                            if (nextInternalDown != NONE && tmp.getDirection() == GOING_DOWN && nextExternal <= currentPosition) {
                                if (nextInternalDown < nextExternal)
                                    nextStop = nextInternalDown;
                                else {
                                    nextStop = nextExternal;
                                    tmp.setSelected(liftNumber);
                                }
                            } else if (nextInternalDown != NONE)
                                nextStop = nextInternalDown;
                            else if (tmp.getDirection() == GOING_DOWN && nextExternal <= currentPosition) {
                                nextStop = nextExternal;
                                tmp.setSelected(liftNumber);
                            } else {
                                nextStop = nextExternal;
                                tmp.setSelected(liftNumber);
                                changeDirection = nextStop;
                            }
                        }
                    }
                    if (currentPosition != nextStop)
                        setStatus(getDirection(currentPosition, nextStop));
                } else {
                    Util.Log(TAG, "Going down", true);
                    ServiceRequest tmp = liftInterface.getNextOutsideFloor(currentPosition, GOING_DOWN, liftNumber);
                    Util.Log(TAG, "next : " + tmp);
                    if (tmp == null) {
                        if (nextInternalDown != NONE)
                            nextStop = nextInternalDown;
                        else nextStop = nextInternalUp;
                    } else {
                        int nextExternal = tmp.getFloor();
                        if (nextInternalDown != NONE && tmp.getDirection() == GOING_DOWN && nextExternal <= currentPosition) {
                            if (nextInternalDown > nextExternal)
                                nextStop = nextInternalDown;
                            else {
                                nextStop = nextExternal;
                                tmp.setSelected(liftNumber);
                            }
                        } else if (nextInternalDown != NONE)
                            nextStop = nextInternalDown;
                        else if (tmp.getDirection() == GOING_DOWN && nextExternal <= currentPosition) {
                            nextStop = nextExternal;
                            tmp.setSelected(liftNumber);
                        } else if (tmp.getDirection() == GOING_UP && nextExternal <= currentPosition) {
                            nextStop = nextExternal;
                            tmp.setSelected(liftNumber);
                            changeDirection = nextStop;
                        } else {
                            if (nextInternalUp != NONE && tmp.getDirection() == GOING_UP && nextExternal >= currentPosition) {
                                if (nextInternalUp > nextExternal)
                                    nextStop = nextInternalUp;
                                else {
                                    nextStop = nextExternal;
                                    tmp.setSelected(liftNumber);
                                }
                            } else if (nextInternalUp != NONE)
                                nextStop = nextInternalUp;
                            else if (tmp.getDirection() == GOING_UP && nextExternal <= currentPosition) {
                                nextStop = nextExternal;
                                tmp.setSelected(liftNumber);
                            } else {
                                nextStop = nextExternal;
                                tmp.setSelected(liftNumber);
                                changeDirection = nextStop;
                            }
                        }
                    }
                    if (currentPosition != nextStop)
                        setStatus(getDirection(currentPosition, nextStop));
                }
                Util.Log(TAG, "My next stop is " + nextStop, true);
                if (nextStop == NONE)
                    setStatus(STATIONARY);
                controller.setNextStop(liftNumber, nextStop);
                lift.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Called when a floor has been reached, so that it can be marked serviced and change direction if needed.
     * @param floor Floor which has been reached.
     */
    @Override
    public void onReached(int floor) {
        if (changeDirection == floor)
            changeDirection();
        serviced(floor, getStatus());
    }

    /**
     * Change direction of the lift.
     */
    private void changeDirection() {
        if (status == GOING_UP)
            setStatus(GOING_DOWN);
        else if (status == GOING_DOWN)
            setStatus(GOING_UP);
        changeDirection = NONE;
    }
}
