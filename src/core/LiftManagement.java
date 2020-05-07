package core;

import enums.Direction;
import interfaces.LiftInterface;
import interfaces.ControllerInterface;
import models.ServiceRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import static constants.Constants.*;
import static enums.Direction.*;

/**
 * Class which acts as a bridge between the core.Lift and core.Controller.
 */
public class LiftManagement implements LiftInterface {
    private final static String TAG = "core.LiftManagement";
    private static final int INT_MAX = 50;
    private static final int INT_MIN = -1;
    Lift lift1;
    Lift lift2;
    List<ServiceRequest> queue;
    Semaphore liftSync;
    ControllerInterface controller;

    /**
     * @param controller Interface to give commands to the controller.
     */
    public LiftManagement(ControllerInterface controller) {
        this.controller = controller;
        liftSync = new Semaphore(1);
        lift1 = new Lift(LIFT_ONE, liftSync, controller, this);
        lift2 = new Lift(LIFT_TWO, liftSync, controller, this);
        queue = new ArrayList<>();
    }

    /**
     * Handle the up button pressed at any floor.
     *
     * @param i The floor at which the button is pressed.
     */
    public void onUpPressed(int i) {
        Util.Log(TAG, "onUpPressed " + i);
        outsideButtonPress(new ServiceRequest(i, GOING_UP));
    }

    /**
     * Handle the down button pressed at any floor.
     *
     * @param i The floor at which the button is pressed.
     */
    public void onDownPressed(int i) {
        Util.Log(TAG, "onDownPressed " + i);
        outsideButtonPress(new ServiceRequest(i, GOING_DOWN));
    }

    /**
     * Callback when a service request have been serviced.
     *
     * @param request Request which have been serviced.
     */
    @Override
    public void serviced(ServiceRequest request) {
        try {
            liftSync.acquire();
            queue.removeIf(e -> e.equals(request));
            controller.disableOutsideButton(request.getFloor(), request.getDirection());
            liftSync.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the closest service request from outside button which can be serviced by the corresponding lift at the earliest according to parameters.
     * 1. Service requests which has been selected by other lifts are scanned.
     * 2. If the queue is empty or no service request can be selected, return empty.
     * 3. If the lift is stationary, return the first request.
     * 4. If the lift is moving upwards, prioritise the floor according to given trend(highest to lowest from a to e) :
     * a. Closest floor which is above current floor and request is upwards.
     * b. Farthest floor which is above current floor and request is downwards.
     * c. Same as current floor.
     * d. Closest floor which is below current floor and request is downwards.
     * e. Farthest floor which is below current floor and request is upwards.
     * 5. If the lift is moving downwards, prioritise the floor according to given trend(highest to lowest from a to e) :
     * a. Closest floor which is below current floor and request is downwards.
     * b. Farthest floor which is below current floor and request is upwards.
     * c. Same as current floor.
     * d. Closest floor which is above current floor and request is upwards.
     * e. Farthest floor which is above current floor and request is downwards.
     *
     * @param currentFloor     Current floor the lift.
     * @param currentDirection Current direction it is moving in.
     * @param lift             Lift id.
     * @return Service request which can serviced.
     */
    @Override
    public ServiceRequest getNextOutsideFloor(int currentFloor, Direction currentDirection, int lift) {
        Util.Log(TAG, "QUEUE " + lift + "" + getQueue());
        if (queue.isEmpty())
            return null;
        if (currentDirection == STATIONARY) {
            for (ServiceRequest e : queue) {
                if (e.isSelected(lift))
                    return e;
            }
            return null;
        } else if (currentDirection == GOING_UP) {
            ServiceRequest minUpUp = new ServiceRequest(INT_MAX);
            ServiceRequest maxUpDown = new ServiceRequest(INT_MIN);
            ServiceRequest sameFloor = new ServiceRequest(NONE);
            ServiceRequest minDownDown = new ServiceRequest(INT_MAX);
            ServiceRequest maxDownUp = new ServiceRequest(INT_MIN);
            //System.out.println(currentDirection);
            for (ServiceRequest e : queue) {
                // System.out.println(e.getFloor() + "," + currentFloor + "," + minUp.getFloor() + "," + maxDown.getFloor() + "," + e.getDirection() + "," + e.isSelected(lift));
                if (e.isSelected(lift)) {
                    if (e.getFloor() > currentFloor) {
                        if (e.getFloor() < minUpUp.getFloor() && e.getDirection() == GOING_UP) {
                            minUpUp = e;
                        } else if (e.getFloor() > maxUpDown.getFloor() && e.getDirection() == GOING_DOWN) {
                            maxUpDown = e;
                        }
                    } else if (e.getFloor() == currentFloor) {
                        sameFloor = e;
                    } else {
                        if (e.getFloor() < minDownDown.getFloor() && e.getDirection() == GOING_DOWN) {
                            minDownDown = e;
                        } else if (e.getFloor() > maxDownUp.getFloor() && e.getDirection() == GOING_UP) {
                            maxDownUp = e;
                        }
                    }
                }
            }
            if (sameFloor.getDirection() != GOING_UP)
                if (minUpUp.getFloor() == INT_MAX)
                    if (maxUpDown.getFloor() == INT_MIN)
                        if (sameFloor.getFloor() == NONE)
                            if (minDownDown.getFloor() == INT_MAX)
                                if (maxDownUp.getFloor() == INT_MIN)
                                    return null;
                                else return maxDownUp;
                            else return minDownDown;
                        else return sameFloor;
                    else return maxUpDown;
                else return minUpUp;
            else return sameFloor;
        } else {
            ServiceRequest maxDownDown = new ServiceRequest(INT_MIN);
            ServiceRequest minDownUp = new ServiceRequest(INT_MAX);
            ServiceRequest sameFloor = new ServiceRequest(NONE);
            ServiceRequest maxUpUp = new ServiceRequest(INT_MIN);
            ServiceRequest minUpDown = new ServiceRequest(INT_MAX);
            //System.out.println(currentDirection);
            for (ServiceRequest e : queue) {
                // System.out.println(e.getFloor() + "," + currentFloor + "," + minUp.getFloor() + "," + maxDown.getFloor() + "," + e.getDirection() + "," + e.isSelected(lift));
                if (e.isSelected(lift)) {
                    if (e.getFloor() < currentFloor) {
                        if (e.getFloor() > maxDownDown.getFloor() && e.getDirection() == GOING_DOWN) {
                            maxDownDown = e;
                        }
                        if (e.getFloor() < minDownUp.getFloor() && e.getDirection() == GOING_UP) {
                            minDownUp = e;
                        }
                    } else if (e.getFloor() == currentFloor) {
                        sameFloor = e;
                    } else {
                        if (e.getFloor() > maxUpUp.getFloor() && e.getDirection() == GOING_UP) {
                            maxUpUp = e;
                        }
                        if (e.getFloor() < minUpDown.getFloor() && e.getDirection() == GOING_DOWN) {
                            minUpDown = e;
                        }
                    }
                }
            }
            if (sameFloor.getDirection() != GOING_DOWN)
                if (maxDownDown.getFloor() == INT_MIN)
                    if (minDownUp.getFloor() == INT_MAX)
                        if (sameFloor.getFloor() == NONE)
                            if (maxUpUp.getFloor() == INT_MIN)
                                if (minUpDown.getFloor() == INT_MAX)
                                    return null;
                                else return minUpDown;
                            else return maxUpUp;
                        else return sameFloor;
                    else return minDownUp;
                else return maxDownDown;
            else return sameFloor;
        }
    }

    /**
     * Utility function to debug the queue.
     */
    private String getQueue() {
        StringBuilder s = new StringBuilder();
        for (ServiceRequest e : queue) {
            s.append(e).append(",");
        }
        if (s.length() == 0)
            return "null";
        return s.toString();
    }

    /**
     * Handle outside button pressed and ask a lift to service it, if possible.
     *
     * @param request Request of button pressed.
     */
    private void outsideButtonPress(ServiceRequest request) {
        if (lift1.serviceImmediately(request) || lift2.serviceImmediately(request)) {
            serviced(request);
        } else {
            try {
                liftSync.acquire();
                queue.add(request);
                liftSync.release();
                int a = Math.abs(controller.getCurrentPosition(0) - request.getFloor());
                int b = Math.abs(controller.getCurrentPosition(1) - request.getFloor());
                if (a < b) {
                    lift1.decideNextStop();
                    lift2.decideNextStop();
                } else {
                    lift2.decideNextStop();
                    lift1.decideNextStop();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void insideButtonPress(int lift, int floor) {
        Util.Log(TAG, "insideButtonLift" + lift + ", floor : " + floor);
        if (lift == 0) {
            lift1.service(floor);
        } else if (lift == 1) {
            lift2.service(floor);
        }
    }

    /**
     * Notify the corresponding lift that floor has been reached.
     *
     * @param lift  Lift id.
     * @param floor Floor which has been reached
     */
    public void onReachedFloor(int lift, int floor) {
        if (lift == 0)
            lift1.onReached(floor);
        else lift2.onReached(floor);
    }

    /**
     * Notify that the corresponding lift is ready to go to next floor.
     *
     * @param lift Lift id.
     */
    public void onMovementStarted(int lift) {
        if (lift == 0)
            lift1.decideNextStop();
        else lift2.decideNextStop();
    }
}
