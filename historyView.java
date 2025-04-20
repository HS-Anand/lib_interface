package cms;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class historyView {

    public Scene historyViewScene(Stage stage) {
        stage.setTitle("➕ Admin - Add New Books");



        VBox dashboardBox = new VBox(50);
        dashboardBox.setPadding(new Insets(40));
        dashboardBox.setAlignment(Pos.TOP_CENTER);

        StackPane root = new StackPane(dashboardBox);
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #fef9f9, #dde6ed);");

        return new Scene(root, 600, 600);
    }
}
