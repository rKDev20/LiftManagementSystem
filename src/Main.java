import core.Controller;
import core.LiftManagement;
import core.Util;
import interfaces.LiftManagementInterface;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main extends Application implements LiftManagementInterface {

    static LiftManagement liftManagement;

    @Override
    public void start(Stage stage) throws Exception {
        Controller controller = new Controller(this);
        liftManagement = new LiftManagement(controller);
        FXMLLoader loader = new FXMLLoader();
        loader.setController(controller);
        String fxmlDocPath = "D:/Documents/Projects/LiftManagementSystem/src/main_activity.fxml";
        FileInputStream fxmlStream = new FileInputStream(fxmlDocPath);
        AnchorPane root = loader.load(fxmlStream);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Lift Management System");
        stage.show();
    }

    public static void main(String[] args) {
        final String TAG = "Main";
        Application.launch(args);
        // if (args.length > 0 && args[0].compareTo("-v") == 0)
        Util.verbose = true;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String s;

        while (true) {
            try {
                s = br.readLine();
                if (s.substring(0, 3).compareTo("UP,") == 0)
                    liftManagement.onUpPressed(s.charAt(3) - 48);
                else if (s.substring(0, 5).compareTo("DOWN,") == 0)
                    liftManagement.onDownPressed(s.charAt(5) - 48);
                else if (s.substring(0, 4).compareTo("LIFT") == 0) {
                    if (s.charAt(4) - 48 == 1)
                        liftManagement.insideButtonPress(1, s.charAt(6) - 48);
                    else if (s.charAt(4) - 48 == 2)
                        liftManagement.insideButtonPress(2, s.charAt(6) - 48);
                }
            } catch (IOException | StringIndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onUpPressed(int i) {
        liftManagement.onUpPressed(i);
    }

    @Override
    public void onDownPressed(int i) {
        liftManagement.onDownPressed(i);
    }

    @Override
    public void onInsideButtonPress(int lift, int floor) {
        liftManagement.insideButtonPress(lift, floor);
    }

    @Override
    public void onReachedFloor(int lift, int floor) {
        liftManagement.onReachedFloor(lift, floor);
    }

    @Override
    public void onMovementStarted(int lift) {
        liftManagement.onMovementStarted(lift);
    }
}
