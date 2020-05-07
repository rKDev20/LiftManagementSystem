package core;

import enums.Direction;
import enums.GateStatus;
import interfaces.LiftManagementInterface;
import interfaces.ControllerInterface;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.Semaphore;

import static constants.Constants.*;
import static constants.Constants.LIFT_ONE;
import static constants.Constants.LIFT_TWO;

/**
 * Simulate the lift with an UI.
 */
public class Controller implements ControllerInterface {

    private int[] nextStop;
    private float[] currentPosition;
    private ProcessLift[] processThread;
    private Semaphore[] open;
    private boolean[] gateOpen;
    private GateStatus[] gateStatus = new GateStatus[2];
    private Thread[] animateLift = new Thread[2];
    private float[] current = new float[]{89f, 89f};
    float decreaseBy = 1.78f;


    public Button liftOneInZero;
    public Button liftOneInOne;
    public Button liftOneInTwo;
    public Button liftOneInThree;
    public Button liftOneInFour;
    public Button liftOneInFive;
    public Button liftOneInSix;
    public Button liftOneInSeven;
    public Button liftTwoInZero;
    public Button liftTwoInOne;
    public Button liftTwoInTwo;
    public Button liftTwoInThree;
    public Button liftTwoInFour;
    public Button liftTwoInFive;
    public Button liftTwoInSix;
    public Button liftTwoInSeven;
    public Button sevenUp;
    public Button sixUp;
    public Button fiveUp;
    public Button fourUp;
    public Button threeUp;
    public Button twoUp;
    public Button oneUp;
    public Button zeroUp;
    public Button sevenDown;
    public Button sixDown;
    public Button fiveDown;
    public Button fourDown;
    public Button threeDown;
    public Button twoDown;
    public Button oneDown;


    public Button[] liftOneInsideButton;
    public Button[] liftTwoInsideButton;
    public Button[] up;
    public Button[] down;
    public Label liftOneLabel;
    public Label liftTwoLabel;
    public ImageView liftOneDirection;
    public ImageView liftTwoDirection;

    public Image upImage;
    public Image downImage;
    public ImageView liftOneLeftGate;
    public ImageView liftOneRightGate;
    public ImageView liftTwoLeftGate;
    public ImageView liftTwoRightGate;
    public ImageView[] liftLeftGate;
    public ImageView[] liftRightGate;

    LiftManagementInterface liftManagementInterface;

    /**
     * Default constructor for javafx initialisation
     */
    public Controller() {
    }

    public Controller(LiftManagementInterface liftManagementInterface, int liftOnePosition, int liftTwoPosition) {
        init(liftManagementInterface, liftOnePosition, liftTwoPosition);
    }

    /**
     * Initial position of lifts are initialised 0.
     *
     * @param liftManagementInterface Interface to provide callbacks to Main.
     */
    public Controller(LiftManagementInterface liftManagementInterface) {
        init(liftManagementInterface, 0, 0);
    }

    /**
     * @param liftManagementInterface Interface to provide callbacks to Main.
     * @param liftOnePosition         Initial position of lift 1.
     * @param liftTwoPosition         Initial position of lift 2.
     */
    private void init(LiftManagementInterface liftManagementInterface, int liftOnePosition, int liftTwoPosition) {
        this.liftManagementInterface = liftManagementInterface;
        nextStop = new int[]{NONE, NONE};
        currentPosition = new float[]{liftOnePosition, liftTwoPosition};
        processThread = new ProcessLift[2];
        open = new Semaphore[]{new Semaphore(1), new Semaphore(1)};
        gateOpen = new boolean[]{false, false};
    }

    /**
     * Initialise and set click listeners of all buttons.
     */
    @FXML
    public void initialize() {
        liftLeftGate = new ImageView[]{liftOneLeftGate, liftTwoLeftGate};
        liftRightGate = new ImageView[]{liftOneRightGate, liftTwoRightGate};
        liftOneInsideButton = new Button[]{liftOneInZero, liftOneInOne, liftOneInTwo, liftOneInThree, liftOneInFour, liftOneInFive, liftOneInSix, liftOneInSeven};
        liftTwoInsideButton = new Button[]{liftTwoInZero, liftTwoInOne, liftTwoInTwo, liftTwoInThree, liftTwoInFour, liftTwoInFive, liftTwoInSix, liftTwoInSeven};
        up = new Button[]{zeroUp, oneUp, twoUp, threeUp, fourUp, fiveUp, sixUp, new Button()};
        down = new Button[]{new Button(), oneDown, twoDown, threeDown, fourDown, fiveDown, sixDown, sevenDown};
        changeFloor(LIFT_ONE, getCurrentPosition(LIFT_ONE));
        changeFloor(LIFT_TWO, getCurrentPosition(LIFT_TWO));
        for (int i = 0; i < 8; i++) {
            int finalI = i;
            liftOneInsideButton[i].setOnAction(event -> {
                liftManagementInterface.onInsideButtonPress(LIFT_ONE, finalI);
                liftOneInsideButton[finalI].getStyleClass().add("selected-button");
                liftOneInsideButton[finalI].getStyleClass().remove("unselected-button");
            });
            liftTwoInsideButton[i].setOnAction(event -> {
                liftManagementInterface.onInsideButtonPress(LIFT_TWO, finalI);
                liftTwoInsideButton[finalI].getStyleClass().remove("unselected-button");
                liftTwoInsideButton[finalI].getStyleClass().add("selected-button");
            });
            up[i].setOnAction(event -> {
                liftManagementInterface.onUpPressed(finalI);
                up[finalI].getStyleClass().add("outside-selected-button");
                up[finalI].getStyleClass().remove("outside-unselected-button");
            });
            down[i].setOnAction(event -> {
                liftManagementInterface.onDownPressed(finalI);
                down[finalI].getStyleClass().add("outside-selected-button");
                down[finalI].getStyleClass().remove("outside-unselected-button");
            });
        }
        try {
            InputStream is1 = new FileInputStream("D:/Documents/Projects/LiftManagementSystem/src/res/up.png");
            InputStream is2 = new FileInputStream("D:/Documents/Projects/LiftManagementSystem/src/res/down.png");
            upImage = new Image(is1);
            downImage = new Image(is2);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Change the floor status of corresponding lift.
     *
     * @param lift  Lift id.
     * @param floor New floor which is to be set.
     */
    @Override
    public void changeFloor(int lift, int floor) {
        Platform.runLater(() -> {
            if (lift == LIFT_ONE)
                liftOneLabel.setText(String.valueOf(floor));
            else
                liftTwoLabel.setText(String.valueOf(floor));
        });
    }

    /**
     * Change the direction status of corresponding lift.
     *
     * @param lift      Lift id.
     * @param direction New direction of the lift.
     */
    @Override
    public void changeDirection(int lift, Direction direction) {
        Platform.runLater(() -> {
            Image tmp;
            switch (direction) {
                case GOING_DOWN:
                    tmp = downImage;
                    break;
                case GOING_UP:
                    tmp = upImage;
                    break;
                default:
                    tmp = null;
            }
            if (lift == LIFT_ONE)
                liftOneDirection.setImage(tmp);
            else
                liftTwoDirection.setImage(tmp);
        });
    }

    /**
     * Disable outside button so as to show that it has been serviced.
     *
     * @param floor     Floor at which button needs to be disabled.
     * @param direction Which direction of button must be disabled.
     */
    @Override
    public void disableOutsideButton(int floor, Direction direction) {
        Platform.runLater(() -> {
            if (direction == Direction.GOING_UP) {
                up[floor].getStyleClass().remove("outside-selected-button");
                up[floor].getStyleClass().add("outside-unselected-button");
            } else {
                down[floor].getStyleClass().remove("outside-selected-button");
                down[floor].getStyleClass().add("outside-unselected-button");
            }
        });
    }

    /**
     * Disable inside button of corresponding lift so as to show that it has been serviced.
     *
     * @param floor Floor at which button needs to be disabled.
     * @param lift  Lift id.
     */
    @Override
    public void disableInsideButton(int lift, int floor) {
        Platform.runLater(() -> {
            if (lift == LIFT_ONE) {
                liftOneInsideButton[floor].getStyleClass().add("unselected-button");
                liftOneInsideButton[floor].getStyleClass().remove("selected-button");
            } else {
                liftTwoInsideButton[floor].getStyleClass().add("unselected-button");
                liftTwoInsideButton[floor].getStyleClass().remove("selected-button");
            }
        });
    }

    /**
     * Check if the gate of corresponding lift is open.
     *
     * @param lift Lift id.
     * @return true if lift is open, otherwise false.
     */
    @Override
    public boolean isGateOpen(int lift) {
        return gateOpen[lift];
    }

    /**
     * Open the gate of corresponding core.Lift.
     *
     * @param lift Lift id.
     */
    @Override
    public void openLift(int lift) {
        try {
            liftManagementInterface.onReachedFloor(lift, getCurrentPosition(lift));
            open[lift].acquire();
            gateOpen[lift] = true;
            animateLift[lift] = new AnimateLift(lift);
            animateLift[lift].start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Thread which simulates gate opening and closing.
     */
    class AnimateLift extends Thread {
        int lift;

        /**
         * @param lift Id of lift which iis to be simulated.
         */
        AnimateLift(int lift) {
            this.lift = lift;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    gateStatus[lift] = GateStatus.OPENING;
                    while (Math.floor(current[lift]) > 0) {
                        liftLeftGate[lift].setFitWidth(current[lift] - decreaseBy);
                        liftRightGate[lift].setFitWidth(current[lift] - decreaseBy);
                        liftRightGate[lift].setX(liftRightGate[lift].getX() + decreaseBy);
                        current[lift] = current[lift] - decreaseBy;
                        Thread.sleep(50);
                    }
                    gateStatus[lift] = GateStatus.OPEN;
                    Thread.sleep(2000);
                    gateStatus[lift] = GateStatus.CLOSING;
                    while (Math.ceil(current[lift]) < 89f) {
                        liftLeftGate[lift].setFitWidth(current[lift] + decreaseBy);
                        liftRightGate[lift].setFitWidth(current[lift] + decreaseBy);
                        liftRightGate[lift].setX(liftRightGate[lift].getX() - decreaseBy);
                        current[lift] = current[lift] + decreaseBy;
                        Thread.sleep(50);
                    }
                    gateOpen[lift] = false;
                    open[lift].release();
                    break;
                } catch (InterruptedException e) {
                    gateStatus[lift] = GateStatus.OPENING;
                    Util.Log("core.Controller", "Gate reopened", true);
                }
            }
        }
    }

    /**
     * Move the corresponding lift to the given floor.
     *
     * @param lift  Lift id.
     * @param floor Floor where lift is to be moved.
     */
    @Override
    public void moveLift(int lift, int floor) {
        nextStop[lift] = floor;
        if (processThread[lift] == null) {
            processThread[lift] = new ProcessLift(lift);
        }
        if (processThread[lift].getState() == Thread.State.NEW)
            processThread[lift].start();
        else if (!processThread[lift].isAlive()) {
            processThread[lift] = new ProcessLift(lift);
            processThread[lift].start();
        }
    }

    /**
     * Get the closest floor to the given lift.
     *
     * @param lift Lift id.
     * @return Closest floor number.
     */
    @Override
    public int getCurrentPosition(int lift) {
        return Math.round(currentPosition[lift]);
    }

    /**
     * Check if the corresponding lift is below next stop.
     *
     * @param lift Lift id.
     * @return true if corresponding lift is below next stop
     */
    private boolean isBelow(int lift) {
        int tmp = Math.round(currentPosition[lift] * 10);
        if (tmp % 10 == 0)
            changeFloor(lift, tmp / 10);
        return tmp < nextStop[lift] * 10;
    }

    /**
     * Check if the corresponding lift is above next stop.
     *
     * @param lift Lift id.
     * @return true if corresponding lift is above next stop
     */
    private boolean isAbove(int lift) {
        int tmp = Math.round(currentPosition[lift] * 10);
        if (tmp % 10 == 0)
            changeFloor(lift, tmp / 10);
        return tmp > nextStop[lift] * 10;
    }

    /**
     * Set next stop of the corresponding lift and move it there.
     *
     * @param lift     Lift id.
     * @param nextStop Next floor where the lift should go.
     */
    @Override
    public void setNextStop(int lift, int nextStop) {
        this.nextStop[lift] = nextStop;
        moveLift(lift, nextStop);
    }

    /**
     * Ask the lift to reopen the gate of corresponding lift if it is closing state.
     *
     * @param lift Lift id.
     */
    @Override
    public void reOpen(int lift) {
        if (gateStatus[lift] == GateStatus.CLOSING)
            if (animateLift[lift] != null && animateLift[lift].isAlive())
                animateLift[lift].interrupt();
    }

    /**
     * Thread to simulate the movement of lift.
     */
    class ProcessLift extends Thread {
        int lift;

        /**
         * @param lift core.Lift to be simulated.
         */
        public ProcessLift(int lift) {
            super();
            this.lift = lift;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    open[lift].acquire();
                    liftManagementInterface.onMovementStarted(lift);
                    if (nextStop[lift] == NONE) {
                        open[lift].release();
                        break;
                    } else if (isBelow(lift)) {
                        for (; isBelow(lift); currentPosition[lift] += 0.1) {
                            Thread.sleep(500);
                            Util.Log("core.Lift " + lift, "Status : " + currentPosition[lift]);
                        }
                        open[lift].release();
                    } else if (isAbove(lift)) {
                        for (; isAbove(lift); currentPosition[lift] -= 0.1) {
                            Thread.sleep(500);
                            Util.Log("core.Lift " + lift, "Status : " + currentPosition[lift]);
                        }
                        open[lift].release();
                    } else {
                        open[lift].release();
                        openLift(lift);//TODO
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}