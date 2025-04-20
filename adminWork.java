package cms;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class adminWork {

    public Scene getAdminScene(Stage stage) {
        stage.setTitle("🧑‍💻 Admin Dashboard");

        // --- Header ---
        Label welcomeLabel = new Label("📚 Welcome, Admin");
        welcomeLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #2b2d42; -fx-font-weight: bold;");

        // --- Logout Button ---
        Button logoutBtn = new Button("🚪 Logout");
        logoutBtn.setStyle("-fx-background-color: #f9a826; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 10; -fx-padding: 8 20;");
        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle("-fx-background-color: #344955; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 10; -fx-padding: 8 20;"));
        logoutBtn.setOnMouseExited(e -> logoutBtn.setStyle("-fx-background-color: #f9a826; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 10; -fx-padding: 8 20;"));
        logoutBtn.setOnAction(e -> {
            Login login = new Login();
            login.start(stage);
        });

        HBox topBar = new HBox(welcomeLabel, logoutBtn);
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.setSpacing(140);
        topBar.setPadding(new Insets(20));

        // --- Admin Action Buttons ---
        VBox buttonsBox = new VBox(20);
        buttonsBox.setAlignment(Pos.CENTER);

        String[][] actions = {
                {"➕ Add Books", "addBooksWindow"},
                {"🔍 Search Books", "searchBooksWindow"},
                {"👥 User Management", "userManagementWindow"},
                {"📤 Issue Book", "issueBookWindow"},
                {"📥 Return Book", "returnBookWindow"},
                {"📋 Manage Books", "manageBooksWindow"},
                {"📜 View History", "historyWindow"}
        };

        for (String[] action : actions) {
            String buttonText = action[0];
            String windowName = action[1]; // You can replace this later

            Button btn = new Button(buttonText);
            btn.setPrefWidth(250);
            btn.setStyle("-fx-background-color: #3a86ff; -fx-text-fill: white; -fx-font-size: 16px; -fx-background-radius: 12;");
            btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #265ef2; -fx-text-fill: white; -fx-font-size: 16px; -fx-background-radius: 12;"));
            btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #3a86ff; -fx-text-fill: white; -fx-font-size: 16px; -fx-background-radius: 12;"));

            btn.setOnAction(e -> {
                switch (windowName) {
                    case "addBooksWindow":
                        stage.setScene(new addBook().addBookScene(stage));
                        break;
                    case "searchBooksWindow":
                        stage.setScene(new searchBook().searchBookScene(stage));
                        break;
                    case "userManagementWindow":
                        stage.setScene(new userManage().userManageScene(stage));
                        break;
                    case "issueBookWindow":
                        stage.setScene(new issueBook().issueBookScene(stage));
                        break;
                    case "returnBookWindow":
                        stage.setScene(new returnBook().returnBookScene(stage));
                        break;
                    case "manageBooksWindow":
                        stage.setScene(new manageBook().manageBookScene(stage));
                        break;
                    case "historyWindow":
                        stage.setScene(new historyView().historyViewScene(stage));
                        break;
                }
            });


            buttonsBox.getChildren().add(btn);
        }

        VBox dashboardBox = new VBox(50, topBar, buttonsBox);
        dashboardBox.setPadding(new Insets(40));
        dashboardBox.setAlignment(Pos.TOP_CENTER);

        StackPane root = new StackPane(dashboardBox);
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #fef9f9, #dde6ed);");

        return new Scene(root, 600, 600);
    }
}
