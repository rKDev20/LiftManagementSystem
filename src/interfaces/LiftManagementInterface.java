package interfaces;

public interface LiftManagementInterface {
    void onUpPressed(int i);
    void onDownPressed(int i);
    void onInsideButtonPress(int lift,int floor);
    void onReachedFloor(int lift, int floor);
    void onMovementStarted(int lift);
}
