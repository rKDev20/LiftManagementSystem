package interfaces;

import enums.Direction;

public interface ControllerInterface {
    //    int lift;
//    ControllerInterface(){
//
//    }
    void changeFloor(int lift, int floor);

    void changeDirection(int lift, Direction direction);

    void disableOutsideButton(int floor, Direction direction);

    void disableInsideButton(int lift, int floor);

    void openLift(int lift);

    void moveLift(int lift, int floor);

    int getCurrentPosition(int lift);

    void setNextStop(int lift, int nextStop);

    boolean isGateOpen(int lift);

    void reOpen(int lift);

}
